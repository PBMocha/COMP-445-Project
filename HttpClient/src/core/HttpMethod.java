package core;

public enum HttpMethod {

    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    PATCH("PATCH"),
    DELETE("DELETE");

    private String value;

    HttpMethod(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue()
    {

    }



}
