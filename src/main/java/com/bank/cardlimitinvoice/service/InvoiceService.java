package com.bank.cardlimitinvoice.service;

import com.bank.cardlimitinvoice.domain.entity.*;
import com.bank.cardlimitinvoice.domain.enums.InvoiceStatus;
import com.bank.cardlimitinvoice.domain.enums.TransactionType;
import com.bank.cardlimitinvoice.dto.request.PayInvoiceRequest;
import com.bank.cardlimitinvoice.dto.response.InvoiceResponse;
import com.bank.cardlimitinvoice.exception.BusinessException;
import com.bank.cardlimitinvoice.exception.ResourceNotFoundException;
import com.bank.cardlimitinvoice.repository.CardTransactionRepository;
import com.bank.cardlimitinvoice.repository.InvoiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class InvoiceService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final InvoiceRepository invoiceRepository;
    private final CardTransactionRepository transactionRepository;
    private final CardService cardService;
    private final LimitService limitService;
    private final InvoiceService self;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          CardTransactionRepository transactionRepository,
                          CardService cardService,
                          LimitService limitService,
                          @Lazy InvoiceService self) {
        this.invoiceRepository = invoiceRepository;
        this.transactionRepository = transactionRepository;
        this.cardService = cardService;
        this.limitService = limitService;
        this.self = self;
    }

    @Transactional(readOnly = true)
    public InvoiceResponse findByCardAndMonth(UUID cardId, String referenceMonth) {
        cardService.getOrThrow(cardId);
        Invoice invoice = invoiceRepository.findByCardIdAndReferenceMonth(cardId, referenceMonth)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fatura não encontrada para o cartão " + cardId + " no mês " + referenceMonth));
        return InvoiceResponse.from(invoice);
    }

    @Transactional
    public InvoiceResponse pay(UUID invoiceId, PayInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura não encontrada: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("Fatura já está paga");
        }

        BigDecimal remaining = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
        if (request.amount().compareTo(remaining) > 0) {
            throw new BusinessException("Valor pago (R$ " + request.amount() +
                    ") excede o saldo da fatura (R$ " + remaining + ")");
        }

        invoice.setPaidAmount(invoice.getPaidAmount().add(request.amount()));
        limitService.incrementLimit(invoice.getCard().getId(), request.amount());

        BigDecimal newRemaining = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
        if (newRemaining.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        return InvoiceResponse.from(invoiceRepository.save(invoice));
    }

    @Transactional
    public InvoiceResponse generateForMonth(UUID cardId, String referenceMonth) {
        Card card = cardService.getOrThrow(cardId);

        if (invoiceRepository.findByCardIdAndReferenceMonth(cardId, referenceMonth).isPresent()) {
            throw new BusinessException("Fatura já gerada para " + referenceMonth);
        }

        YearMonth yearMonth = YearMonth.parse(referenceMonth, MONTH_FMT);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<CardTransaction> transactions = transactionRepository
                .findByCardIdAndCreatedAtBetween(cardId, start, end)
                .stream()
                .filter(t -> t.getType() == TransactionType.PURCHASE)
                .toList();

        BigDecimal total = transactions.stream()
                .map(CardTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Invoice invoice = Invoice.builder()
                .card(card)
                .referenceMonth(referenceMonth)
                .totalAmount(total)
                .paidAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.OPEN)
                .dueDate(yearMonth.plusMonths(1).atDay(10))
                .build();

        transactions.forEach(t -> invoice.getItems().add(
                InvoiceItem.builder()
                        .invoice(invoice)
                        .transaction(t)
                        .amount(t.getAmount())
                        .description(t.getDescription())
                        .transactionDate(t.getCreatedAt())
                        .build()
        ));

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Fatura gerada: cardId={}, mês={}, total={}", cardId, referenceMonth, total);
        return InvoiceResponse.from(saved);
    }

    // Chamado pelo scheduler todo dia 1 às 00:01
    @Transactional
    public void generateMonthlyInvoicesForAllCards() {
        String previousMonth = YearMonth.now().minusMonths(1).format(MONTH_FMT);
        log.info("Iniciando geração de faturas mensais para o mês {}", previousMonth);

        cardService.findAllCards().forEach(card -> {
            try {
                self.generateForMonth(card.getId(), previousMonth);
            } catch (BusinessException e) {
                log.warn("Fatura já existente para cardId={}, mês={}", card.getId(), previousMonth);
            }
        });
    }
}
