package fr.doranco.GestionStock.email;

import fr.doranco.GestionStock.enums.TokenType;
import fr.doranco.GestionStock.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountActivationEMailStrategy extends AbstractEmailContext {
        public AccountActivationEMailStrategy(User user, String frontendUrl) {
            super(user, frontendUrl);
        }
    @Override
    public String getSubject() {
        return "Activate your account";
    }

    @Override
    public String buildContent(String name, String tokenOrPwd) {
        return "Hello " + name + ",\n\n" +
                "An account has been created for you."+
                "To activate your account and set your password, please click the following link : \n" +
                "http://localhost:8080/api/rest/auth/activate?token=" + tokenOrPwd + "\n\n" +
                "This link is valid for 24 hours."+
                "If you did not request this email, please ignore it.\n\n" +
                "Best regards,\n" +
                "The Team";
    }

    @Override
    public TokenType getSupportedType() {
        return TokenType.ACTIVATION;
    }
}
