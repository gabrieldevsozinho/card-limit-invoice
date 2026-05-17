package com.bank.cardlimitinvoice.service;

import com.bank.cardlimitinvoice.domain.entity.Card;
import com.bank.cardlimitinvoice.domain.entity.Customer;
import com.bank.cardlimitinvoice.domain.entity.Invoice;
import com.bank.cardlimitinvoice.domain.enums.CardStatus;
import com.bank.cardlimitinvoice.domain.enums.InvoiceStatus;
import com.bank.cardlimitinvoice.dto.request.PayInvoiceRequest;
import com.bank.cardlimitinvoice.dto.response.InvoiceResponse;
import com.bank.cardlimitinvoice.exception.BusinessException;
import com.bank.cardlimitinvoice.repository.CardTransactionRepository;
import com.bank.cardlimitinvoice.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private CardTransactionRepository transactionRepository;
    @Mock
    private CardService cardService;
    @Mock
    private LimitService limitService;

    @InjectMocks
    private InvoiceService invoiceService;

    private Card card;
    private UUID cardId;

    @BeforeEach
    void setUp() {
        cardId = UUID.randomUUID();
        Customer customer = Customer.builder().id(UUID.randomUUID()).name("Pedro").build();
        card = Card.builder()
                .id(cardId)
                .customer(customer)
                .cardNumber("9876543210987654")
                .creditLimit(new BigDecimal("2000.00"))
                .status(CardStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Deve pagar fatura totalmente e atualizar status para PAID")
    void pay_fullPayment() {
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = Invoice.builder()
                .id(invoiceId)
                .card(card)
                .referenceMonth("2025-04")
                .totalAmount(new BigDecimal("500.00"))
                .paidAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.OPEN)
                .dueDate(LocalDate.of(2025, 5, 10))
                .createdAt(LocalDateTime.now())
                .build();

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any())).thenReturn(invoice);

        InvoiceResponse response = invoiceService.pay(invoiceId, new PayInvoiceRequest(new BigDecimal("500.00")));

        assertThat(response.status()).isEqualTo(InvoiceStatus.PAID);
        assertThat(response.paidAmount()).isEqualByComparingTo("500.00");
        verify(limitService).incrementLimit(cardId, new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("Deve pagar fatura parcialmente e atualizar status para PARTIALLY_PAID")
    void pay_partialPayment() {
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = Invoice.builder()
                .id(invoiceId)
                .card(card)
                .referenceMonth("2025-04")
                .totalAmount(new BigDecimal("500.00"))
                .paidAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.OPEN)
                .dueDate(LocalDate.of(2025, 5, 10))
                .createdAt(LocalDateTime.now())
                .build();

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any())).thenReturn(invoice);

        invoiceService.pay(invoiceId, new PayInvoiceRequest(new BigDecimal("200.00")));

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
        verify(limitService).incrementLimit(cardId, new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar pagar fatura já paga")
    void pay_alreadyPaid() {
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = Invoice.builder()
                .id(invoiceId)
                .card(card)
                .referenceMonth("2025-03")
                .totalAmount(new BigDecimal("300.00"))
                .paidAmount(new BigDecimal("300.00"))
                .status(InvoiceStatus.PAID)
                .dueDate(LocalDate.of(2025, 4, 10))
                .createdAt(LocalDateTime.now())
                .build();

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> invoiceService.pay(invoiceId, new PayInvoiceRequest(new BigDecimal("100.00"))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Fatura já está paga");
    }

    @Test
    @DisplayName("Deve gerar fatura mensal com lançamentos do período")
    void generateForMonth_success() {
        String referenceMonth = "2025-04";
        when(cardService.getOrThrow(cardId)).thenReturn(card);
        when(invoiceRepository.findByCardIdAndReferenceMonth(cardId, referenceMonth)).thenReturn(Optional.empty());
        when(transactionRepository.findByCardIdAndCreatedAtBetween(any(), any(), any())).thenReturn(List.of());

        Invoice invoice = Invoice.builder()
                .id(UUID.randomUUID())
                .card(card)
                .referenceMonth(referenceMonth)
                .totalAmount(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.OPEN)
                .dueDate(LocalDate.of(2025, 5, 10))
                .createdAt(LocalDateTime.now())
                .build();

        when(invoiceRepository.save(any())).thenReturn(invoice);

        InvoiceResponse response = invoiceService.generateForMonth(cardId, referenceMonth);

        assertThat(response.referenceMonth()).isEqualTo(referenceMonth);
        assertThat(response.status()).isEqualTo(InvoiceStatus.OPEN);
    }
}
