package com.rbkmoney.cashreg.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;
import java.util.Map;

@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvoiceLine {

    @JsonProperty("product")
    private String product;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("price")
    private BigInteger price;

    @JsonProperty("metadata")
    private Map<String, String> metadata;

}
