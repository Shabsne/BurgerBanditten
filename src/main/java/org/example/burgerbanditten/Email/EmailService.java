package org.example.burgerbanditten.Email;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
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
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);

        if (response.getStatusCode() >= 400) {
            throw new RuntimeException("Fejl ved afsendelse af email: " + response.getBody());
        }
    }

    // Send bekræftelsesmail ved oprettelse
    public void sendRegistrationConfirmation(String toEmail, String name) {
        try {
            String subject = "Velkommen til BurgerBanditten!";
            String body = "Hej " + name + "!\n\n"
                    + "Din konto er blevet oprettet.\n"
                    + "Velkommen til BurgerBanditten – vi glæder os til at se dig!\n\n"
                    + "Mange hilsner,\nBurgerBanditten";
            sendEmail(toEmail, subject, body);
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke sende bekræftelsesmail", e);
        }
    }

    // Send nulstillingsmail ved glemt adgangskode
    public void sendPasswordReset(String toEmail, String resetLink) {
        try {
            String subject = "Nulstil din adgangskode – BurgerBanditten";
            String body = "Hej!\n\n"
                    + "Vi har modtaget en anmodning om at nulstille din adgangskode.\n"
                    + "Klik på linket herunder for at vælge en ny:\n\n"
                    + resetLink + "\n\n"
                    + "Hvis du ikke har anmodet om dette, kan du se bort fra denne email.\n\n"
                    + "Mange hilsner,\nBurgerBanditten";
            sendEmail(toEmail, subject, body);
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke sende nulstillingsmail", e);
        }
    }

    // Send ordre-bekræftelse (#send notifikation til kunde)
    public void sendOrderConfirmation(String toEmail, String name, Long orderId) {
        try {
            String subject = "Din ordre er modtaget – BurgerBanditten";
            String body = "Hej " + name + "!\n\n"
                    + "Vi har modtaget din ordre #" + orderId + ".\n"
                    + "Du vil modtage en besked når din ordre er klar til afhentning.\n\n"
                    + "Mange hilsner,\nBurgerBanditten";
            sendEmail(toEmail, subject, body);
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke sende bekræftelse på din ordre", e);
        }
    }
}






