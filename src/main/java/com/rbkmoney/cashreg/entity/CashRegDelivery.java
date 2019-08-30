package com.rbkmoney.cashreg.entity;

import lombok.*;

import javax.persistence.*;

@Data
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "cashreg_delivery")
@Table(name = "cashreg_delivery")
public class CashRegDelivery {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_cashreg_delivery_id_gen"
    )
    @SequenceGenerator(
            name = "seq_cashreg_delivery_id_gen",
            sequenceName = "seq_cashreg_delivery_id",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private InvoicePayer invoiceId;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    private Payment paymentId;

    @ManyToOne
    @JoinColumn(name = "refund_id")
    private Refund refundId;

    @Column(name = "type_operation")
    private String typeOperation;

    @Column(name = "request_id")
    private String requestId;

    /**
     * @see com.rbkmoney.cashreg.utils.constant.CartState
     */
    @Column(name = "cart_state")
    private String cartState;

    /**
     * @see com.rbkmoney.cashreg.utils.constant.CashRegStatus
     */
    @Column(name = "cashreg_status")
    private String cashregStatus;

    @Column(name = "cashreg_response")
    private String cashregResponse;

    @Column(name = "cashreg_uuid")
    private String cashregUuid;

    public CashRegDelivery(Long id) {
        this.id = id;
    }

}
