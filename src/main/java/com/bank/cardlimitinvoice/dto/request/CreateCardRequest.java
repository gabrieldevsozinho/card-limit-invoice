package com.bank.cardlimitinvoice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateCardRequest(
        @NotNull UUID customerId,
        @NotNull @DecimalMin(value = "1.00", message = "Limite mínimo é R$ 1,00") BigDecimal creditLimit
) {}
