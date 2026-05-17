package com.bank.cardlimitinvoice.dto.response;

import com.bank.cardlimitinvoice.domain.entity.Invoice;
import com.bank.cardlimitinvoice.domain.entity.InvoiceItem;
import com.bank.cardlimitinvoice.domain.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID cardId,
        String referenceMonth,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        InvoiceStatus status,
        LocalDate dueDate,
        LocalDateTime createdAt,
        List<InvoiceItemResponse> items
) {
    public record InvoiceItemResponse(
            UUID id,
            UUID transactionId,
            BigDecimal amount,
            String description,
            LocalDateTime transactionDate
    ) {
        public static InvoiceItemResponse from(InvoiceItem item) {
            return new InvoiceItemResponse(
                    item.getId(),
                    item.getTransaction().getId(),
                    item.getAmount(),
                    item.getDescription(),
                    item.getTransactionDate()
            );
        }
    }

    public static InvoiceResponse from(Invoice invoice) {
        BigDecimal remaining = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getCard().getId(),
                invoice.getReferenceMonth(),
                invoice.getTotalAmount(),
                invoice.getPaidAmount(),
                remaining,
                invoice.getStatus(),
                invoice.getDueDate(),
                invoice.getCreatedAt(),
                invoice.getItems().stream().map(InvoiceItemResponse::from).toList()
        );
    }
}
