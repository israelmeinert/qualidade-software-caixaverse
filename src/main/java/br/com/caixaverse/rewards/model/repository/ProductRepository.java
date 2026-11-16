package br.com.caixaverse.rewards.model.repository;

import br.com.caixaverse.rewards.model.entity.Product;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductRepository implements PanacheRepositoryBase<Product, Long> {
    public Optional<Product> lock(long id) {
        return Optional.ofNullable(findById(id, LockModeType.PESSIMISTIC_WRITE));
    }

    public List<Product> activePage(int page, int size) {
        return find("active = true", Sort.by("id")).page(Page.of(page, size)).list();
    }
}
