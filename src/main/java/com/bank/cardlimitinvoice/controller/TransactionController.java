package com.bank.cardlimitinvoice.controller;

import com.bank.cardlimitinvoice.dto.request.PurchaseRequest;
import com.bank.cardlimitinvoice.dto.response.TransactionResponse;
import com.bank.cardlimitinvoice.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Compras e estornos")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/purchases")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Realizar compra")
    public TransactionResponse purchase(@Valid @RequestBody PurchaseRequest request) {
        return transactionService.purchase(request.cardId(), request);
    }

    @PostMapping("/transactions/{transactionId}/reverse")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Estornar transação")
    public TransactionResponse reverse(@PathVariable UUID transactionId) {
        return transactionService.reverse(transactionId);
    }

    @GetMapping("/{cardId}/transactions")
    @Operation(summary = "Extrato do cartão")
    public List<TransactionResponse> findByCardId(@PathVariable UUID cardId) {
        return transactionService.findByCardId(cardId);
    }
}
