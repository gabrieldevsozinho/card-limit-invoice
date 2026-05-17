package com.bank.cardlimitinvoice.repository;

import com.bank.cardlimitinvoice.domain.entity.CardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CardTransactionRepository extends JpaRepository<CardTransaction, UUID> {
    List<CardTransaction> findByCardIdOrderByCreatedAtDesc(UUID cardId);
    List<CardTransaction> findByCardIdAndCreatedAtBetween(UUID cardId, LocalDateTime start, LocalDateTime end);
}
