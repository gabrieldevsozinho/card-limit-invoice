package com.bank.cardlimitinvoice.service;

import com.bank.cardlimitinvoice.domain.entity.Card;
import com.bank.cardlimitinvoice.domain.entity.CardTransaction;
import com.bank.cardlimitinvoice.domain.entity.Customer;
import com.bank.cardlimitinvoice.domain.enums.CardStatus;
import com.bank.cardlimitinvoice.domain.enums.TransactionType;
import com.bank.cardlimitinvoice.dto.request.PurchaseRequest;
import com.bank.cardlimitinvoice.dto.response.TransactionResponse;
import com.bank.cardlimitinvoice.exception.BusinessException;
import com.bank.cardlimitinvoice.exception.InsufficientLimitException;
import com.bank.cardlimitinvoice.repository.CardTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private CardTransactionRepository transactionRepository;
    @Mock
    private CardService cardService;
    @Mock
    private LimitService limitService;

    @InjectMocks
    private TransactionService transactionService;

    private Card activeCard;
    private UUID cardId;

    @BeforeEach
    void setUp() {
        cardId = UUID.randomUUID();
        Customer customer = Customer.builder().id(UUID.randomUUID()).name("João").build();
        activeCard = Card.builder()
                .id(cardId)
                .customer(customer)
                .cardNumber("1234567890123456")
                .creditLimit(new BigDecimal("1000.00"))
                .status(CardStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Deve realizar compra com sucesso quando há limite suficiente")
    void purchase_success() {
        PurchaseRequest request = new PurchaseRequest(cardId, new BigDecimal("200.00"), "Supermercado");

        when(cardService.getOrThrow(cardId)).thenReturn(activeCard);
        when(limitService.getAvailableLimit(cardId)).thenReturn(new BigDecimal("1000.00"));

        CardTransaction saved = CardTransaction.builder()
                .id(UUID.randomUUID())
                .card(activeCard)
                .amount(request.amount())
                .type(TransactionType.PURCHASE)
                .description(request.description())
                .createdAt(LocalDateTime.now())
                .build();
        when(transactionRepository.save(any())).thenReturn(saved);

        TransactionResponse response = transactionService.purchase(cardId, request);

        assertThat(response.amount()).isEqualByComparingTo("200.00");
        assertThat(response.type()).isEqualTo(TransactionType.PURCHASE);
        verify(limitService).decrementLimit(cardId, request.amount());
    }

    @Test
    @DisplayName("Deve lançar InsufficientLimitException quando limite é insuficiente")
    void purchase_insufficientLimit() {
        PurchaseRequest request = new PurchaseRequest(cardId, new BigDecimal("500.00"), "Eletrônicos");

        when(cardService.getOrThrow(cardId)).thenReturn(activeCard);
        when(limitService.getAvailableLimit(cardId)).thenReturn(new BigDecimal("100.00"));

        assertThatThrownBy(() -> transactionService.purchase(cardId, request))
                .isInstanceOf(InsufficientLimitException.class)
                .hasMessageContaining("Limite insuficiente");

        verify(limitService, never()).decrementLimit(any(), any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar comprar com cartão bloqueado")
    void purchase_blockedCard() {
        activeCard.setStatus(CardStatus.BLOCKED);
        PurchaseRequest request = new PurchaseRequest(cardId, new BigDecimal("100.00"), "Loja");

        when(cardService.getOrThrow(cardId)).thenReturn(activeCard);

        assertThatThrownBy(() -> transactionService.purchase(cardId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não está ativo");
    }

    @Test
    @DisplayName("Deve estornar transação e devolver limite")
    void reverse_success() {
        UUID transactionId = UUID.randomUUID();
        CardTransaction original = CardTransaction.builder()
                .id(transactionId)
                .card(activeCard)
                .amount(new BigDecimal("300.00"))
                .type(TransactionType.PURCHASE)
                .description("Restaurante")
                .createdAt(LocalDateTime.now())
                .build();

        CardTransaction reversal = CardTransaction.builder()
                .id(UUID.randomUUID())
                .card(activeCard)
                .amount(new BigDecimal("300.00"))
                .type(TransactionType.REVERSAL)
                .description("Estorno: Restaurante")
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(original));
        when(transactionRepository.save(any())).thenReturn(reversal);

        TransactionResponse response = transactionService.reverse(transactionId);

        assertThat(response.type()).isEqualTo(TransactionType.REVERSAL);
        verify(limitService).incrementLimit(cardId, new BigDecimal("300.00"));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar estornar um estorno")
    void reverse_ofReversal_throwsException() {
        UUID transactionId = UUID.randomUUID();
        CardTransaction reversal = CardTransaction.builder()
                .id(transactionId)
                .card(activeCard)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.REVERSAL)
                .description("Estorno anterior")
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(reversal));

        assertThatThrownBy(() -> transactionService.reverse(transactionId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Não é possível estornar um estorno");
    }
}
