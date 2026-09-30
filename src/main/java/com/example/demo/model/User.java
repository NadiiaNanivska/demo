package com.example.demo.model;

import com.example.demo.constants.UserValidationConstants;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Email(message = UserValidationConstants.INVALID_EMAIL)
    private String email;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    @Past
    private LocalDate birthDate;
    // Completed years as of the database's current date; no stored age column.
    @Setter(AccessLevel.NONE)
    @Formula("""
            extract(year from current_date) - extract(year from birth_date)
            - case when extract(month from current_date) * 100 + extract(day from current_date)
                      < extract(month from birth_date) * 100 + extract(day from birth_date)
                   then 1 else 0 end
            """)
    private Integer age;
    private String address;
    private String phoneNumber;
}
