package com.rbkmoney.cashreg.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;


@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metadata {

    @JsonProperty("cms")
    private String cms;

    @JsonProperty("version")
    private String version;

    @JsonProperty("items")
    private List<Items> items;

}
