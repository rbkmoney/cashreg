package com.rbkmoney.cashreg.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity(name = "payer_info")
@Table(name = "payer_info")
public class PayerInfo {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_payer_info_id_gen"
    )
    @SequenceGenerator(
            name = "seq_payer_info_id_gen",
            sequenceName = "seq_payer_info_id",
            allocationSize = 1
    )
    private Long id;

    /**
     * Contact: e-mail
     * e.g. test@test.com
     */
    @Column(name = "contact")
    private String contact;

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    private ContactType contactType;

    public PayerInfo(String contact, ContactType contactType) {
        this.contact = contact;
        this.contactType = contactType;
    }

}
