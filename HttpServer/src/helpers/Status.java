package helpers;

public enum Status {
    OK(200, "OK"),
    CREATED(201,"File Created"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    private final int code;
    private final String reason;

    private Status(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public int getCode() { return code;}
    public String getReason() { return reason;}

}
