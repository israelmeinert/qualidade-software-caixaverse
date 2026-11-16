package br.com.caixaverse.rewards.controller;

import br.com.caixaverse.rewards.model.service.CatalogService;
import br.com.caixaverse.rewards.model.service.CreditService;
import br.com.caixaverse.rewards.view.request.CreditRequest;
import br.com.caixaverse.rewards.view.request.ProductRequest;
import br.com.caixaverse.rewards.view.response.CreditResponse;
import br.com.caixaverse.rewards.view.response.ProductResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.ResponseStatus;

import java.time.YearMonth;

@Path("/admin")
@RolesAllowed("admin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {
    private final CatalogService catalog;
    private final CreditService credits;

    public AdminResource(CatalogService catalog, CreditService credits) {
        this.catalog = catalog;
        this.credits = credits;
    }

    @POST
    @Path("/products")
    @ResponseStatus(201)
    public ProductResponse create(@Valid @NotNull ProductRequest request) {
        return catalog.create(request);
    }

    @PUT
    @Path("/products/{id}")
    public ProductResponse update(@PathParam("id") @Positive long id, @Valid @NotNull ProductRequest request) {
        return catalog.update(id, request);
    }

    @PUT
    @Path("/employees/{employeeId}/credits")
    public CreditResponse credit(@PathParam("employeeId") @Pattern(regexp = "[a-zA-Z0-9_-]{1,80}") String employeeId,
                                 @Valid @NotNull CreditRequest request) {
        return credits.post(employeeId, YearMonth.parse(request.competence()), request.deliveries());
    }
}
