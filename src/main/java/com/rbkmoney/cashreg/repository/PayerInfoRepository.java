package com.rbkmoney.cashreg.repository;


import com.rbkmoney.cashreg.entity.PayerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PayerInfoRepository extends JpaRepository<PayerInfo, Long> {

    PayerInfo findByContact(String contact);

}
