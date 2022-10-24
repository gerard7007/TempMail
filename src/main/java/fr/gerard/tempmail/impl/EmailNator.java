package fr.gerard.tempmail.impl;

import fr.gerard.tempmail.TempMail4J;
import fr.gerard.tempmail.core.Message;
import fr.gerard.tempmail.core.TempMail;
import fr.gerard.tempmail.util.Utils;
import okhttp3.*;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EmailNator extends TempMail {

    private Logger LOGGER = Logger.getLogger(getClass().getName());

    private OkHttpClient httpClient;
    private String cookie;
    private String email;

    public EmailNator(@Nullable OkHttpClient httpClient) throws IOException {
        this.httpClient = httpClient != null ? httpClient : TempMail4J.getDefaultHttpClient();

        Request request = new Request.Builder().url("https://www.emailnator.com/")
                .headers(TempMail4J.getDefaultHeaders())
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Host", "www.emailnator.com")
                .header("Referer", "https://duckduckgo.com/")
                .header("Sec-Fetch-Dest", "document")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", "cross-site")
                .header("Sec-Fetch-User", "?1")
                .header("Upgrade-Insecure-Requests", "1")
                .build();

        try (Response response = this.httpClient.newCall(request).execute()) {
            cookie = Utils.formatCookie(response.headers("set-cookie"));
        }
    }

    public EmailNator() throws IOException {
        this(null);
    }

    private String xsrfToken() {
        try {
            return URLDecoder.decode(cookie.split("XSRF-TOKEN=")[1].split(";")[0], "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.warning("UnsupportedEncodingException: " + e.getMessage());
            return URLDecoder.decode(cookie.split("XSRF-TOKEN=")[1].split(";")[0]);
        }
    }

    private Response messageList(@Nullable String messageId) throws IOException {
        JSONObject payload = new JSONObject();
        payload.put("email", emailAddress());

        if (messageId != null) {
            payload.put("messageID", messageId);
        }

        Request request = new Request.Builder().url("https://www.emailnator.com/message-list")
                .headers(TempMail4J.getDefaultHeaders())
                .header("Accept", "application/json, text/plain, */*")
                .header("Content-Type", "application/json")
                .header("Cookie", cookie)
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-origin")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("X-XSRF-TOKEN", xsrfToken())
                .post(RequestBody.create(payload.toString().getBytes(StandardCharsets.UTF_8))).build();

        return httpClient.newCall(request).execute();
    }

    @Override
    public String generateNewAddress() throws IOException {
        byte[] payload = "{\"email\":[\"domain\",\"plusGmail\",\"dotGmail\"]}".getBytes(StandardCharsets.UTF_8);

        Request request = new Request.Builder().url("https://www.emailnator.com/generate-email")
                .headers(TempMail4J.getDefaultHeaders())
                .header("Accept", "application/json, text/plain, */*")
                .header("Content-Type", "application/json")
                .header("Cookie", cookie)
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-origin")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("X-XSRF-TOKEN", xsrfToken())
                .post(RequestBody.create(payload)).build();

        try (Response response = httpClient.newCall(request).execute(); ResponseBody body = response.body()) {
            cookie = Utils.formatCookie(response.headers("set-cookie"));
            JSONObject data = new JSONObject(body.string());
            return email = data.getJSONArray("email").getString(0);
        }
    }

    @Override
    public String emailAddress() {
        return email;
    }

    @Override
    public List<Message> fetchMessages() throws IOException {
        try (Response response = messageList(null); ResponseBody body = response.body()) {
            cookie = Utils.formatCookie(response.headers("set-cookie"));
            JSONObject data = new JSONObject(body.string());
            JSONArray messageData = data.getJSONArray("messageData");
            List<Message> messages = new ArrayList<>(messageData.length());

            for (int i = 0; i < messageData.length(); i++) {
                JSONObject jsonMessage = messageData.getJSONObject(i);

                messages.add(new Message() {
                    @Override
                    public String id() {
                        return jsonMessage.getString("messageID");
                    }

                    @Override
                    public String from() {
                        return jsonMessage.getString("from");
                    }

                    @Override
                    public String subject() {
                        return jsonMessage.getString("subject");
                    }

                    @Override
                    public String time() {
                        return jsonMessage.getString("time");
                    }

                    @Override
                    public String content() throws IOException {
                        try (Response response = messageList(this.id()); ResponseBody body = response.body()) {
                            cookie = Utils.formatCookie(response.headers("set-cookie"));
                            return body.string();
                        }
                    }

                    @Override
                    public String toString() {
                        return jsonMessage.toString();
                    }
                });
            }

            return messages;
        }
    }

    public void setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }
}
