package fr.gerard.tempmail.core;

import java.io.IOException;

public interface IMessage {

    /**
     * @return The message's ID
     */
    String id();

    /**
     * @return The email's sender address
     */
    String from();

    /**
     * @return The email's subject
     */
    String subject();

    /**
     * @return The receipt date
     */
    String time();

    /**
     * @return The message's content
     */
    String content() throws IOException;

}
