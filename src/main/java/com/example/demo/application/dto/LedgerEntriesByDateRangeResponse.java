package com.example.demo.application.dto;

import java.util.List;

public record LedgerEntriesByDateRangeResponse(
    DateRange dateRange,
    List<LedgerResult> results
) {
}
