package br.com.caixaverse.rewards.controller;

import br.com.caixaverse.rewards.model.exception.BusinessException;
import br.com.caixaverse.rewards.model.service.LedgerService;
import br.com.caixaverse.rewards.model.service.RedemptionService;
import br.com.caixaverse.rewards.view.request.RedemptionRequest;
import br.com.caixaverse.rewards.view.response.LedgerResponse;
import br.com.caixaverse.rewards.view.response.RedemptionResponse;
import br.com.caixaverse.rewards.view.response.WalletResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.UUID;

@Path("/me")
@RolesAllowed("employee")
@Produces(MediaType.APPLICATION_JSON)
public class EmployeeResource {
    private final JsonWebToken jwt;
    private final LedgerService ledger;
    private final RedemptionService orders;

    public EmployeeResource(JsonWebToken jwt, LedgerService ledger, RedemptionService orders) {
        this.jwt = jwt;
        this.ledger = ledger;
        this.orders = orders;
    }

    @GET
    @Path("/wallet")
    public WalletResponse wallet() {
        return ledger.wallet(employeeId());
    }

    @GET
    @Path("/ledger")
    public List<LedgerResponse> ledger(@QueryParam("page") @DefaultValue("0") @Min(0) @Max(1000000) int page,
                                      @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size) {
        return ledger.list(employeeId(), page, size);
    }

    @POST
    @Path("/redemptions")
    @Consumes(MediaType.APPLICATION_JSON)
    public RedemptionResponse redeem(
            @HeaderParam("Idempotency-Key") @NotNull @Pattern(regexp = "[a-zA-Z0-9_-]{1,80}") String key,
            @Valid @NotNull RedemptionRequest request) {
        return orders.redeem(employeeId(), key, request);
    }

    @GET
    @Path("/redemptions/{id}")
    public RedemptionResponse order(@PathParam("id") UUID id) {
        return orders.detail(employeeId(), id);
    }

    private String employeeId() {
        String subject = jwt.getSubject();
        if (subject == null || !subject.matches("[a-zA-Z0-9_-]{1,80}")) {
            throw BusinessException.invalid("Sub do colaborador inválido");
        }
        return subject;
    }
}
