package com.example.demo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.Period;

public class MinimumAgeValidator implements ConstraintValidator<MinimumAge, LocalDate> {
    private final int minimumAge;

    public MinimumAgeValidator(@Value("${user.min.age}") int minimumAge) {
        this.minimumAge = minimumAge;
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        // Requiredness is handled by @NotNull, independently of the age constraint.
        if (birthDate == null) {
            return true;
        }
        LocalDate today = LocalDate.now(context.getClockProvider().getClock());
        if (!birthDate.isAfter(today) && Period.between(birthDate, today).getYears() >= minimumAge) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("User must be at least " + minimumAge + " years old.")
                .addConstraintViolation();
        return false;
    }
}
