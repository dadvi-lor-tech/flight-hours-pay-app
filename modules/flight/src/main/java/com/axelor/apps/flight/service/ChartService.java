package com.axelor.apps.flight.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axelor.apps.flight.db.ActualDuty;
import com.axelor.apps.flight.db.repo.ActualDutyRepository;
import com.google.inject.Inject;

public class ChartService {

  @Inject protected ActualDutyRepository actualDutyRepo;

  public List<Map<String, Object>> getCumulatedOnYear(int year) {
    List<Map<String, Object>> dataMapList = new ArrayList<>();

    LocalDate start = LocalDate.ofYearDay(year, 1);
    LocalDate end = LocalDate.ofYearDay(year + 1, 1);

    for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
      Map<String, Object> dataMap = new HashMap<>();
      dataMap.put("day", date);
      dataMap.put("hours", getCumulatedHoursOnYear(date));
      dataMapList.add(dataMap);
    }

    return dataMapList;
  }

  private BigDecimal getCumulatedHoursOnYear(LocalDate date) {
    BigDecimal cumulated = BigDecimal.ZERO;
    List<ActualDuty> duties =
        actualDutyRepo
            .all()
            .filter("self.departureDate BETWEEN :startOfYear AND :date")
            .bind("startOfYear", date.withDayOfYear(1))
            .bind("date", date)
            .fetch();

    for (ActualDuty actualDuty : duties) {
      cumulated = cumulated.add(BigDecimal.valueOf(actualDuty.getActualInboundDuration()));
      cumulated = cumulated.add(BigDecimal.valueOf(actualDuty.getActualOutboundDuration()));
    }

    return cumulated.divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
  }

  public List<Map<String, Object>> getCumulatedOnPeriod(LocalDate inputDate) {
    List<Map<String, Object>> dataMapList = new ArrayList<>();

    LocalDate start = inputDate.minusDays(30);
    LocalDate end = inputDate.plusDays(30);

    for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
      Map<String, Object> dataMap = new HashMap<>();
      dataMap.put("day", date);
      dataMap.put("hours", getCumulatedHoursOnPeriod(date));
      dataMapList.add(dataMap);
    }

    return dataMapList;
  }

  private BigDecimal getCumulatedHoursOnPeriod(LocalDate date) {
    BigDecimal cumulated = BigDecimal.ZERO;
    List<ActualDuty> duties =
        actualDutyRepo
            .all()
            .filter("self.departureDate BETWEEN :startOfPeriod AND :date")
            .bind("startOfPeriod", date.minusDays(28))
            .bind("date", date)
            .fetch();

    for (ActualDuty actualDuty : duties) {
      cumulated = cumulated.add(BigDecimal.valueOf(actualDuty.getActualInboundDuration()));
      cumulated = cumulated.add(BigDecimal.valueOf(actualDuty.getActualOutboundDuration()));
    }

    return cumulated.divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
  }
}
