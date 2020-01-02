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

import com.axelor.apps.flight.db.Payslip;
import com.axelor.apps.flight.db.repo.PayslipRepository;
import com.axelor.apps.flight.exception.FlightException;
import com.axelor.apps.flight.service.PayslipService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PayslipController {

  public void compute(ActionRequest request, ActionResponse response) {
    try {
      Payslip payslip = request.getContext().asType(Payslip.class);
      payslip = Beans.get(PayslipRepository.class).find(payslip.getId());

      Beans.get(PayslipService.class).compute(payslip);
      response.setReload(true);

    } catch (FlightException e) {
      response.setError(e.getMessage());
    }
  }
}
