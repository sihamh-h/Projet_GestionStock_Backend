package fr.doranco.GestionStock.email;

import fr.doranco.GestionStock.enums.TokenType;
import fr.doranco.GestionStock.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ForgotPasswordEmailStrategy extends AbstractEmailContext {
    public ForgotPasswordEmailStrategy(User user, String frontendUrl) {
        super(user, frontendUrl);
    }

    @Override
    public String getSubject() {
        return "Reset your password - GestionStock";
    }

    @Override
    public String buildContent(String name, String token) {
        return "Hello " + name + ",\n\n" +
                "We have received a request to reset the password for your account.\n" +
                "Please click the link below to choose a new password : \n" +
                "http://localhost:8080/api/rest/auth/reset-password?token=" + token + "\n\n" +
                "This link is valid for 1 hour.\n" +
                "If you did not request this password reset, please ignore this email.\n\n" +
                "Kind regards,\n" +
                "The Team";
    }

    @Override
    public TokenType getSupportedType() {
        return TokenType.PASSWORD_RESET;
    }
}
