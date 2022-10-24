package fr.gerard.tempmail.util;

import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.List;
import java.util.Random;

public class Utils {

    /*
        Requests utils
     */

    public static String formatCookie(List<String> cookies) {
        if (cookies.size() == 0) {
            throw  new IllegalArgumentException("Cookies list is empty");
        }

        StringBuilder sb = new StringBuilder();
        for (String cookie : cookies) sb.append(cookie.split("; ")[0]).append("; ");
        return sb.substring(0, sb.length() - 2);
    }

    /*
        Strings utils
     */

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return URLEncoder.encode(s);
        }
    }

    public static String randomAlphabetic(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        char[] chars = "azertyuiopqsdfghjklmwxxcvbn1234567890".toCharArray(); // technique du bled

        for (int i = 0; i < length; i++) {
            sb.append(chars[random.nextInt(chars.length - 1)]);
        }

        return sb.toString();
    }

    public static String repeat(CharSequence sequence, int n) {
        return new String(new char[n]).replace("\0", sequence);
    }

    /*
        Threads utils
     */

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.err.printf("(%s) Sleep interrupted (%sms)%n", Thread.currentThread().getName(), ms);
        }
    }

    public static void sleep(Duration duration) {
        sleep(duration.toMillis());
    }

}
