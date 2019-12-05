package com.axelor.apps.flight.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.flight.service.PayMonthService;
import com.axelor.apps.flight.service.PayMonthServiceImpl;

public class FlightModule extends AxelorModule {

	@Override
	protected void configure() {
		bind(PayMonthService.class).to(PayMonthServiceImpl.class);
	}
}
