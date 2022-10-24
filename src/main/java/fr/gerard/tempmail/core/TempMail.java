package fr.gerard.tempmail.core;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public abstract class TempMail {

    /**
     * @return The new email address
     */
    public abstract String generateNewAddress() throws IOException;

    /**
     * @return The current email address
     */
    public abstract String emailAddress();

    /**
     * @return Fetch incoming messages
     */
    public abstract List<Message> fetchMessages() throws IOException;

    public Message waitForMessage(Predicate<Message> condition, Duration timeout) throws IOException, TimeoutException {
        long millis = timeout.toMillis();
        int delay = 1000;

        for (int i = 0; i < millis / delay; i++) {
            List<Message> messages = fetchMessages();

            for (Message message : messages) {
                if (condition.test(message)) {
                    return message;
                }
            }
        }
        throw new TimeoutException(String.format("Waiting for message (%s)", millis));
    }

}
