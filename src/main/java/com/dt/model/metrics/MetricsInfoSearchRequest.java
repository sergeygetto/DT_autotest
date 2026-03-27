package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель для POST /metrics-info/search
 */
public class MetricsInfoSearchRequest {

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("presetName")
    private String presetName;

    @JsonProperty("period")
    private MetricsInfoSearchPeriod period;

    public MetricsInfoSearchRequest() {
    }

    public MetricsInfoSearchRequest(String userName, String presetName, MetricsInfoSearchPeriod period) {
        this.userName = userName;
        this.presetName = presetName;
        this.period = period;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPresetName() {
        return presetName;
    }

    public void setPresetName(String presetName) {
        this.presetName = presetName;
    }

    public MetricsInfoSearchPeriod getPeriod() {
        return period;
    }

    public void setPeriod(MetricsInfoSearchPeriod period) {
        this.period = period;
    }
}

