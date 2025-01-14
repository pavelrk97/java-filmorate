package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.exception.ValidationException;

// не совсем разобрался с валидациями, но мне кажется что тут от throw new ValidationException смысла особо не было,
// и так тот же код кидало вроде
public class NotSpacesValidator implements ConstraintValidator<NotSpaces, String> {
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s.contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелы");
        }
        return true;
    }
}
