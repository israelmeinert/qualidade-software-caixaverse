package br.com.caixaverse.rewards.model.service;

import br.com.caixaverse.rewards.model.entity.MonthlyCredit;
import br.com.caixaverse.rewards.model.entity.Wallet;
import br.com.caixaverse.rewards.model.exception.BusinessException;
import br.com.caixaverse.rewards.model.policy.MonthlyCreditPolicy;
import br.com.caixaverse.rewards.model.repository.MonthlyCreditRepository;
import br.com.caixaverse.rewards.model.repository.WalletRepository;
import br.com.caixaverse.rewards.view.response.CreditResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.YearMonth;
import java.util.Optional;

@ApplicationScoped
public class CreditService {
    private final WalletRepository wallets;
    private final MonthlyCreditRepository credits;
    private final LedgerService ledger;
    private final MonthlyCreditPolicy policy = new MonthlyCreditPolicy();

    public CreditService(WalletRepository wallets, MonthlyCreditRepository credits, LedgerService ledger) {
        this.wallets = wallets;
        this.credits = credits;
        this.ledger = ledger;
    }

    @Transactional
    public CreditResponse post(String employeeId, YearMonth competence, int deliveries) {
        int points = policy.calculate(deliveries);
        wallets.limitLockWait();
        wallets.ensureExists(employeeId);
        Wallet wallet = wallets.lock(employeeId).orElseThrow();

        Optional<MonthlyCredit> existing = credits.byCompetence(employeeId, competence);
        if (existing.isPresent()) {
            return replay(existing.get(), deliveries);
        }

        MonthlyCredit credit = new MonthlyCredit();
        credit.employeeId = employeeId;
        credit.competence = competence;
        credit.deliveries = deliveries;
        credit.points = points;
        wallet.balance = Math.addExact(wallet.balance, points);
        credits.persist(credit);
        ledger.credit(credit);
        return CreditResponse.from(credit);
    }

    private static CreditResponse replay(MonthlyCredit existing, int deliveries) {
        if (existing.deliveries != deliveries) {
            throw BusinessException.conflict("credit_already_posted", "A competência possui outro lançamento");
        }
        return CreditResponse.from(existing);
    }
}
