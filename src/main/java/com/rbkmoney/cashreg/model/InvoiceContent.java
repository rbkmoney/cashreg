package com.rbkmoney.cashreg.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceContent {

    /**
     * Type
     */
    private String type;

    /**
     * Data
     */
    private byte[] data;


    public InvoiceContent(InvoiceContent other) {
        this.type = other.type;
        this.data = other.data;
    }

}
