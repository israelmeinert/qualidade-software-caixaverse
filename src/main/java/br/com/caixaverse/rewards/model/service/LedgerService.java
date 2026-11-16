package br.com.caixaverse.rewards.model.service;

import br.com.caixaverse.rewards.model.entity.LedgerEntry;
import br.com.caixaverse.rewards.model.entity.MonthlyCredit;
import br.com.caixaverse.rewards.model.entity.Redemption;
import br.com.caixaverse.rewards.model.exception.BusinessException;
import br.com.caixaverse.rewards.model.repository.LedgerEntryRepository;
import br.com.caixaverse.rewards.model.repository.WalletRepository;
import br.com.caixaverse.rewards.view.response.LedgerResponse;
import br.com.caixaverse.rewards.view.response.WalletResponse;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class LedgerService {
    private final WalletRepository wallets;
    private final LedgerEntryRepository entries;

    public LedgerService(WalletRepository wallets, LedgerEntryRepository entries) {
        this.wallets = wallets;
        this.entries = entries;
    }

    public WalletResponse wallet(String employeeId) {
        return wallets.findByIdOptional(employeeId).map(wallet -> new WalletResponse(wallet.balance))
                .orElseThrow(() -> BusinessException.notFound("Carteira não encontrada"));
    }

    public List<LedgerResponse> list(String employeeId, int page, int size) {
        return entries.byEmployee(employeeId, page, size).stream().map(LedgerResponse::from).toList();
    }

    void credit(MonthlyCredit credit) {
        LedgerEntry entry = entry(credit.employeeId, credit.points);
        entry.monthlyCreditId = credit.id;
        entries.persist(entry);
    }

    void debit(Redemption redemption, long cost) {
        LedgerEntry entry = entry(redemption.employeeId, -cost);
        entry.redemptionId = redemption.id;
        entries.persist(entry);
    }

    private static LedgerEntry entry(String employeeId, long points) {
        LedgerEntry entry = new LedgerEntry();
        entry.employeeId = employeeId;
        entry.points = points;
        entry.createdAt = Instant.now();
        return entry;
    }
}
