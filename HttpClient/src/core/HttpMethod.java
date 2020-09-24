package core;

public enum HttpMethod {

    GET(1),
    POST(2),
    PUT(3),
    PATCH(4),
    DELETE(5);

    private int value;

    private HttpMethod(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

}
