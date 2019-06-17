package com.rbkmoney.cashreg.repository;


import com.rbkmoney.cashreg.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

}
