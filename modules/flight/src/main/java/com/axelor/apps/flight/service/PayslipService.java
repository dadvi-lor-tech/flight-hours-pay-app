package com.axelor.apps.flight.service;

import com.axelor.apps.flight.db.Payslip;
import com.axelor.apps.flight.exception.FlightException;

public interface PayslipService {

  void compute(Payslip payslip) throws FlightException;
}
