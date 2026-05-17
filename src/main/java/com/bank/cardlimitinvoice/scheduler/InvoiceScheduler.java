package com.bank.cardlimitinvoice.scheduler;

import com.bank.cardlimitinvoice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceScheduler {

    private final InvoiceService invoiceService;

    // Todo dia 1 de cada mês às 00:01
    @Scheduled(cron = "0 1 0 1 * *")
    public void generateMonthlyInvoices() {
        log.info("Scheduler: iniciando geração de faturas mensais");
        invoiceService.generateMonthlyInvoicesForAllCards();
        log.info("Scheduler: geração de faturas mensais concluída");
    }
}
