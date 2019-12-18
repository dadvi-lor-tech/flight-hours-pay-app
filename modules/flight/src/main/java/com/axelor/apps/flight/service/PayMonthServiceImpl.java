package com.axelor.apps.flight.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.axelor.apps.flight.db.ActualDuty;
import com.axelor.apps.flight.db.LeaveRate;
import com.axelor.apps.flight.db.PayMonth;
import com.axelor.apps.flight.db.Sbh;
import com.axelor.apps.flight.db.repo.ActualDutyRepository;
import com.axelor.apps.flight.exception.FlightException;
import com.axelor.auth.AuthUtils;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class PayMonthServiceImpl implements PayMonthService {

  private static final BigDecimal THRESHOLD_MINIMUM = BigDecimal.valueOf(67);
  private static final BigDecimal OVERTIME_VALUE = BigDecimal.valueOf(0.25);

  private static final BigDecimal OOB_UNIT_VALUE = BigDecimal.valueOf(75);
  private static final BigDecimal WOFF_UNIT_VALUE = BigDecimal.valueOf(400);

  @Override
  @Transactional(rollbackOn = {FlightException.class, RuntimeException.class})
  public void compute(PayMonth payMonth) throws FlightException {
    computeHours(payMonth);
    computeFlightPay(payMonth);

    computeThreshold(payMonth);
    computeOvertime(payMonth);

    computeExtras(payMonth);

    computeTotal(payMonth);
  }

  /**
   * Computes the number of scheduled and actual hours in the pay month.
   *
   * @param payMonth
   */
  protected void computeHours(PayMonth payMonth) {
    List<ActualDuty> actualDuties = getPeriodDuties(payMonth);

    int scheduledDuration =
        actualDuties
            .stream()
            .mapToInt(
                duty -> duty.getEstimatedInboundDuration() + duty.getEstimatedOutboundDuration())
            .sum();
    int actualDuration = actualDuties.stream().mapToInt(ActualDuty::getActualDuration).sum();

    payMonth.setScheduledDuration(scheduledDuration);
    payMonth.setActualDuration(actualDuration);
    payMonth.setDifferenceHours((actualDuration - scheduledDuration) / 3600);
  }

  /**
   * Computes the flight pay according to the hourly rate at the last day of the pay month
   *
   * @param payMonth
   * @throws FlightException
   */
  protected void computeFlightPay(PayMonth payMonth) throws FlightException {
    Integer hours = payMonth.getScheduledDuration();

    LocalDate payDate = payMonth.getToDate();
    Optional<Sbh> sbhOpt =
        AuthUtils.getUser()
            .getSbhList()
            .stream()
            .filter(
                sbh -> payDate.isAfter(sbh.getStartDate()) && !payDate.isAfter(sbh.getEndDate()))
            .findAny();

    if (!sbhOpt.isPresent()) {
      throw new FlightException("Please fill your current SBH.");
    }

    payMonth.setFlightSalary(
        sbhOpt.get().getHourlyRate().multiply(BigDecimal.valueOf(hours / 3600)));
  }

  /**
   * Computes the threshold according to some given computation rules.
   *
   * @param payMonth
   */
  protected void computeThreshold(PayMonth payMonth) {
    BigDecimal subtrahend =
        BigDecimal.valueOf(payMonth.getStepNb())
            .subtract(BigDecimal.valueOf(20))
            .divide(BigDecimal.valueOf(6), 4, RoundingMode.HALF_UP);
    BigDecimal computedThreshold = BigDecimal.valueOf(75).subtract(subtrahend);

    payMonth.setThreshold(computedThreshold.max(THRESHOLD_MINIMUM));
  }

  protected void computeOvertime(PayMonth payMonth) {
    BigDecimal threshold = payMonth.getThreshold();
    BigDecimal actualHours =
        BigDecimal.valueOf(payMonth.getActualDuration())
            .divide(BigDecimal.valueOf(3600), 4, RoundingMode.HALF_UP);

    if (actualHours.compareTo(threshold) > 0) {
      payMonth.setOvertimeHours(actualHours.subtract(threshold));
    } else {
      payMonth.setOvertimeHours(BigDecimal.ZERO);
    }

    payMonth.setOvertimeValue(payMonth.getOvertimeHours().multiply(OVERTIME_VALUE));
  }

  protected void computeTotal(PayMonth payMonth) {
    BigDecimal total =
        payMonth
            .getBaseSalary()
            .add(payMonth.getOobValue())
            .add(payMonth.getAlValue())
            .add(payMonth.getWoffValue())
            .add(payMonth.getFlightSalary())
            .add(payMonth.getOvertimeValue());

    payMonth.setTotalSalary(total);
  }

  protected void computeExtras(PayMonth payMonth) throws FlightException {
    List<ActualDuty> actualDuties = getPeriodDuties(payMonth);

    // OOB computation
    countOob(payMonth, actualDuties);

    // AL computation
    countAl(payMonth, actualDuties);

    // WOFF computation
    countWoff(payMonth, actualDuties);
  }

  private List<ActualDuty> getPeriodDuties(PayMonth payMonth) {
    return Beans.get(ActualDutyRepository.class)
        .all()
        .filter("self.departureDate BETWEEN :fromDate AND :toDate")
        .bind("fromDate", payMonth.getFromDate())
        .bind("toDate", payMonth.getToDate())
        .fetch();
  }

  protected void countOob(PayMonth payMonth, List<ActualDuty> actualDuties) {
    Integer oobNb = (int) actualDuties.stream().filter(ActualDuty::getIsOob).count();
    payMonth.setOobNb(oobNb);
    payMonth.setOobValue(OOB_UNIT_VALUE.multiply(BigDecimal.valueOf(oobNb)));
  }

  protected void countAl(PayMonth payMonth, List<ActualDuty> actualDuties) throws FlightException {
    int alNb = 0;
    BigDecimal alValue = BigDecimal.ZERO;

    for (ActualDuty actualDuty : actualDuties) {
      if (Boolean.TRUE.equals(actualDuty.getDuty().getIsLeave())) {
        alNb++;
        alValue = alValue.add(computeLeaveValue(actualDuty));
      }
    }

    payMonth.setAlNb(alNb);
    payMonth.setAlValue(alValue);
  }

  private BigDecimal computeLeaveValue(ActualDuty actualDuty) throws FlightException {
    LocalDate leaveDate = actualDuty.getDepartureDate();
    Optional<LeaveRate> currentLeaveRate =
        actualDuty
            .getDuty()
            .getLeaveRateList()
            .stream()
            .filter(
                rate ->
                    leaveDate.isAfter(rate.getStartDate()) && leaveDate.isBefore(rate.getEndDate()))
            .findAny();

    return currentLeaveRate
        .orElseThrow(() -> new FlightException("Please fill a leave rate to apply on " + leaveDate))
        .getDailyRate();
  }

  protected void countWoff(PayMonth payMonth, List<ActualDuty> actualDuties) {
    Integer woffNb = (int) actualDuties.stream().filter(ActualDuty::getIsWoff).count();
    payMonth.setWoffNb(woffNb);
    payMonth.setWoffValue(WOFF_UNIT_VALUE.multiply(BigDecimal.valueOf(woffNb)));
  }
}
