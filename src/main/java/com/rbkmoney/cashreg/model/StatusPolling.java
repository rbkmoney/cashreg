package com.rbkmoney.cashreg.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusPolling {

    @JsonProperty(value = "start_date_time_polling")
    private Instant startDateTimePolling;

    @JsonProperty(value = "max_date_time_polling")
    private Instant maxDateTimePolling;

    public Boolean timeIsOver(Instant currentTime) {
        return (this.getMaxDateTimePolling().getEpochSecond() < currentTime.getEpochSecond());
    }
}
