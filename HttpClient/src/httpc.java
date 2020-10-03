public class httpc {

    public static void main(String[] args) {
    	
    	String[] a = {"trace","-v", "https://www.google.com/"};
    	String[] b = {"post", "-v", "-h", "Content-Type:application/json", "http://httpbin.org/post"};

    	String[] c = {"help", "trace"};

        CommandLine scuffedCurl = new CommandLine(a);
        scuffedCurl.executeCommand();
    }
}