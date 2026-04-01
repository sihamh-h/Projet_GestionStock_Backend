package fr.doranco.GestionStock.email;

import fr.doranco.GestionStock.enums.TokenType;
import fr.doranco.GestionStock.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordChangedEmailStrategy extends AbstractEmailContext{
    PasswordChangedEmailStrategy (User user, String frontendUrl) {
        super(user, frontendUrl);
    }
    @Override
    public String getSubject() {
        return "Security Alert: Your password has been updated";
    }

    @Override
    public String buildContent(String name, String info) {
        return "Bonjour " + name + ",\n\n" +
                "Le mot de passe de votre compte GestionStock a été modifié avec succès.\n" +
                "Si vous avez effectué ce changement, vous pouvez ignorer cet e-mail.\n\n" +
                "Si vous n'avez PAS modifié votre mot de passe, veuillez contacter un administrateur immédiatement.\n\n" +
                "Cordialement,\n" +
                "L'équipe Sécurité";
    }

    @Override
    public TokenType getSupportedType() {
        return TokenType.CONFIRMATION;
    }
}
