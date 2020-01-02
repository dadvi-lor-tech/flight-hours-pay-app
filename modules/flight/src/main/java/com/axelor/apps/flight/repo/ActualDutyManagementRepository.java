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

import com.axelor.apps.flight.db.ActualDuty;
import com.axelor.apps.flight.db.repo.ActualDutyRepository;

public class ActualDutyManagementRepository extends ActualDutyRepository {

  @Override
  public ActualDuty save(ActualDuty actualDuty) {
    actualDuty.setName(
        Boolean.FALSE.equals(actualDuty.getIsLeave()) ? actualDuty.getDuty().getName() : "Leave");
    return super.save(actualDuty);
  }
}
