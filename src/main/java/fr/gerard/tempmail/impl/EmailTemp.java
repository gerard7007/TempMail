package fr.gerard.tempmail.impl;

import fr.gerard.tempmail.TempMailHelper;
import fr.gerard.tempmail.core.Message;
import fr.gerard.tempmail.core.TempMail;
import fr.gerard.tempmail.util.Utils;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class EmailTemp extends TempMail {

    private OkHttpClient httpClient;
    private String email;
    private String csrfToken;
    private String cookie;

    public EmailTemp(@NotNull OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public EmailTemp() {
        this(TempMailHelper.getDefaultHttpClient());
    }

    private JSONArray getMessages() throws IOException {
        RequestBody requestBody = RequestBody.create(("_token=" + csrfToken + "&captcha=").getBytes(StandardCharsets.UTF_8));

        Request request = new Request.Builder().url("https://emailtemp.org/messages")
                .headers(TempMailHelper.getDefaultHeaders())
                .header("Accept", " */*")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("Cookie", cookie)
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-origin")
                .header("X-Requested-With", "XMLHttpRequest")
                .post(requestBody).build();

        try (Response response = this.httpClient.newCall(request).execute()) {
            try (ResponseBody responseBody = response.body()) {
                String body = responseBody.string();

                if (!body.startsWith("{"))
                    throw new RuntimeException("Malformed json message");

                JSONObject json = new JSONObject(body);

                if (json.isNull("mailbox") || json.isNull("messages"))
                    throw new RuntimeException("Unexpected json (\"" + json + "\")");

                email = json.getString("mailbox");
                cookie = Utils.formatCookie(response.headers("set-cookie"));

                return json.getJSONArray("messages");
            }
        }
    }

    @Override
    public String generateNewAddress() throws IOException {
        Request request = new Request.Builder().url("https://emailtemp.org/en")
                .headers(TempMailHelper.getDefaultHeaders())
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Sec-Fetch-Dest", "document")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", "cross-site")
                .header("Sec-Fetch-User", "?1")
                .header("Upgrade-Insecure-Requests", "1")
                .get().build();

        try (Response response = this.httpClient.newCall(request).execute()) {
            try (ResponseBody responseBody = response.body()) {
                String body = responseBody.string();
                cookie = Utils.formatCookie(response.headers("set-cookie"));
                csrfToken = body.split("<meta name=\"csrf-token\" content=\"")[1].split("\"")[0];
                getMessages();
                return email;
            }
        }
    }

    @Override
    public String emailAddress() {
        return email;
    }

    @Override
    public List<Message> fetchMessages() throws IOException {
        JSONArray array = getMessages();

        System.out.println(array.toString(4));

        return Collections.singletonList(null);
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }
}
