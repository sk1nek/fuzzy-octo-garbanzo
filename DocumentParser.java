
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class DocumentParser implements Callable<HashMap<String, Integer>> {

    private String url;

    DocumentParser(String url){
        this.url = url;
    }

    private DocumentParser(){}

    @Override
    public HashMap<String, Integer> call() throws Exception {
        HashMap<String, Integer> ret = new HashMap<>();

        Document doc = getDocumentFromUrl(url);
        Elements links = doc.select("a");
        links.eachAttr("abs:href").forEach( s -> {
            ret.put(s, ret.get(s) > 0 ? ret.get(s) + 1 : 0); // counting occurences of each href
        });

        return ret;
    }

    private Document getDocumentFromUrl(String url) throws URISyntaxException, InterruptedException, IOException {

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(url)).GET().build(); // building GET request
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandler.asString()); //retrieve String representation of body

        return Jsoup.parse(httpResponse.body());

    }
}
