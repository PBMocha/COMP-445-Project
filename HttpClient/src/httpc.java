public class httpc {

    public static void main(String[] args) {
    	
    	String[] a = {"trace","-v", "http://httpbin.org/get?course=networking&assignment=1"};
    	String[] b = {"post", "-v", "-h", "Content-Type:application/json", "http://httpbin.org/post"};

        CommandLine scuffedCurl = new CommandLine(b);
        scuffedCurl.executeCommand();
    }
}