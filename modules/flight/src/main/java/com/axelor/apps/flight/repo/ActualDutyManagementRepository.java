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
