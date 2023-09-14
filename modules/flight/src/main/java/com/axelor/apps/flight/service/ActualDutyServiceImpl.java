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

    LocalDate date = actualDuty.getDepartureDate();
    Optional<ScheduledTime> scheduledTime =
        duty.getScheduledTimeList()
            .stream()
            .filter(time -> date.isAfter(time.getStartDate()) && date.isBefore(time.getEndDate()))
            .findAny();

    if (scheduledTime.isPresent()) {
      actualDuty.setEstimatedInboundDuration(scheduledTime.get().getInboundDuration());
      actualDuty.setEstimatedOutboundDuration(scheduledTime.get().getOutboundDuration());

    } else {
      throw new FlightException("Please fill the duration(s) to apply of this duty on " + date);
    }
  }
}
