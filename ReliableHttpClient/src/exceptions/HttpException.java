package exceptions;

public class HttpException extends Exception{

    private int statusCode;

    public HttpException(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String getMessage()
    {
        return "HttpException";
    }

    private void handleStatusCode()
    {

    }


}
