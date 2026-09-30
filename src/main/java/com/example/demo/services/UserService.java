package com.example.demo.services;

import com.example.demo.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface UserService {
    void setMIN_AGE(int MIN_AGE);
    UserDto register(UserDto request);
    UserDto update(long id, UserDto request);
    String delete(long id);
    Page<UserDto> search(String filter, LocalDate from, LocalDate to, Pageable pageable);
    UserDto partiallyUpdate(long id, Map<String, Object> updates);
}
