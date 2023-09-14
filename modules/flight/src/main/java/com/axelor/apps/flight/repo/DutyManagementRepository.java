package com.axelor.apps.flight.repo;

import java.time.LocalDate;
import java.util.Optional;

import com.axelor.apps.flight.db.Duty;
import com.axelor.apps.flight.db.ScheduledTime;
import com.axelor.apps.flight.db.repo.DutyRepository;

public class DutyManagementRepository extends DutyRepository {

  @Override
  public Duty save(Duty duty) {
    LocalDate now = LocalDate.now();
    Optional<ScheduledTime> scheduledTimeOpt =
        duty.getScheduledTimeList()
            .stream()
            .filter(time -> now.isAfter(time.getStartDate()) && !now.isAfter(time.getEndDate()))
            .findAny();

    if (scheduledTimeOpt.isPresent()) {
      ScheduledTime scheduledTime = scheduledTimeOpt.get();
      duty.setEstimatedInboundDuration(scheduledTime.getInboundDuration());
      duty.setEstimatedOutboundDuration(scheduledTime.getOutboundDuration());
    }

    return super.save(duty);
  }
}
