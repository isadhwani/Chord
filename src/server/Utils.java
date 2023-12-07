package server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    static final int START_PORT = 5000;
    public static final int CLIENT_LISTEN_PORT = 4000;
    public static final int CLIENT_TALK_PORT = 4001;
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.out.println("Interrupted!");
        }
    }

    public static int extractNumberFromTarget(String value, String target) {
        // Define a pattern to match "peer" followed by one or more digits
        Pattern pattern = Pattern.compile(target + "(\\d+)");
        // Create a matcher with the input hostName
        Matcher matcher = pattern.matcher(value);
        // Check if the pattern is found
        if (matcher.find()) {
            // Extract and parse the matched digits
            String numberString = matcher.group(1);
            return Integer.parseInt(numberString);
        } else {
            // Return a default value or throw an exception, depending on your requirements
            throw new IllegalArgumentException("Invalid host name format: " + value);
        }
    }
}
