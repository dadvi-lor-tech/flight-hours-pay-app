package com.axelor.apps.flight.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.axelor.apps.flight.db.ActualDuty;
import com.axelor.apps.flight.db.Duty;
import com.axelor.apps.flight.db.LeaveRate;
import com.axelor.apps.flight.db.PayMonth;
import com.axelor.apps.flight.db.Sbh;
import com.axelor.apps.flight.db.repo.ActualDutyRepository;
import com.axelor.apps.flight.exception.FlightException;
import com.axelor.auth.AuthUtils;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class PayMonthServiceImpl implements PayMonthService {

  protected static final BigDecimal OOB_UNIT_VALUE = BigDecimal.valueOf(75);
  protected static final BigDecimal WOFF_UNIT_VALUE = BigDecimal.valueOf(400);

  @Override
  @Transactional(rollbackOn = {FlightException.class, RuntimeException.class})
  public void compute(PayMonth payMonth) throws FlightException {
    computeActualHours(payMonth);
    computeFlightPay(payMonth);

    computeThreshold(payMonth);
    computeOvertime(payMonth);

    computeExtras(payMonth);

    computeTotal(payMonth);
  }

  protected void computeActualHours(PayMonth payMonth) {
    List<ActualDuty> actualDuties =
        Beans.get(ActualDutyRepository.class)
            .all()
            .filter("self.duty.isLeave = 'f' AND self.departureDate BETWEEN :fromDate AND :toDate")
            .bind("fromDate", payMonth.getFromDate())
            .bind("toDate", payMonth.getToDate())
            .fetch();

    Integer actualHours =
        actualDuties
            .stream()
            .map(ActualDuty::getActualDuration)
            .reduce(Integer.valueOf(0), Integer::sum);
    payMonth.setActualHours(BigDecimal.valueOf(actualHours));
  }

  protected void computeFlightPay(PayMonth payMonth) throws FlightException {
    BigDecimal hours = payMonth.getScheduledHours();

    LocalDate now = LocalDate.now();
    Optional<Sbh> sbhOpt =
        AuthUtils.getUser()
            .getSbhList()
            .stream()
            .filter(sbh -> now.isAfter(sbh.getStartDate()) && now.isBefore(sbh.getEndDate()))
            .findAny();

    if (!sbhOpt.isPresent()) {
      throw new FlightException("Please fill your current SBH.");
    }

    payMonth.setFlightSalary(hours.multiply(sbhOpt.get().getHourlyRate()));
  }

  protected void computeThreshold(PayMonth payMonth) {
    BigDecimal subtrahend =
        BigDecimal.valueOf(payMonth.getStepNb())
            .subtract(BigDecimal.valueOf(20))
            .divide(BigDecimal.valueOf(6), 4, RoundingMode.HALF_UP);
    BigDecimal computedThreshold = BigDecimal.valueOf(75).subtract(subtrahend);

    payMonth.setThreshold(computedThreshold.max(BigDecimal.valueOf(67)));
  }

  protected void computeOvertime(PayMonth payMonth) {
    BigDecimal threshold = payMonth.getThreshold();

    if (payMonth.getActualHours().compareTo(threshold) > 0) {
      payMonth.setOvertimeHours(payMonth.getActualHours().subtract(threshold));
    } else {
      payMonth.setOvertimeHours(BigDecimal.ZERO);
    }

    payMonth.setOvertimeValue(payMonth.getOvertimeHours().multiply(BigDecimal.valueOf(0.25)));
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
    List<ActualDuty> actualDuties =
        Beans.get(ActualDutyRepository.class)
            .all()
            .filter("self.departureDate BETWEEN :fromDate AND :toDate")
            .bind("fromDate", payMonth.getFromDate())
            .bind("toDate", payMonth.getToDate())
            .fetch();

    // OOB computation
    countOob(payMonth, actualDuties);

    // AL computation
    countAl(payMonth, actualDuties);

    // WOFF computation
    countWoff(payMonth, actualDuties);
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
      Duty duty = actualDuty.getDuty();
      if (Boolean.TRUE.equals(duty.getIsLeave())) {
        alNb++;
        alValue = alValue.add(computeLeaveValue(actualDuty, duty));
      }
    }

    payMonth.setAlNb(alNb);
    payMonth.setAlValue(alValue);
  }

  private BigDecimal computeLeaveValue(ActualDuty actualDuty, Duty duty) throws FlightException {
    LocalDate leaveDate = actualDuty.getDepartureDate();
    Optional<LeaveRate> currentLeaveRate =
        duty.getLeaveRateList()
            .stream()
            .filter(
                rate ->
                    leaveDate.isAfter(rate.getStartDate()) && leaveDate.isBefore(rate.getEndDate()))
            .findAny();

    // TODO: check
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
