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
