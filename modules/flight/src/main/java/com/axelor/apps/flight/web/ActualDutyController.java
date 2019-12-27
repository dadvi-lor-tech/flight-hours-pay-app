package com.axelor.apps.flight.web;

import com.axelor.apps.flight.db.ActualDuty;
import com.axelor.apps.flight.exception.FlightException;
import com.axelor.apps.flight.service.ActualDutyService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ActualDutyController {

  public void setEstimatedDurations(ActionRequest request, ActionResponse response) {
    try {
      ActualDuty actualDuty = request.getContext().asType(ActualDuty.class);
      Beans.get(ActualDutyService.class).setEstimatedDurations(actualDuty);

      response.setValue("estimatedInboundDuration", actualDuty.getEstimatedInboundDuration());
      response.setValue("estimatedOutboundDuration", actualDuty.getEstimatedOutboundDuration());

    } catch (FlightException e) {
      response.setError(e.getMessage());
    }
  }
}
