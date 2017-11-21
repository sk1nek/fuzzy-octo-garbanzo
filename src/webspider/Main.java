package webspider;

import org.apache.commons.validator.routines.UrlValidator;

public class Main {

    private static String initialURL;

    public static void main(String[] args) {

        UrlValidator urlValidator = new UrlValidator();
        if (!urlValidator.isValid(args[0])) {
            System.out.println("Provided URL does not seem to be valid. Quitting...");
            return;
        }

        WebCrawler.getInstance().scheduleParsing(args[0]);

    }
}
