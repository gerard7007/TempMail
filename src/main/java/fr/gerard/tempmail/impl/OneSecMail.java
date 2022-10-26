package fr.gerard.tempmail.impl;

import fr.gerard.tempmail.TempMailHelper;
import fr.gerard.tempmail.core.Message;
import fr.gerard.tempmail.core.TempMail;
import fr.gerard.tempmail.util.Utils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class OneSecMail extends TempMail {

    private static final Logger LOGGER = Logger.getLogger(Logger.class.getName());
    private static Predicate<String> isDomainAvailable;

    private OkHttpClient httpClient;
    private String domain;
    private String email;

    static {
        Request request = new Request.Builder().url("https://www.1secmail.com/api/v1/?action=getDomainList")
                .build();

        try (Response response = TempMailHelper.getDefaultHttpClient().newCall(request).execute(); ResponseBody body = response.body()) {
            JSONArray array = new JSONArray(body.string());
            List<String> domains = new ArrayList<>(array.length());

            for (int i = 0; i < array.length(); i++) {
                domains.add(array.getString(i));
            }

            isDomainAvailable = s -> domains.contains("s");
        } catch (Exception e) {
            LOGGER.severe("Unable to fetch available domains: " + e);

            String[] domains = {
                    "1secmail.com",
                    "1secmail.org",
                    "1secmail.net",
            };

            isDomainAvailable = s -> Arrays.asList(domains).contains(s);
        }
    }

    public OneSecMail(@NotNull OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public OneSecMail() {
        this(TempMailHelper.getDefaultHttpClient());
    }

    private String[] emailSplit() {
        return emailAddress().split("@");
    }

    @Override
    public String generateNewAddress() {
        if (domain == null)
            setDomain("1secmail.com");

        return email = Utils.randomAlphabetic(13) + "@" + domain;
    }

    @Override
    public String emailAddress() {
        return email;
    }

    @Override
    public List<Message> fetchMessages() throws IOException {
        String[] emailSplit = emailSplit();

        Request request = new Request.Builder().url(String.format("https://www.1secmail.com/api/v1/?action=getMessages&login=%s&domain=%s", emailSplit[0], emailSplit[1]))
                .build();

        try (Response response = httpClient.newCall(request).execute(); ResponseBody body = response.body()) {
            JSONArray array = new JSONArray(body.string());
            List<Message> messages = new ArrayList<>(array.length());

            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                int id = json.getInt("id");

                Request readRequest = new Request.Builder().url(String.format("https://www.1secmail.com/api/v1/?action=readMessage&login=%s&domain=%s&id=%s", emailSplit[0], emailSplit[1], id))
                        .build();

                try (Response readResponse = httpClient.newCall(readRequest).execute(); ResponseBody readBody = readResponse.body()) {
                    JSONObject data = new JSONObject(readBody.string());

                    messages.add(new Message() {
                        @Override
                        public String id() {
                            return id + "";
                        }

                        @Override
                        public String from() {
                            return data.getString("from");
                        }

                        @Override
                        public String subject() {
                            return data.getString("subject");
                        }

                        @Override
                        public String time() {
                            return data.getString("date");
                        }

                        @Override
                        public String content() throws IOException {
                            return data.getString("body");
                        }
                    });
                }
            }

            return messages;
        }
    }

    public void setDomain(@NotNull String domain) {
        if (isDomainAvailable.test(domain))
            throw new IllegalArgumentException(String.format("Unknown domain \"%s\"", domain));

        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(@NotNull OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }
}
