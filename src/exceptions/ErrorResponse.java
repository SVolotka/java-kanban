package exceptions;

public class ErrorResponse {
    private final String message;
    private final Integer errorCode;
    private final String url;

    public ErrorResponse(String message, Integer errorCode, String url) {
        this.message = message;
        this.errorCode = errorCode;
        this.url = url;
    }
}
