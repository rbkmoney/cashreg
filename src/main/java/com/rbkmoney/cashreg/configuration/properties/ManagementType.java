package com.rbkmoney.cashreg.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties("change.path")
public class ManagementType {

    @NotEmpty
    private String created;

    @NotEmpty
    private String statusChanged;

    @NotEmpty
    private String sessionPayloadStarted;

    @NotEmpty
    private String sessionPayloadFinished;

    @NotEmpty
    private String sessionAdapterStateChanged;

    @NotEmpty
    private String sessionPayloadFinishedResultFailed;

    @NotEmpty
    private String sessionPayloadFinishedResultSucceeded;

    @NotEmpty
    private String statusChangedStatusFailed;

    @NotEmpty
    private String statusChangedStatusDelivered;

    @NotEmpty
    private String statusChangedStatusPending;

}
