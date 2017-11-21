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

    private final long PARSING_TIMEOUT = 5000L; //If no new content is added to Collections during this interval, program begins to close.
    private long freshContentTimestamp = 0;

    private HashSet<String> links = new HashSet<>(); //every "href" from html body
    private HashSet<String> imgs = new HashSet<>(); //every "img src" from html body

    private static ParserManager instance;
    private static ExecutorService threadPool;
    private static UrlValidator validator;

    private Thread freshContentWarden;

    /**
     * Returns ParserManager singleton instance, calls constructor if null.
     *
     */
    static ParserManager getInstance() {
        if(instance == null)
            instance = new ParserManager();
        return instance;
    }
    
    private ParserManager(){
        threadPool = Executors.newFixedThreadPool(4);
        String[] validatorSchemes = {"http", "https"};
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

    /**
     *Starts warden thread. If provided url hasn't been parsed yet, new DocumentParser is created and added to executors queue.
     * 
     * @param url URL to be parsed
     */
    void scheduleParsing(String url){
        if(!freshContentWarden.isAlive())
            freshContentWarden.start();
        if (!links.contains(url)) {
            threadPool.execute(new DocumentParser(url));
            freshContentTimestamp = System.currentTimeMillis();
        }
    }

    /**
     * Writes output to files and sends exit signal to runtime
     */
    private void closingSequence(){

        writeCollectionToFile("links.txt", links);
        System.out.println("Printed links to links.txt");

        writeCollectionToFile("imgs.txt", imgs);
        System.out.println("Printed imgs to imgs.txt");

        Runtime.getRuntime().exit(0);
    }

    /**
     *
     * @param fileName - output file name
     * @param col Collection
     */
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

    /**
     *  Runnable implementation meant to provide an elegant way of getting URLs from html bodies.
     *  Constructor String parameter is url of html body to be parsed.
     */
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

        /**
         *
         * @param url Location of HTML body to download
         * @return JSoup Document class member containing unparsed HTML body.
         * @throws URISyntaxException
         * @throws InterruptedException
         * @throws IOException
         */
        private Document getDocumentFromUrl(String url) throws URISyntaxException, InterruptedException, IOException {

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(url)).GET().build(); // building GET request
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandler.asString()); //retrieve String representation of body

            return Jsoup.parse(httpResponse.body());

        }
    }

}

