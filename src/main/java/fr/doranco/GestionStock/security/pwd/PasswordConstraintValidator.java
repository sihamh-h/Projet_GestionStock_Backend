package fr.doranco.GestionStock.security.pwd;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.passay.RuleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PasswordConstraintValidator implements ConstraintValidator <ValidPassword, String>  {
    @Autowired
    private PasswordPolicyValidator passwordPolicyValidator;


    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {


        RuleResult result = passwordPolicyValidator.validate(password);

        if (result.isValid()) return true;

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(passwordPolicyValidator.getMessages(result))
                .addConstraintViolation();

        return false;
    }
}
