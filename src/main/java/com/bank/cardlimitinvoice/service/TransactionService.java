package com.bank.cardlimitinvoice.service;

import com.bank.cardlimitinvoice.domain.entity.Card;
import com.bank.cardlimitinvoice.domain.entity.CardTransaction;
import com.bank.cardlimitinvoice.domain.enums.CardStatus;
import com.bank.cardlimitinvoice.domain.enums.TransactionType;
import com.bank.cardlimitinvoice.dto.request.PurchaseRequest;
import com.bank.cardlimitinvoice.dto.response.TransactionResponse;
import com.bank.cardlimitinvoice.exception.BusinessException;
import com.bank.cardlimitinvoice.exception.InsufficientLimitException;
import com.bank.cardlimitinvoice.exception.ResourceNotFoundException;
import com.bank.cardlimitinvoice.repository.CardTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final CardTransactionRepository transactionRepository;
    private final CardService cardService;
    private final LimitService limitService;

    @Transactional
    public TransactionResponse purchase(UUID cardId, PurchaseRequest request) {
        Card card = cardService.getOrThrow(cardId);

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new BusinessException("Cartão não está ativo. Status: " + card.getStatus());
        }

        BigDecimal available = limitService.getAvailableLimit(cardId);
        if (request.amount().compareTo(available) > 0) {
            throw new InsufficientLimitException(request.amount(), available);
        }

        limitService.decrementLimit(cardId, request.amount());

        CardTransaction transaction = CardTransaction.builder()
                .card(card)
                .amount(request.amount())
                .type(TransactionType.PURCHASE)
                .description(request.description())
                .build();

        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse reverse(UUID transactionId) {
        CardTransaction original = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada: " + transactionId));

        if (original.getType() == TransactionType.REVERSAL) {
            throw new BusinessException("Não é possível estornar um estorno");
        }

        limitService.incrementLimit(original.getCard().getId(), original.getAmount());

        CardTransaction reversal = CardTransaction.builder()
                .card(original.getCard())
                .amount(original.getAmount())
                .type(TransactionType.REVERSAL)
                .description("Estorno: " + original.getDescription())
                .build();

        return TransactionResponse.from(transactionRepository.save(reversal));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findByCardId(UUID cardId) {
        cardService.getOrThrow(cardId);
        return transactionRepository.findByCardIdOrderByCreatedAtDesc(cardId)
                .stream().map(TransactionResponse::from).toList();
    }
}
