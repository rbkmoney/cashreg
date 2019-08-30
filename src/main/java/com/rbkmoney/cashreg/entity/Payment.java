package com.rbkmoney.cashreg.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "payment")
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_payment_id_gen"
    )
    @SequenceGenerator(
            name = "seq_payment_id_gen",
            sequenceName = "seq_payment_id",
            allocationSize = 1
    )
    private Long id;

    /**
     * Payment ID
     * e.g. sQoyFthoX2
     */
    @Column(name = "payment_id")
    private String paymentId;

    /**
     * Amount
     * e.g. 10000 (minor units)
     */
    @Column(name = "amountOrig")
    private Long amountOrig;

    /**
     * Partial Amount
     * e.g. 10000 (minor units)
     */
    @Column(name = "partial_amount")
    private Long partialAmount;

    @Column(name = "currency")
    private String currency;

    @ManyToOne
    @JoinColumn(name = "payer_info_id")
    private PayerInfo payerInfo;

    @ManyToOne
    @JoinColumn(name = "refund_id")
    private Refund refund;

    @Column(name = "payment_type")
    private String paymentType;

    /**
     * Status
     * e.g. pending
     */
    @Column(name = "status")
    private String status;

    /**
     * Cart
     * e.g. {"test":"test"}
     */
    @Column(name = "capture_cart")
    private String captureCart;

}
