package com.bank.cardlimitinvoice.service;

import com.bank.cardlimitinvoice.domain.entity.Card;
import com.bank.cardlimitinvoice.domain.entity.Customer;
import com.bank.cardlimitinvoice.domain.enums.CardStatus;
import com.bank.cardlimitinvoice.dto.request.CreateCardRequest;
import com.bank.cardlimitinvoice.dto.response.CardResponse;
import com.bank.cardlimitinvoice.exception.BusinessException;
import com.bank.cardlimitinvoice.exception.ResourceNotFoundException;
import com.bank.cardlimitinvoice.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final CustomerService customerService;
    private final LimitService limitService;

    @Transactional
    public CardResponse create(CreateCardRequest request) {
        Customer customer = customerService.getOrThrow(request.customerId());
        String cardNumber = generateUniqueCardNumber();

        Card card = Card.builder()
                .customer(customer)
                .cardNumber(cardNumber)
                .creditLimit(request.creditLimit())
                .status(CardStatus.ACTIVE)
                .build();

        Card saved = cardRepository.save(card);
        limitService.initializeLimit(saved.getId(), request.creditLimit());

        return CardResponse.from(saved, request.creditLimit());
    }

    @Transactional(readOnly = true)
    public CardResponse findById(UUID id) {
        Card card = getOrThrow(id);
        return CardResponse.from(card, limitService.getAvailableLimit(id));
    }

    @Transactional(readOnly = true)
    public List<CardResponse> findByCustomerId(UUID customerId) {
        return cardRepository.findByCustomerId(customerId).stream()
                .map(card -> CardResponse.from(card, limitService.getAvailableLimit(card.getId())))
                .toList();
    }

    @Transactional
    public CardResponse block(UUID id) {
        Card card = getOrThrow(id);
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new BusinessException("Cartão já está bloqueado");
        }
        card.setStatus(CardStatus.BLOCKED);
        return CardResponse.from(cardRepository.save(card), limitService.getAvailableLimit(id));
    }

    @Transactional
    public CardResponse unblock(UUID id) {
        Card card = getOrThrow(id);
        if (card.getStatus() != CardStatus.BLOCKED) {
            throw new BusinessException("Cartão não está bloqueado");
        }
        card.setStatus(CardStatus.ACTIVE);
        return CardResponse.from(cardRepository.save(card), limitService.getAvailableLimit(id));
    }

    public Card getOrThrow(UUID id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cartão não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<Card> findAllCards() {
        return cardRepository.findAll();
    }

    private String generateUniqueCardNumber() {
        String number;
        do {
            long value = ThreadLocalRandom.current().nextLong(1_000_000_000_000_000L, 9_999_999_999_999_999L);
            number = Long.toString(value);
        } while (cardRepository.existsByCardNumber(number));
        return number;
    }
}
