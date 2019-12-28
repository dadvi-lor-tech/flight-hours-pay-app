package com.axelor.apps.flight.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.axelor.apps.flight.db.ActualDuty;
import com.axelor.apps.flight.db.LeaveRate;
import com.axelor.apps.flight.db.Payslip;
import com.axelor.apps.flight.db.Sbh;
import com.axelor.apps.flight.db.repo.ActualDutyRepository;
import com.axelor.apps.flight.exception.FlightException;
import com.axelor.auth.AuthUtils;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class PayslipServiceImpl implements PayslipService {

  @Override
  @Transactional(rollbackOn = {FlightException.class, RuntimeException.class})
  public void compute(Payslip payslip) throws FlightException {
    computeHours(payslip);
    computeFlightPay(payslip);

    computeSectorNb(payslip);
    computeThreshold(payslip);
    computeOvertime(payslip);

    computeExtras(payslip);

    computeTotal(payslip);
  }

  /**
   * Computes the number of scheduled and actual hours in the payslip.
   *
   * @param payslip
   */
  protected void computeHours(Payslip payslip) {
    int scheduledDuration = 0;
    int actualDuration = 0;

    for (ActualDuty actualDuty : getPeriodDuties(payslip)) {
      scheduledDuration +=
          actualDuty.getEstimatedInboundDuration() + actualDuty.getEstimatedOutboundDuration();
      actualDuration +=
          actualDuty.getActualInboundDuration() + actualDuty.getActualOutboundDuration();
    }

    payslip.setScheduledDuration(scheduledDuration);
    payslip.setActualDuration(actualDuration);

    payslip.setDifferenceHours(BigDecimal.valueOf((actualDuration - scheduledDuration) / 3600.0));
  }

  /**
   * Computes the flight pay according to the hourly rate at the last day of the payslip
   *
   * @param payslip
   * @throws FlightException
   */
  protected void computeFlightPay(Payslip payslip) throws FlightException {
    payslip.setFlightSalary(
        getCurrentSbhRate(payslip)
            .multiply(BigDecimal.valueOf(payslip.getScheduledDuration() / 3600.0)));
  }

  private BigDecimal getCurrentSbhRate(Payslip payslip) throws FlightException {
    LocalDate fromDate = LocalDate.of(payslip.getYear(), payslip.getMonth(), 1);
    LocalDate payDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());

    Optional<Sbh> sbhOpt =
        AuthUtils.getUser()
            .getSbhList()
            .stream()
            .filter(
                sbh -> payDate.isAfter(sbh.getStartDate()) && !payDate.isAfter(sbh.getEndDate()))
            .findAny();

    return sbhOpt
        .orElseThrow(() -> new FlightException("Please fill the SBH to apply on " + payDate))
        .getHourlyRate();
  }

  private void computeSectorNb(Payslip payslip) {
    int sectorNb =
        getPeriodDuties(payslip)
            .stream()
            .mapToInt(
                duty ->
                    duty.getEstimatedInboundDuration().compareTo(0)
                        + duty.getEstimatedOutboundDuration().compareTo(0))
            .sum();

    payslip.setSectorNb(sectorNb);
  }

  /**
   * Computes the threshold according to some given computation rules.
   *
   * @param payslip
   */
  protected void computeThreshold(Payslip payslip) {
    BigDecimal subtrahend =
        BigDecimal.valueOf(payslip.getSectorNb())
            .subtract(BigDecimal.valueOf(20))
            .divide(BigDecimal.valueOf(6), 4, RoundingMode.HALF_UP);
    BigDecimal computedThreshold =
        BigDecimal.valueOf(75).subtract(subtrahend).multiply(BigDecimal.valueOf(3600));

    payslip.setThreshold(
        computedThreshold.max(AuthUtils.getUser().getThresholdMinimum()).intValue());
  }

  protected void computeOvertime(Payslip payslip) throws FlightException {
    BigDecimal threshold = BigDecimal.valueOf(payslip.getThreshold());
    BigDecimal actualHours =
        BigDecimal.valueOf(payslip.getActualDuration())
            .divide(BigDecimal.valueOf(3600), 4, RoundingMode.HALF_UP);

    if (actualHours.compareTo(threshold) > 0) {
      payslip.setOvertimeHours(actualHours.subtract(threshold));
    } else {
      payslip.setOvertimeHours(BigDecimal.ZERO);
    }

    payslip.setOvertimeValue(
        payslip
            .getOvertimeHours()
            .multiply(AuthUtils.getUser().getOvertimeUnitValue())
            .multiply(getCurrentSbhRate(payslip)));
  }

  protected void computeTotal(Payslip payslip) {
    BigDecimal total =
        payslip
            .getBaseSalary()
            .add(payslip.getOobValue())
            .add(payslip.getAlValue())
            .add(payslip.getWoffValue())
            .add(payslip.getFlightSalary())
            .add(payslip.getOvertimeValue());

    payslip.setTotalSalary(total);
  }

  protected void computeExtras(Payslip payslip) throws FlightException {
    List<ActualDuty> actualDuties = getPeriodDuties(payslip);

    // OOB computation
    countOob(payslip, actualDuties);

    // AL computation
    countAl(payslip, actualDuties);

    // WOFF computation
    countWoff(payslip, actualDuties);
  }

  private List<ActualDuty> getPeriodDuties(Payslip payslip) {
    LocalDate fromDate = LocalDate.of(payslip.getYear(), payslip.getMonth(), 1);
    LocalDate toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());

    return Beans.get(ActualDutyRepository.class)
        .all()
        .filter("self.departureDate BETWEEN :fromDate AND :toDate")
        .bind("fromDate", fromDate)
        .bind("toDate", toDate)
        .fetch();
  }

  protected void countOob(Payslip payslip, List<ActualDuty> actualDuties) {
    Integer oobNb = (int) actualDuties.stream().filter(ActualDuty::getIsOob).count();
    payslip.setOobNb(oobNb);
    payslip.setOobValue(AuthUtils.getUser().getOobUnitValue().multiply(BigDecimal.valueOf(oobNb)));
  }

  protected void countAl(Payslip payslip, List<ActualDuty> actualDuties) throws FlightException {
    int alNb = 0;
    BigDecimal alValue = BigDecimal.ZERO;

    for (ActualDuty actualDuty : actualDuties) {
      if (Boolean.TRUE.equals(actualDuty.getIsLeave())) {
        alNb++;
        alValue = alValue.add(computeLeaveValue(actualDuty));
      }
    }

    payslip.setAlNb(alNb);
    payslip.setAlValue(alValue);
  }

  private BigDecimal computeLeaveValue(ActualDuty actualDuty) throws FlightException {
    LocalDate leaveDate = actualDuty.getDepartureDate();
    Optional<LeaveRate> currentLeaveRate =
        AuthUtils.getUser()
            .getLeaveRateList()
            .stream()
            .filter(
                rate ->
                    leaveDate.isAfter(rate.getStartDate()) && leaveDate.isBefore(rate.getEndDate()))
            .findAny();

    return currentLeaveRate
        .orElseThrow(
            () ->
                new FlightException("Please fill a leave rate in profile to apply on " + leaveDate))
        .getDailyRate();
  }

  protected void countWoff(Payslip payslip, List<ActualDuty> actualDuties) {
    Integer woffNb = (int) actualDuties.stream().filter(ActualDuty::getIsWoff).count();
    payslip.setWoffNb(woffNb);
    payslip.setWoffValue(
        AuthUtils.getUser().getWoffUnitValue().multiply(BigDecimal.valueOf(woffNb)));
  }
}
