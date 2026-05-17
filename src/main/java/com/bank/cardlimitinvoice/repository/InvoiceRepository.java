package com.bank.cardlimitinvoice.repository;

import com.bank.cardlimitinvoice.domain.entity.Invoice;
import com.bank.cardlimitinvoice.domain.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByCardIdAndReferenceMonth(UUID cardId, String referenceMonth);
    List<Invoice> findByCardIdOrderByReferenceMonthDesc(UUID cardId);
    List<Invoice> findByStatus(InvoiceStatus status);
}
