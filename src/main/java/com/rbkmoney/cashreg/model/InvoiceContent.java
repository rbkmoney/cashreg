package com.rbkmoney.cashreg.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceContent {

    private String type;
    private byte[] data;

    public InvoiceContent(InvoiceContent other) {
        this.type = other.type;
        this.data = other.data;
    }

}
