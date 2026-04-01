package fr.doranco.GestionStock.security.pwd;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { PasswordConstraintValidator.class })
public @interface  ValidPassword {
    String message() default "The password must contain at least 12 characters, including at least one uppercase letter, one lowercase letter, one digit, and one special character.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
