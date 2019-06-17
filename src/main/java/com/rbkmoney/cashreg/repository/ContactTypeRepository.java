package com.rbkmoney.cashreg.repository;


import com.rbkmoney.cashreg.entity.ContactType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ContactTypeRepository extends JpaRepository<ContactType, Long> {

    ContactType findByType(String type);

}
