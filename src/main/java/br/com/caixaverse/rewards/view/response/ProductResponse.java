package br.com.caixaverse.rewards.view.response;

import br.com.caixaverse.rewards.model.entity.Product;

public record ProductResponse(long id, String sku, String description, int price, int stock,
                              boolean active, boolean customizable) {
    public static ProductResponse from(Product entity) {
        return new ProductResponse(entity.id,
                entity.sku,
                entity.description,
                entity.price,
                entity.stock,
                entity.active,
                entity.customizable);
    }
}
