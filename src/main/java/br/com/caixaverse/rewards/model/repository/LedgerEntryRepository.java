package br.com.caixaverse.rewards.model.repository;

import br.com.caixaverse.rewards.model.entity.LedgerEntry;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class LedgerEntryRepository implements PanacheRepositoryBase<LedgerEntry, Long> {
    public List<LedgerEntry> byEmployee(String employeeId, int page, int size) {
        return find("employeeId", Sort.descending("id"), employeeId).page(Page.of(page, size)).list();
    }
}
