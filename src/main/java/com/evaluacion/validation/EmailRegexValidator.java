package com.evaluacion.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class EmailRegexValidator implements ConstraintValidator<ValidEmail, String> {

    @Value("${validacion.regex.correo}")
    private String regex;

    private Pattern pattern;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return false;
        }
        if (pattern == null) {
            pattern = Pattern.compile(regex);
        }
        return pattern.matcher(email).matches();
    }
}
