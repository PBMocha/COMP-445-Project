package helpers;

public enum Method {

    GET("GET"),
    POST("POST");

    private final String value;

    private Method(String value) {
        this.value = value;
    }

    public String getMethod() { return value; }

}
