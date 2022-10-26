package fr.gerard.tempmail.impl;

import fr.gerard.tempmail.util.Utils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

public class Kopeechka {

    private final String apiKey;

    public Kopeechka(String apiKey) {
        this.apiKey = apiKey;
    }

    public double getBalance() throws IOException {
        JSONObject json = Helper.fetch("http://api.kopeechka.store/user-balance?token=" + apiKey + "&api=2.0");
        if (json.getString("status").equals("OK")) {
            return json.getDouble("balance");
        } else {
            throw new RuntimeException("Error with kopeechka api: " + json);
        }
    }

    public Task createNewTask(String domain, String site) throws IOException {
        JSONObject json = Helper.fetch("https://api.kopeechka.store/mailbox-get-email?api=2.0&spa=1&site=" + site + "&sender=" + site.split("\\.")[0] + "&regex=&mail_type=" + domain + "&token=" + apiKey);
        if (!json.getString("status").equals("OK")) {
            throw new RuntimeException(json.toString());
        }
        return new Task(this, json.getInt("id"), json.getString("mail"));
    }

    public String getApiKey() {
        return apiKey;
    }

    public enum Domains {
        OUTLOOK("outlook.com");

        private final String value;

        Domains(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static class Task {

        private final Kopeechka kopeechka;
        private final int id;
        private final String mail;

        private Task(Kopeechka kopeechka, int id, String mail) {
            this.kopeechka = kopeechka;
            this.id = id;
            this.mail = mail;
        }

        public void deleteEmail() throws IOException {
            JSONObject json = Helper.fetch("https://api.kopeechka.store/mailbox-cancel?id=" + id + "&token=" + kopeechka.getApiKey());
        }

        public String waitForEmail(Duration timeout) throws IOException, TimeoutException {
            int interval = 2000;

            for (int i = 0; i < timeout.toMillis() / interval; i++) {
                Utils.sleep(interval);

                JSONObject json = Helper.fetch("http://api.kopeechka.store/mailbox-get-message?full=1&id=" + id + "&token=" + kopeechka.getApiKey() + "&type=JSON&api=2.0");
                String status = json.getString("status");
                String statusVal = json.getString("value");

                if (status.equals("ERROR")) {
                    if (!statusVal.equals("WAIT_LINK")) {
                        throw new RuntimeException("Unexpected error status: " + statusVal);
                    }
                } else if (status.equals("OK")) {
                    return json.getString("value");
                }
            }
            throw new TimeoutException("Email receive timeout (after " + timeout.toMillis() + "ms)");
        }

        public int getId() {
            return id;
        }

        public String getMail() {
            return mail;
        }
    }

    protected static class Helper {

        protected static JSONObject fetch(String url) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null){
                    sb.append(line).append("\n");
                }
                return new JSONObject(sb.toString());
            }
        }

    }

}
