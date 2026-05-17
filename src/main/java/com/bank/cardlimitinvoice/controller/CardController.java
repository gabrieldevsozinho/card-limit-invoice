package com.bank.cardlimitinvoice.controller;

import com.bank.cardlimitinvoice.dto.request.CreateCardRequest;
import com.bank.cardlimitinvoice.dto.response.CardResponse;
import com.bank.cardlimitinvoice.service.CardService;
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
@Tag(name = "Cards", description = "Gerenciamento de cartões")
public class CardController {

    private final CardService cardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar cartão")
    public CardResponse create(@Valid @RequestBody CreateCardRequest request) {
        return cardService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cartão por ID")
    public CardResponse findById(@PathVariable UUID id) {
        return cardService.findById(id);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Listar cartões do cliente")
    public List<CardResponse> findByCustomerId(@PathVariable UUID customerId) {
        return cardService.findByCustomerId(customerId);
    }

    @PatchMapping("/{id}/block")
    @Operation(summary = "Bloquear cartão")
    public CardResponse block(@PathVariable UUID id) {
        return cardService.block(id);
    }

    @PatchMapping("/{id}/unblock")
    @Operation(summary = "Desbloquear cartão")
    public CardResponse unblock(@PathVariable UUID id) {
        return cardService.unblock(id);
    }
}
