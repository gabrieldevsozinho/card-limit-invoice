package com.bank.cardlimitinvoice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PayInvoiceRequest(
        @NotNull @DecimalMin(value = "0.01", message = "Valor mínimo é R$ 0,01") BigDecimal amount
) {}
