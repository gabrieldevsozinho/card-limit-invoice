package com.bank.cardlimitinvoice.dto.response;

import com.bank.cardlimitinvoice.domain.entity.CardTransaction;
import com.bank.cardlimitinvoice.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID cardId,
        BigDecimal amount,
        TransactionType type,
        String description,
        LocalDateTime createdAt
) {
    public static TransactionResponse from(CardTransaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getCard().getId(),
                t.getAmount(),
                t.getType(),
                t.getDescription(),
                t.getCreatedAt()
        );
    }
}
