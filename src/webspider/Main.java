package webspider;

public class Main {

    private static String initialURL;

    public static void main(String[] args) {

        System.out.println(args[0]);

        if(!sanitizeURL(args[0])){
            System.out.println("Provided URL does not seem to be valid. Quitting...");
            return;
        }

        ParserManager pm = ParserManager.getInstance();
        pm.addToQueue(args[0]);
        pm.executeQueue();

    }

    /**
     * @param url - url address to be checked
     * @return true if provided URL appears as valid
     */
    private static boolean sanitizeURL(String url) {
        if(url.matches("^((http[s]?|ftp):\\/)?\\/?([^:\\/\\s]+)((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?$")){
            initialURL = url;
            return true;
        }else
            return false;
    }

}
