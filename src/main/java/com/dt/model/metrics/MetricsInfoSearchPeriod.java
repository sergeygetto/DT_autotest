package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель периода для POST /metrics-info/search
 */
public class MetricsInfoSearchPeriod {

    @JsonProperty("start")
    private String start;

    @JsonProperty("end")
    private String end;

    public MetricsInfoSearchPeriod() {
    }

    public MetricsInfoSearchPeriod(String start, String end) {
        this.start = start;
        this.end = end;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}

