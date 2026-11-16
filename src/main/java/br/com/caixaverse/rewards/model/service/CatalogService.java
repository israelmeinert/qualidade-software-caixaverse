package br.com.caixaverse.rewards.model.service;

import br.com.caixaverse.rewards.model.entity.Product;
import br.com.caixaverse.rewards.model.exception.BusinessException;
import br.com.caixaverse.rewards.model.repository.ProductRepository;
import br.com.caixaverse.rewards.model.repository.PostgresLocks;
import br.com.caixaverse.rewards.view.request.ProductRequest;
import br.com.caixaverse.rewards.view.response.ProductResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class CatalogService {
    private final ProductRepository products;
    private final PostgresLocks locks;

    public CatalogService(ProductRepository products, PostgresLocks locks) {
        this.products = products;
        this.locks = locks;
    }

    public List<ProductResponse> list(int page, int size) {
        return products.activePage(page, size).stream().map(ProductResponse::from).toList();
    }

    public ProductResponse detail(long id) {
        return products.findByIdOptional(id).filter(product -> product.active)
                .map(ProductResponse::from).orElseThrow(CatalogService::missingProduct);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        locks.setTimeoutForCurrentTransaction();
        Product product = new Product();
        apply(product, request);
        products.persist(product);
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse update(long id, ProductRequest request) {
        locks.setTimeoutForCurrentTransaction();
        Product product = products.lock(id).orElseThrow(CatalogService::missingProduct);
        apply(product, request);
        return ProductResponse.from(product);
    }

    private static void apply(Product product, ProductRequest request) {
        product.sku = request.sku();
        product.description = request.description();
        product.price = request.price();
        product.stock = request.stock();
        product.active = request.active();
        product.customizable = request.customizable();
    }

    private static BusinessException missingProduct() {
        return BusinessException.notFound("Produto não encontrado");
    }
}
