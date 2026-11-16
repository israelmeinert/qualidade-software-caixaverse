package br.com.caixaverse.rewards.configuration;

import br.com.caixaverse.rewards.model.exception.BusinessException;
import br.com.caixaverse.rewards.view.response.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.sql.SQLException;

@Provider
public class ApiErrors implements ExceptionMapper<Exception>, ContainerResponseFilter {
    private static final Logger LOG = Logger.getLogger(ApiErrors.class);

    private final Correlation correlation;

    public ApiErrors(Correlation correlation) {
        this.correlation = correlation;
    }

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof BusinessException business) {
            return response(business.status(), business.code(), business.getMessage());
        }
        if (exception instanceof WebApplicationException web) {
            return web.getResponse();
        }
        String sqlState = sqlState(exception);
        return switch (sqlState) {
            case "55P03", "40P01", "40001" -> response(409, "concurrent_update",
                    "Operação concorrente. Repita a solicitação com a mesma chave");
            case "23505" -> response(409, "duplicate_resource", "Já existe um registro com esta chave");
            default -> unexpected(exception);
        };
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        response.getHeaders().putSingle("X-Correlation-ID", correlation.id());
        if (response.getStatus() >= 400 && !(response.getEntity() instanceof ErrorResponse)) {
            ErrorResponse error = switch (response.getStatus()) {
                case 400 -> error("invalid_request", "Dados da solicitação inválidos");
                case 401 -> error("unauthenticated", "Autenticação necessária");
                case 403 -> error("forbidden", "Acesso negado");
                case 404 -> error("not_found", "Recurso não encontrado");
                default -> error("http_error", "A solicitação não pôde ser atendida");
            };
            response.setEntity(error, null, MediaType.APPLICATION_JSON_TYPE);
        }
    }

    private Response unexpected(Exception exception) {
        LOG.errorf("Falha na API. correlationId=%s exception=%s", correlation.id(), exception.getClass().getName());
        return response(500, "internal_error", "Erro interno");
    }

    private Response response(int status, String code, String message) {
        return Response.status(status).type(MediaType.APPLICATION_JSON_TYPE).entity(error(code, message)).build();
    }

    private ErrorResponse error(String code, String message) {
        return new ErrorResponse(code, message, correlation.id());
    }

    private static String sqlState(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && sql.getSQLState() != null) {
                return sql.getSQLState();
            }
        }
        return "";
    }
}
