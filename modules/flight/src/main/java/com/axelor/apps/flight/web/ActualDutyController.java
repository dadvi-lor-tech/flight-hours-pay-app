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
package com.axelor.apps.flight.web;

import java.time.LocalDate;

import com.axelor.apps.flight.db.ActualDuty;
import com.axelor.apps.flight.exception.FlightException;
import com.axelor.apps.flight.service.ActualDutyService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ActualDutyController {

  public void createActualDuty(ActionRequest request, ActionResponse response) {
    Object dateObj = request.getContext().get("departureDate");
    response.setValue("departureDate", dateObj != null ? (LocalDate) dateObj : null);
  }

  public void setEstimatedDurations(ActionRequest request, ActionResponse response) {
    try {
      ActualDuty actualDuty = request.getContext().asType(ActualDuty.class);
      Beans.get(ActualDutyService.class).setEstimatedDurations(actualDuty);

      response.setValue("estimatedInboundDuration", actualDuty.getEstimatedInboundDuration());
      response.setValue("estimatedOutboundDuration", actualDuty.getEstimatedOutboundDuration());

    } catch (FlightException e) {
      response.setError(e.getMessage());
    }
  }
}
