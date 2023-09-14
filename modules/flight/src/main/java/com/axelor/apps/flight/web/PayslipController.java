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
