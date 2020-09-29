package core;

/**
 * URL class that is responsible for splitting the url into its seperate segments
 */
public final class Url {

    private String fullUrl;
    private String host;
    private String resource;
    private String port;

    public Url()
    {
        //TODO: implement later
    }

    public Url(String fullUrl)
    {
        this.fullUrl = fullUrl;
        //this.port = "";
        divideUrlString();
    }

    private void divideUrlString()
    {
        //Split url to {host, resource},
        // eg. "www.localhost.com/getresource" => {"www.localhost.com", "getResource"}
        /*
            Cases to consider from left to right:
            1. url contains "http://" or "https://"
            2. url contains port; "localhost.com:8000"
            3.
         */

        //Ignore http and https
        if (fullUrl.startsWith("http://") || fullUrl.startsWith("https://")) {
            //System.out.println("Removing Http(s) extension");
            fullUrl = fullUrl.replace("https://", "");
            //System.out.println(url);
        }

        //TODO: Store the port

        //
        String[] urlSet = fullUrl.split("/", 2);

        host = urlSet[0];
        resource = "/" + urlSet[1];
    }

    @Override
    public String toString()
    {
        return fullUrl;
    }

    public String getHost()
    {
        return host;
    }

    public String getResource()
    {
        return resource;
    }


}
