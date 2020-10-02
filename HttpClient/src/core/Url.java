package core;

import java.net.URL;

/**
 * URL class that is responsible for splitting the url into its seperate segments
 */
public final class Url {

    private String fullUrl;
    private String host;
    private String resource;
    private String port;

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

        //Cases to consider; quotes singular or double will break the parsing for the url
        fullUrl = fullUrl.replace("\'", "");

        //Ignore http and https
        if (fullUrl.startsWith("http://")) {
            //System.out.println("Removing Http(s) extension");
            fullUrl = fullUrl.replace("http://", "");
            //System.out.println(fullUrl);
        }
        else if (fullUrl.startsWith("https://")) {
            fullUrl = fullUrl.replace("https://", "");
        }

        //TODO: Store the port

        //TODO: Make below more robust; Possibly use regex for matching www.***.**/
        String[] urlSet = fullUrl.split("/", 2);
        //If no resource path is given; default to /
        if (urlSet.length < 2) {
            host = urlSet[0];
            resource = "/";
            return;
        }

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
