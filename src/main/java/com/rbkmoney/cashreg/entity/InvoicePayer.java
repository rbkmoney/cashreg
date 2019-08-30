package com.rbkmoney.cashreg.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@Entity(name = "invoice_payer")
@Table(name = "invoice_payer")
public class InvoicePayer {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_invoice_payer_id_gen"
    )
    @SequenceGenerator(
            name = "seq_invoice_payer_id_gen",
            sequenceName = "seq_invoice_payer_id",
            allocationSize = 1
    )
    private Long id;

    /**
     * Invoice ID
     * e.g. sQoyFthoX2
     */
    @Column(name = "invoice_id")
    private String invoiceId;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne
    @JoinColumn(name = "currency")
    private String currency;

    /**
     * Amount
     * e.g. 10000 (minor units)
     */
    @Column(name = "amount")
    private Long amount;

    /**
     * Metadata
     * e.g. {"test":"test"}
     */
    @Column(name = "metadata")
    private String metadata;

    /**
     * Cart
     * e.g. {"test":"test"}
     */
    @Column(name = "cart")
    private String cart;

    @Column(name = "exchange_cart")
    private String exchangeCart;

}
