package com.example.demo.services;

import com.example.demo.dto.UserDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface UserService {
    void setMIN_AGE(int MIN_AGE);
    UserDto register(UserDto request);
    UserDto update(long id, UserDto request);
    String delete(long id);
    List<UserDto> search(LocalDate from, LocalDate to);
    UserDto partiallyUpdate(long id, Map<String, Object> updates);
}
