package fr.doranco.GestionStock.email;

import fr.doranco.GestionStock.enums.TokenType;
import fr.doranco.GestionStock.model.entity.User;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final List<IEmailStrategy> strategies;

    @Value("${spring.mail.username}")
    private String defaultFrom;

    @Value("${app.mail.from-name:Gestion Stock}")
    private String fromName;



    public void sendEmail(User user, String token, TokenType type ) {
        IEmailStrategy strategy = strategies.stream()
                .filter(s -> s.getSupportedType() == type)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Stratégie non trouvée"));

        try {
            // Utilisation de MimeMessage pour pouvoir ajouter un "Nom d'affichage" (ex: "Gestion Stock <support@test.com>")
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(new InternetAddress(defaultFrom, fromName));
            helper.setTo(user.getEmail());
            helper.setSubject(strategy.getSubject());
            helper.setText(strategy.buildContent(user.getFirstname(), token), false); // false = texte brut, true = HTML

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi du mail : " + e.getMessage());
        }
    }


}
