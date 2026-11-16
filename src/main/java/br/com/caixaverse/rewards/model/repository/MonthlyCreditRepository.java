package br.com.caixaverse.rewards.model.repository;

import br.com.caixaverse.rewards.model.entity.MonthlyCredit;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.YearMonth;
import java.util.Optional;

@ApplicationScoped
public class MonthlyCreditRepository implements PanacheRepositoryBase<MonthlyCredit, Long> {
    public Optional<MonthlyCredit> byCompetence(String employeeId, YearMonth competence) {
        return find("employeeId = ?1 and competence = ?2", employeeId, competence).firstResultOptional();
    }
}
