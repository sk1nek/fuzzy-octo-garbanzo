package webspider;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ParserManager{

    private final String[] validatorSchemes = {"http", "https"};

    private HashSet<String> results = new HashSet<>();

    private static ParserManager instance;
    private static ExecutorService threadPool;
    private static UrlValidator validator;

    public static ParserManager getInstance() {
        if(instance == null)
            instance = new ParserManager();
        return instance;
    }

    private ParserManager(){
        threadPool = Executors.newFixedThreadPool(4);
        validator = new UrlValidator(validatorSchemes);
    }

    public void scheduleParsing(String s){
        if (!results.contains(s)) {
            threadPool.execute(new DocumentParser(s));
        }

    }

    class DocumentParser implements Runnable{

        private String url;

        DocumentParser(String url){
            this.url = url;
        }

        private DocumentParser(){}

        @Override
        public void run() {
            try{
                Document doc = getDocumentFromUrl(url);
                System.out.println("Getting doc");
                Elements links = doc.select("a");

                for(Element el : links){
                    String s = el.attr("href");
                    if (validator.isValid(s)) {
                        results.add(s);
                        scheduleParsing(s);
                    }
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }

        private Document getDocumentFromUrl(String url) throws URISyntaxException, InterruptedException, IOException {

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(url)).GET().build(); // building GET request
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandler.asString()); //retrieve String representation of body

            return Jsoup.parse(httpResponse.body());

        }

    }


}

