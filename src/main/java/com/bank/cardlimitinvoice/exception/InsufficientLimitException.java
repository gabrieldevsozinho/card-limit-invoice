package com.bank.cardlimitinvoice.exception;

import java.math.BigDecimal;

public class InsufficientLimitException extends RuntimeException {
    public InsufficientLimitException(BigDecimal requested, BigDecimal available) {
        super("Limite insuficiente. Solicitado: R$ " + requested + ", Disponível: R$ " + available);
    }
}
