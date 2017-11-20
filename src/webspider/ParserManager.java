package webspider;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;

import java.util.Queue;
import java.util.concurrent.*;

public class ParserManager{

    private Queue<String> queue = new ArrayBlockingQueue(256);

    private ConcurrentHashMap<String, Integer> results = new ConcurrentHashMap<>();

    private static ParserManager instance;

    private ExecutorService threadPool;

    public static ParserManager getInstance() {
        if(instance == null)
            instance = new ParserManager();
        return instance;
    }

    private ParserManager(){
        threadPool = Executors.newFixedThreadPool(4);

    }

    public void addToQueue(String s){
        queue.add(s);
    }

    public void addToQueue(Collection<String> collection){
        queue.addAll(collection);
    }

    void addResult(String url){
        results.put(url, 1);
    }

    HashMap<String, Integer> getResults() {
        HashMap<String, Integer> ret = new HashMap<>(results.size());
        ret.putAll(results);
        return ret;
    }

    public void executeQueue(){

        while(!queue.isEmpty()){
            threadPool.execute(new DocumentParser(queue.remove()));

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
                    if (s != null && ((s.contains("http://")) || s.contains("https://"))) {
                        System.out.println(s);
                        addResult(s);
                        queue.add(s);
                    }
                }

            }catch(Exception ex){
                ex.printStackTrace();
            }
            executeQueue();
        }

        private Document getDocumentFromUrl(String url) throws URISyntaxException, InterruptedException, IOException {

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(url)).GET().build(); // building GET request
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandler.asString()); //retrieve String representation of body

            return Jsoup.parse(httpResponse.body());

        }

    }


}

