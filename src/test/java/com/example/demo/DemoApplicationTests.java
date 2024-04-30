package com.example.demo;

import com.example.demo.dto.UserDto;
import com.example.demo.exceptions.InvalidAgeException;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;
    @Mock
    private Environment environment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testRegisterUser() {
        UserDto userDto = new UserDto();
        userDto.setBirthDate(LocalDate.of(1990, 1, 1));
        User user = new User();
        when(modelMapper.map(userDto, User.class)).thenReturn(user);
        userService.register(userDto);
        verify(userRepository).save(user);
    }

    @Test
    void testRegisterUser_InvalidAge() {
        userService.setMIN_AGE(18);
        UserDto userDto = new UserDto();
        userDto.setBirthDate(LocalDate.now());
        assertThrows(InvalidAgeException.class, () -> userService.register(userDto));
    }

    @Test
    void testUpdateUser() {
        long userId = 1;
        UserDto userDto = new UserDto();
        userDto.setBirthDate(LocalDate.of(1990, 1, 1));
        User existingUser = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(modelMapper.map(userDto, User.class)).thenReturn(existingUser);
        userService.update(userId, userDto);
        verify(userRepository).save(existingUser);
    }

    @Test
    void testUpdateUser_InvalidAge() {
        userService.setMIN_AGE(18);
        long userId = 1;
        UserDto userDto = new UserDto();
        userDto.setBirthDate(LocalDate.now());
        User existingUser = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        assertThrows(InvalidAgeException.class, () -> userService.update(userId, userDto));
    }

    @Test
    void testDeleteUser() {
        long userId = 1;
        User existingUser = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        String result = userService.delete(userId);
        verify(userRepository).delete(existingUser);
        assertEquals("User deleted successfully.", result);
    }

    @Test
    void testDeleteUser_UserNotFound() {
        long userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.delete(userId));
    }
}
