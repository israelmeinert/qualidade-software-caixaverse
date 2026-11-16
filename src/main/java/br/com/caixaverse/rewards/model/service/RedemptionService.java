package br.com.caixaverse.rewards.model.service;

import br.com.caixaverse.rewards.model.entity.Product;
import br.com.caixaverse.rewards.model.entity.Redemption;
import br.com.caixaverse.rewards.model.entity.Wallet;
import br.com.caixaverse.rewards.model.exception.BusinessException;
import br.com.caixaverse.rewards.model.repository.ProductRepository;
import br.com.caixaverse.rewards.model.repository.RedemptionRepository;
import br.com.caixaverse.rewards.model.repository.WalletRepository;
import br.com.caixaverse.rewards.view.request.RedemptionRequest;
import br.com.caixaverse.rewards.view.response.RedemptionResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RedemptionService {
    private final WalletRepository wallets;
    private final ProductRepository products;
    private final RedemptionRepository orders;
    private final LedgerService ledger;

    public RedemptionService(WalletRepository wallets, ProductRepository products,
                             RedemptionRepository orders, LedgerService ledger) {
        this.wallets = wallets;
        this.products = products;
        this.orders = orders;
        this.ledger = ledger;
    }

    @Transactional
    public RedemptionResponse redeem(String employeeId, String key, RedemptionRequest request) {
        wallets.limitLockWait();
        Wallet wallet = wallets.lock(employeeId)
                .orElseThrow(() -> BusinessException.notFound("Carteira não encontrada"));
        Optional<Redemption> existing = orders.byKey(employeeId, key);
        if (existing.isPresent()) {
            return replay(existing.get(), request);
        }

        Product product = products.lock(request.productId()).filter(item -> item.active)
                .orElseThrow(() -> BusinessException.notFound("Produto ativo não encontrado"));
        long cost = (long) product.price * request.quantity();
        validate(request, product, wallet, cost);

        Redemption order = newOrder(employeeId, key, request, product);
        wallet.balance -= cost;
        product.stock -= order.quantity;
        orders.persist(order);
        ledger.debit(order, cost);
        return RedemptionResponse.from(order);
    }

    public RedemptionResponse detail(String employeeId, UUID id) {
        return orders.owned(id, employeeId).map(RedemptionResponse::from)
                .orElseThrow(() -> BusinessException.notFound("Pedido não encontrado"));
    }

    private static RedemptionResponse replay(Redemption existing, RedemptionRequest request) {
        if (existing.productId != request.productId() || existing.quantity != request.quantity()
                || !Objects.equals(existing.personalization, request.personalization())) {
            throw BusinessException.conflict("idempotency_conflict", "A chave já identifica outro pedido");
        }
        return RedemptionResponse.from(existing);
    }

    private static void validate(RedemptionRequest request, Product product, Wallet wallet, long cost) {
        if (request.personalization() != null && !product.customizable) {
            throw BusinessException.invalid("O produto não aceita personalização");
        }
        if (product.stock < request.quantity()) {
            throw BusinessException.conflict("insufficient_stock", "Estoque insuficiente");
        }
        if (wallet.balance < cost) {
            throw BusinessException.conflict("insufficient_balance", "Saldo insuficiente");
        }
    }

    private static Redemption newOrder(String employeeId, String key, RedemptionRequest request, Product product) {
        Redemption order = new Redemption();
        order.id = UUID.randomUUID();
        order.employeeId = employeeId;
        order.idempotencyKey = key;
        order.productId = product.id;
        order.quantity = request.quantity();
        order.personalization = request.personalization();
        order.unitPrice = product.price;
        order.state = Redemption.State.CONFIRMED;
        return order;
    }
}
