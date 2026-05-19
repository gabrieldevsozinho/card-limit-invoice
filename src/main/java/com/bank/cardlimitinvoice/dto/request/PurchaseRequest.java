package com.bank.cardlimitinvoice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseRequest(
        @NotNull UUID cardId,
        @NotNull @DecimalMin(value = "0.01", message = "Valor mínimo é R$ 0,01") BigDecimal amount,
        @NotBlank String description
) {}
