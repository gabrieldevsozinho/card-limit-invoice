package com.bank.cardlimitinvoice.repository;

import com.bank.cardlimitinvoice.domain.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
    List<Card> findByCustomerId(UUID customerId);
    boolean existsByCardNumber(String cardNumber);
}
