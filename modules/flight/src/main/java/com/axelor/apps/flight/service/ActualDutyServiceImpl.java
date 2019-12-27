package com.axelor.apps.flight.service;

import java.time.LocalDate;
import java.util.Optional;

import com.axelor.apps.flight.db.ActualDuty;
import com.axelor.apps.flight.db.Duty;
import com.axelor.apps.flight.db.ScheduledTime;
import com.axelor.apps.flight.db.repo.DutyRepository;
import com.axelor.apps.flight.exception.FlightException;
import com.axelor.inject.Beans;

public class ActualDutyServiceImpl implements ActualDutyService {

  @Override
  public void setEstimatedDurations(ActualDuty actualDuty) throws FlightException {
    Duty duty = Beans.get(DutyRepository.class).find(actualDuty.getDuty().getId());

    LocalDate dutyDate = actualDuty.getDepartureDate();
    Optional<ScheduledTime> currentLeaveRate =
        duty.getScheduledTimeList()
            .stream()
            .filter(
                scheduledTime ->
                    dutyDate.isAfter(scheduledTime.getStartDate())
                        && dutyDate.isBefore(scheduledTime.getEndDate()))
            .findAny();

    if (currentLeaveRate.isPresent()) {
      actualDuty.setEstimatedInboundDuration(currentLeaveRate.get().getInboundDuration());
      actualDuty.setEstimatedOutboundDuration(currentLeaveRate.get().getOutboundDuration());

    } else {
      throw new FlightException("Please fill the duration(s) to apply of this duty on " + dutyDate);
    }
  }
}
