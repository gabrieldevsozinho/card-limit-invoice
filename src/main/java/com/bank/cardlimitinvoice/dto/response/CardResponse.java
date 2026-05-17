package com.bank.cardlimitinvoice.dto.response;

import com.bank.cardlimitinvoice.domain.entity.Card;
import com.bank.cardlimitinvoice.domain.enums.CardStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CardResponse(
        UUID id,
        UUID customerId,
        String cardNumber,
        BigDecimal creditLimit,
        BigDecimal availableLimit,
        CardStatus status
) {
    public static CardResponse from(Card card, BigDecimal availableLimit) {
        return new CardResponse(
                card.getId(),
                card.getCustomer().getId(),
                card.getCardNumber(),
                card.getCreditLimit(),
                availableLimit,
                card.getStatus()
        );
    }
}
