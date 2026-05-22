package org.example.burgerbanditten.email;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    private void sendEmail(String toEmail, String subject, String body) throws IOException {
        Email from    = new Email(fromEmail);
        Email to      = new Email(toEmail);
        Content content = new Content("text/plain", body);
        Mail mail     = new Mail(from, subject, to, content);

        SendGrid sg   = new SendGrid(apiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);
        if (response.getStatusCode() >= 400) {
            throw new RuntimeException("Fejl ved afsendelse af email: " + response.getBody());
        }
    }

    // Bekræftelsesmail ved oprettelse
    public void sendRegistrationConfirmation(String toEmail, String name) {
        try {
            sendEmail(toEmail,
                    "Velkommen til BurgerBanditten!",
                    "Hej " + name + "!\n\nDin konto er blevet oprettet.\nVelkommen til BurgerBanditten!\n\nMange hilsner,\nBurgerBanditten");
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke sende bekræftelsesmail", e);
        }
    }

    // Nulstillingsmail
    public void sendPasswordReset(String toEmail, String resetLink) {
        try {
            sendEmail(toEmail,
                    "Nulstil din adgangskode – BurgerBanditten",
                    "Hej!\n\nKlik på linket for at nulstille din adgangskode:\n\n" + resetLink +
                            "\n\nMange hilsner,\nBurgerBanditten");
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke sende nulstillingsmail", e);
        }
    }

    // Ordrebekræftelse
    public void sendOrderConfirmation(String toEmail, String name, Long orderId) {
        try {
            sendEmail(toEmail,
                    "Din ordre er modtaget – BurgerBanditten",
                    "Hej " + name + "!\n\nVi har modtaget din ordre #" + orderId +
                            ".\nDu vil modtage en besked når din ordre er klar.\n\nMange hilsner,\nBurgerBanditten");
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke sende ordrebekræftelse", e);
        }
    }

    // Notifikation når ordre accepteres
    public void sendOrderAcceptedNotification(String toEmail, String name, Long orderId) {
        try {
            sendEmail(toEmail,
                    "Din ordre er accepteret – BurgerBanditten",
                    "Hej " + name + "!\n\nDin ordre #" + orderId +
                            " er blevet accepteret og er nu under behandling.\n\nMange hilsner,\nBurgerBanditten");
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke sende acceptnotifikation", e);
        }
    }

    // Notifikation når ordre ændres af admin
    public void sendOrderUpdatedNotification(String toEmail, String name, Long orderId) {
        try {
            sendEmail(toEmail,
                    "Din ordre er blevet ændret – BurgerBanditten",
                    "Hej " + name + "!\n\nDin ordre #" + orderId +
                            " er blevet ændret af restauranten.\n" +
                            "Har du spørgsmål, er du velkommen til at kontakte os.\n\n" +
                            "Mange hilsner,\nBurgerBanditten");
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke sende ændringsnotifikation", e);
        }
    }
}