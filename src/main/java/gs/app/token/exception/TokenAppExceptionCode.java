package gs.app.token.exception;

public enum TokenAppExceptionCode {

    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_FOUND(404, "Not Found"),
    BAD_REQUEST(400, "Bas Request"),
    NO_TOKEN_SLOT_AVAILABLE(400, "No token slot available"),
    FIELD_CONFIGURATION_LOAD_FAILED(500, "Failed to load fields configuration"),
    CLIENT_CONFIGURATION_LOAD_FAILED(500, "Failed to load client configuration"),
    TOKEN_LOAD_FAILED(500, "Failed to load generated token"),
    TOKEN_INSERT_FAILED(500, "Failed to save generated token"),

    ;

    private int responseCode;
    private String message;

    /**
     *
     * @param responseCode
     * @param message
     */
    TokenAppExceptionCode(int responseCode, String message) {
        this.responseCode = responseCode;
        this.message = message;
    }

    /**
     *
     * @return responseCode
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

}
