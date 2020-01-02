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

import com.axelor.apps.flight.service.ChartService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ChartController {

  public void getCumulatedOnYear(ActionRequest request, ActionResponse response) {
    int year = Integer.parseInt((String) request.getContext().get("year"));
    response.setData(Beans.get(ChartService.class).getCumulatedOnYear(year));
  }

  public void getCumulatedOnPeriod(ActionRequest request, ActionResponse response) {
    LocalDate date = LocalDate.parse((String) request.getContext().get("date"));
    response.setData(Beans.get(ChartService.class).getCumulatedOnPeriod(date));
  }
}
