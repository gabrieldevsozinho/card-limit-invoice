package com.bank.cardlimitinvoice.controller;

import com.bank.cardlimitinvoice.dto.request.PayInvoiceRequest;
import com.bank.cardlimitinvoice.dto.response.InvoiceResponse;
import com.bank.cardlimitinvoice.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Faturas do cartão")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/cards/{cardId}/invoices/{referenceMonth}")
    @Operation(summary = "Consultar fatura por mês (formato: yyyy-MM)")
    public InvoiceResponse findByCardAndMonth(@PathVariable UUID cardId,
                                              @PathVariable String referenceMonth) {
        return invoiceService.findByCardAndMonth(cardId, referenceMonth);
    }

    @PostMapping("/cards/{cardId}/invoices/{referenceMonth}/generate")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Gerar fatura manualmente para um mês")
    public InvoiceResponse generate(@PathVariable UUID cardId,
                                    @PathVariable String referenceMonth) {
        return invoiceService.generateForMonth(cardId, referenceMonth);
    }

    @PostMapping("/invoices/{invoiceId}/pay")
    @Operation(summary = "Pagar fatura (total ou parcial)")
    public InvoiceResponse pay(@PathVariable UUID invoiceId,
                               @Valid @RequestBody PayInvoiceRequest request) {
        return invoiceService.pay(invoiceId, request);
    }
}
