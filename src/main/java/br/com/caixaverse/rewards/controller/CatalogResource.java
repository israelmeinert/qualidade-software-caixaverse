package br.com.caixaverse.rewards.controller;

import br.com.caixaverse.rewards.model.service.CatalogService;
import br.com.caixaverse.rewards.view.response.ProductResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/catalog")
@Produces(MediaType.APPLICATION_JSON)
public class CatalogResource {
    private final CatalogService catalog;

    public CatalogResource(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GET
    public List<ProductResponse> list(@QueryParam("page") @DefaultValue("0") @Min(0) @Max(1000000) int page,
                                      @QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size) {
        return catalog.list(page, size);
    }

    @GET
    @Path("/{id}")
    public ProductResponse detail(@PathParam("id") @Positive long id) {
        return catalog.detail(id);
    }
}
