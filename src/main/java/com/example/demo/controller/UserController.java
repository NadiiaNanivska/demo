package com.example.demo.controller;

import com.example.demo.dto.UserDto;
import com.example.demo.model.User;
import com.example.demo.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAllFields(@PathVariable long id, @RequestBody UserDto user) {
        return ResponseEntity.ok(userService.update(id, user));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partiallyUpdateUser(@PathVariable long id, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(userService.partiallyUpdate(id, updates));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.delete(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsersByBirthDateRange(@RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                                     @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(userService.search(fromDate, toDate));
    }


}
