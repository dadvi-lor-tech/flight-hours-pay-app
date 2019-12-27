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
