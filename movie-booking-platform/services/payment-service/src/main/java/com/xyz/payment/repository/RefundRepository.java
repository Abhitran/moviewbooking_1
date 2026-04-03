package com.xyz.payment.repository;

import com.xyz.payment.entity.Refund;
import com.xyz.payment.entity.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {

    List<Refund> findByPaymentId(UUID paymentId);

    Optional<Refund> findByRefundIdAndPaymentId(UUID refundId, UUID paymentId);

    List<Refund> findByStatus(RefundStatus status);
}
