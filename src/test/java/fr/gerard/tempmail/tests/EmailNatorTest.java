package fr.gerard.tempmail.tests;

import fr.gerard.tempmail.core.Message;
import fr.gerard.tempmail.core.TempMail;
import fr.gerard.tempmail.impl.EmailNator;

import java.time.Duration;

public class EmailNatorTest {

    public static void main(String[] args) throws Exception {

        TempMail tempMail = new EmailNator();
        String email = tempMail.generateNewAddress();

        System.out.println("Email address: " + email);
        System.out.println("Waiting for email...");

        Message message = tempMail.waitForMessage(msg -> msg.from().toLowerCase().contains("trustdice"), Duration.ofSeconds(300));
        String verifLink = message.content().split("<a style=\"word-break: break-all;\" href=\"")[1].split("\"")[0];

        System.out.println(verifLink);

    }
}
