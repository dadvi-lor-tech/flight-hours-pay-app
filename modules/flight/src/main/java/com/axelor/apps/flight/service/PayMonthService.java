package com.axelor.apps.flight.service;

import com.axelor.apps.flight.db.PayMonth;
import com.axelor.apps.flight.exception.FlightException;

public interface PayMonthService {

  void compute(PayMonth payMonth) throws FlightException;
}
