package fr.gerard.tempmail.tests;

import fr.gerard.tempmail.core.Message;
import fr.gerard.tempmail.impl.OneSecMail;

import java.util.concurrent.TimeUnit;

public class MainTest {

    public static void main(String[] args) throws Exception {

        // TempMail tempMail = new EmailNator();
        // String email = tempMail.generateNewAddress();
        //
        // System.out.println("Email address: " + email);
        // System.out.println("Waiting for email...");
        //
        // Message message = tempMail.awaitMessage(msg -> msg.from().toLowerCase().contains("trustdice")).get(300, TimeUnit.SECONDS);
        // String verifLink = message.content().split("<a style=\"word-break: break-all;\" href=\"")[1].split("\"")[0];
        //
        // System.out.println(verifLink);


        OneSecMail tempMail = new OneSecMail(); // Specify email api here
        tempMail.setDomain("dcctb.com");
        String email = tempMail.generateNewAddress();

        System.out.println("Email address: " + email);
        System.out.println("Waiting for email...");

        Message message = tempMail.awaitMessage(msg -> msg.from().contains("@")).get(300, TimeUnit.SECONDS);

        System.out.println("Received");
        System.out.println(message.content());

    }
}
