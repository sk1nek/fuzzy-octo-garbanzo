import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {

    private static String initialURL;

    private static ConcurrentLinkedQueue

    public static void main(String[] args) {

        if(!sanitizeURL(args[0])){
            System.out.println("Provided URL does not seem to be valid. Quitting...");
            return;
        }





    }

    /**
     *
     *
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

    /**
     *     *
     * @param url Location of document to be loaded
     * @return
     */
    private Document getDocumentFromUrl(String url) throws URISyntaxException, InterruptedException, IOException{


        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(initialURL)).GET().build(); // building GET request
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandler.asString()); //retrieve String representation of body

        return Jsoup.parse(httpResponse.body());

    }

    private 
}
