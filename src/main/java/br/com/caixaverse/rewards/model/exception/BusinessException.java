package br.com.caixaverse.rewards.model.exception;

public class BusinessException extends RuntimeException {
    private final int status;
    private final String code;

    private BusinessException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static BusinessException invalid(String message) {
        return new BusinessException(400, "invalid_request", message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(404, "not_found", message);
    }

    public static BusinessException conflict(String code, String message) {
        return new BusinessException(409, code, message);
    }

    public int status() { return status; }
    public String code() { return code; }
}
