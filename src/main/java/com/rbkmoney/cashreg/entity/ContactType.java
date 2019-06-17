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
@Entity(name = "contact_type")
@Table(name = "contact_type")
public class ContactType {

    public static final String EMAIL = "email";
    public static final String PHONE_NUMBER = "phone_number";

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_contact_type_id_gen"
    )
    @SequenceGenerator(
            name = "seq_contact_type_id_gen",
            sequenceName = "seq_contact_type_id",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "type")
    private String type;

    public ContactType(String type) {
        this.type = type;
    }

}
