package com.axelor.apps.flight.web;

import com.axelor.apps.flight.db.PayMonth;
import com.axelor.apps.flight.db.repo.PayMonthRepository;
import com.axelor.apps.flight.exception.FlightException;
import com.axelor.apps.flight.service.PayMonthService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PayMonthController {

  public void compute(ActionRequest request, ActionResponse response) {
    try {
      PayMonth payMonth = request.getContext().asType(PayMonth.class);
      payMonth = Beans.get(PayMonthRepository.class).find(payMonth.getId());

      Beans.get(PayMonthService.class).compute(payMonth);
      response.setReload(true);

    } catch (FlightException e) {
      response.setError(e.getMessage());
    }
  }
}
