package com.axelor.apps.flight.service;

import com.axelor.apps.flight.db.ActualDuty;
import com.axelor.apps.flight.exception.FlightException;

public interface ActualDutyService {

  void setEstimatedDurations(ActualDuty actualDuty) throws FlightException;
}
