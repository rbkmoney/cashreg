package com.rbkmoney.cashreg.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Invoice {

    /**
     * ID
     */
    private String id;

    /**
     * Shop ID
     */
    private int shopID;

    /**
     * Created At
     */
    private String createdAt;

    /**
     * Status
     */
    private String status;

    /**
     * Reason
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String reason;

    /**
     * DueDate
     */
    private String dueDate;

    /**
     * Amount
     */
    private long amount;

    /**
     * Currency
     */
    private String currency;

    /**
     * see {@link InvoiceContent}
     */
    @JsonSerialize(using = MetadataSerializer.class)
    private InvoiceContent metadata;

    /**
     * Product
     */
    private String product;

    /**
     * Description
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;


    @JsonProperty("cart")
    private List<InvoiceLine> cart;

    public Invoice(Invoice other) {
        this.id = other.id;
        this.shopID = other.shopID;
        this.createdAt = other.createdAt;
        this.status = other.status;
        this.reason = other.reason;
        this.dueDate = other.dueDate;
        this.amount = other.amount;
        this.currency = other.currency;
        if (other.metadata != null) {
            this.metadata = new InvoiceContent(other.metadata);
        }
        this.product = other.product;
        this.description = other.description;
        this.cart = other.cart;
    }

}
