package webspider;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ParserManager{

    private final long PARSING_TIMEOUT = 5000L;
    private long freshContentTimestamp = 0;

    private final String[] validatorSchemes = {"http", "https"};

    private HashSet<String> links = new HashSet<>();
    private HashSet<String> imgs = new HashSet<>();

    private static ParserManager instance;
    private static ExecutorService threadPool;
    private static UrlValidator validator;

    private Thread freshContentWarden;

    public static ParserManager getInstance() {
        if(instance == null)
            instance = new ParserManager();
        return instance;
    }

    private ParserManager(){
        threadPool = Executors.newFixedThreadPool(4);
        validator = new UrlValidator(validatorSchemes);

        freshContentWarden = new Thread(() -> {
            while(true){
                try {
                    Thread.sleep(500L);
                    if (System.currentTimeMillis() - freshContentTimestamp > 5000L) {
                        System.out.println("No content updates since " + PARSING_TIMEOUT + "ms \nExiting");
                        closingSequence();
                    }

                } catch (InterruptedException iex) {
                    iex.printStackTrace();
                }
            }
        });
    }

    public void scheduleParsing(String s){
        if(!freshContentWarden.isAlive())
            freshContentWarden.start();
        if (!links.contains(s)) {
            threadPool.execute(new DocumentParser(s));
            freshContentTimestamp = System.currentTimeMillis();
        }
        System.out.println("Scheduling parse of " + s);

    }

    private void closingSequence(){


        writeCollectionToFile("links.txt", links);
        System.out.println("Printed links to links.txt");

        writeCollectionToFile("imgs.txt", imgs);
        System.out.println("Printed imgs to imgs.txt");

        Runtime.getRuntime().exit(0);
    }

    private void writeCollectionToFile(String fileName, Collection<String> col){
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)))) {
            for(String s: col){
                bw.write(s);
                bw.newLine();
            }
        }catch(IOException ioex){
            ioex.printStackTrace();
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
                Elements aElems = doc.select("a");

                for(Element el : aElems){
                    String s = el.attr("href");
                    if (validator.isValid(s)) {
                        ParserManager.this.links.add(s);
                        scheduleParsing(s);
                    }
                }

                Elements imgSrcs = doc.select("img");
                for(Element el: imgSrcs){
                    String s = el.attr("src");
                    imgs.add(s);
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

