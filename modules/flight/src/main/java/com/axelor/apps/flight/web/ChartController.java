package com.axelor.apps.flight.web;

import java.time.LocalDate;

import com.axelor.apps.flight.service.ChartService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ChartController {

  public void getCumulatedOnYear(ActionRequest request, ActionResponse response) {
    int year = Integer.parseInt((String) request.getContext().get("year"));
    response.setData(Beans.get(ChartService.class).getCumulatedOnYear(year));
  }

  public void getCumulatedOnPeriod(ActionRequest request, ActionResponse response) {
    LocalDate date = LocalDate.parse((String) request.getContext().get("date"));
    response.setData(Beans.get(ChartService.class).getCumulatedOnPeriod(date));
  }
}
