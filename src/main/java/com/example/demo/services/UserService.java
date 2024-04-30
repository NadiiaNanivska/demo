package com.example.demo.services;

import com.example.demo.exceptions.InvalidAgeException;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.demo.dto.UserDto;
import com.example.demo.constants.UserValidationConstants;

import java.beans.PropertyDescriptor;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    public void setMIN_AGE(int MIN_AGE) {
        this.MIN_AGE = MIN_AGE;
    }

    @Value("${user.min.age}")
    private int MIN_AGE;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserDto register(UserDto request) {
       if (calculateAge(request.getBirthDate()) < MIN_AGE) {
            throw new InvalidAgeException(UserValidationConstants.INVALID_AGE);
        }
        User user = modelMapper.map(request, User.class);
        userRepository.save(user);
        return request;
    }

    public UserDto update(long id, UserDto request) {
        User user = getUser(id);
        if (calculateAge(request.getBirthDate()) < MIN_AGE) {
            throw new InvalidAgeException(UserValidationConstants.INVALID_AGE);
        }
        modelMapper.map(request, user);
        userRepository.save(user);
        return request;
    }

    public String delete(long id) {
        User user = getUser(id);
        userRepository.delete(user);
        return "User deleted successfully.";
    }

    public List<UserDto> search(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new InvalidAgeException("Start date must be earlier than end date");
        }
        return userRepository.findAllByBirthDateBetween(from, to).stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }

    private User getUser(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(UserValidationConstants.INVALID_CREDENTIALS));
    }

    private int calculateAge(LocalDate birthDate) {
        return LocalDate.now().minusYears(birthDate.getYear()).getYear();
    }

    @Transactional
    public UserDto partiallyUpdate(long id, Map<String, Object> updates) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(UserValidationConstants.INVALID_CREDENTIALS));
        applyUpdatesToUser(existingUser, updates);
        User updatedUser = userRepository.save(existingUser);
        UserDto map = modelMapper.map(updatedUser, UserDto.class);
        return map;
    }

    private void applyUpdatesToUser(User user, Map<String, Object> updates) {
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String fieldName = entry.getKey();
            Object newValue = entry.getValue();
            if ("birthDate".equals(fieldName) && newValue instanceof String) {
                newValue = LocalDate.parse((String) newValue);
            }
            setProperty(user, fieldName, newValue);
        }
    }

    private void setProperty(User user, String fieldName, Object newValue) {
        try {
            PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(User.class, fieldName);
            if (propertyDescriptor != null && propertyDescriptor.getWriteMethod() != null) {
                propertyDescriptor.getWriteMethod().invoke(user, newValue);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error setting property '" + fieldName + "' on User entity", e);
        }
    }
}
