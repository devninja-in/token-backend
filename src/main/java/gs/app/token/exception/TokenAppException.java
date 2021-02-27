package gs.app.token.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public class TokenAppException extends RuntimeException {

    private TokenAppExceptionCode tokenAppExceptionCode;

    /**
     *
     * @param tokenAppExceptionCode
     * @param detailMessage
     */
    public TokenAppException(TokenAppExceptionCode tokenAppExceptionCode,
        String detailMessage) {
        super(detailMessage);
        this.tokenAppExceptionCode = tokenAppExceptionCode;
    }

    /**
     *
     * @return response code
     */
    public int getResponseCode() {
        return tokenAppExceptionCode.getResponseCode();
    }

    /**
     *
     * @return message
     */
    public String getMessage() {
        return tokenAppExceptionCode.getMessage();
    }

    /**
     *
     * @return detail message
     */
    public String getDetailMessage() {
        return super.getMessage();
    }

    /**
     *
     * @return exception details as map
     */
    public Map<String, Object> toMap() {
        LinkedHashMap<String, Object> exceptionFieldsAsMap = new LinkedHashMap<>();
        exceptionFieldsAsMap.put("message", getMessage());
        exceptionFieldsAsMap.put("detailMessage", getDetailMessage());
        return exceptionFieldsAsMap;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TokenAppException.class.getSimpleName() + "[", "]")
            .add("ResponseCode=" + getResponseCode()).add("Message=" + getMessage())
            .add("DetailMessage=" + getDetailMessage()).toString();
    }
}
