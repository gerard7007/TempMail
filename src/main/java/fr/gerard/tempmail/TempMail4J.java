package fr.gerard.tempmail;

import fr.gerard.tempmail.util.okhttp.UnzippingInterceptor;
import okhttp3.ConnectionSpec;
import okhttp3.Headers;
import okhttp3.OkHttpClient;

import java.util.Collections;

public class TempMail4J {

    private static OkHttpClient httpClient;
    private static Headers headers;

    public static OkHttpClient getDefaultHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                    .addInterceptor(new UnzippingInterceptor())
                    .connectionSpecs(Collections.singletonList(ConnectionSpec.MODERN_TLS))
                    .build();
        }

        return httpClient;
    }

    public static Headers getDefaultHeaders() {
        if (headers == null) {
            headers = new Headers.Builder()
                    .set("Accept", "*/*")
                    .set("Accept-Encoding", "gzip, deflate, br")
                    .set("Accept-Language", "en-US,en;q=0.5")
                    .set("Cache-Control", "no-cache")
                    .set("Connection", "close")
                    .set("DNT", "1")
                    .set("Pragma", "no-cache")
                    .set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:106.0) Gecko/20100101 Firefox/106.0")
                    .build();
        }

        return headers;
    }
}
