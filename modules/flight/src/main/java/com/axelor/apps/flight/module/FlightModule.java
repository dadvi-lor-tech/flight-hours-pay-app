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
package com.axelor.apps.flight.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.flight.db.repo.ActualDutyRepository;
import com.axelor.apps.flight.db.repo.DutyRepository;
import com.axelor.apps.flight.repo.ActualDutyManagementRepository;
import com.axelor.apps.flight.repo.DutyManagementRepository;
import com.axelor.apps.flight.service.ActualDutyService;
import com.axelor.apps.flight.service.ActualDutyServiceImpl;
import com.axelor.apps.flight.service.PayslipService;
import com.axelor.apps.flight.service.PayslipServiceImpl;

public class FlightModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(PayslipService.class).to(PayslipServiceImpl.class);
    bind(ActualDutyService.class).to(ActualDutyServiceImpl.class);
    bind(DutyRepository.class).to(DutyManagementRepository.class);
    bind(ActualDutyRepository.class).to(ActualDutyManagementRepository.class);
  }
}
