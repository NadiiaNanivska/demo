package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findById(Long id);
    List<User> findAllByBirthDateBetween(LocalDate birthDate, LocalDate birthDate2);
}