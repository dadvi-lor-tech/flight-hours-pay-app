/*
 * Flight hours & pay app
 *
 * Copyright (C) 2020 David Trochel (<https://github.com/trochel>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
