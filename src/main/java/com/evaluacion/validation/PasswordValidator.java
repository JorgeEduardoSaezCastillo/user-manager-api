package com.evaluacion.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Value("${validacion.regex.contrasena}")
    private String regex;

    private Pattern pattern;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // Se inicializa el patrón dinámicamente en isValid
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) return false;
        if (pattern == null) {
            pattern = Pattern.compile(regex);
        }
        return pattern.matcher(password).matches();
    }
}
