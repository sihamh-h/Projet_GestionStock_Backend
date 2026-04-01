package fr.doranco.GestionStock.security.pwd;

import lombok.RequiredArgsConstructor;
import org.passay.*;
import org.springframework.stereotype.Component;


import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class PasswordPolicyValidator {
    private final PasswordValidator validator = new PasswordValidator(Arrays.asList(
            new LengthRule(12, 50),
            new CharacterRule(EnglishCharacterData.UpperCase, 1),
            new CharacterRule(EnglishCharacterData.LowerCase, 1),
            new CharacterRule(EnglishCharacterData.Digit, 1),
            new CharacterRule(EnglishCharacterData.Special, 1),
            new WhitespaceRule()
            //new HistoryRule(3)
    ));

   /* public boolean validate(String password) throws IllegalArgumentException {
        RuleResult result = validator.validate(new PasswordData(password));
        if (!result.isValid()) {
            String messages = String.join(", ",validator.getMessages(result));
            throw new IllegalArgumentException(messages);
        }
        return true;
    }*/
   public RuleResult validate(String password) {
       return validator.validate(new PasswordData(password));
   }

    public String getMessages(RuleResult result) {
        return String.join(" ",validator.getMessages(result));
    }
}
