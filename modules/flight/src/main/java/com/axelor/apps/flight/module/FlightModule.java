package com.axelor.apps.flight.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.flight.db.repo.DutyRepository;
import com.axelor.apps.flight.repo.DutyManagmentRepository;
import com.axelor.apps.flight.service.PayslipService;
import com.axelor.apps.flight.service.PayslipServiceImpl;

public class FlightModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(PayslipService.class).to(PayslipServiceImpl.class);
    bind(DutyRepository.class).to(DutyManagmentRepository.class);
  }
}
