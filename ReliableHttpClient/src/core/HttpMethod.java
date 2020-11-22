package core;

public enum HttpMethod {

    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    PATCH("PATCH"),
    DELETE("DELETE"),
    TRACE("TRACE");

    private String value;

    HttpMethod(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }


}
