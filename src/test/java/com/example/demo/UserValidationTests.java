package com.example.demo;

import com.example.demo.dto.UserDto;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:user-validation;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "user.min.age=21"
})
@AutoConfigureMockMvc
class UserValidationTests {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private UserRepository repository;

    private Long userId;
    private LocalDate originalBirthDate;

    @BeforeEach
    void createUser() {
        originalBirthDate = LocalDate.now().minusYears(30);
        User user = new User();
        user.setFirstName("Nadia");
        user.setLastName("Test");
        user.setEmail("existing@example.com");
        user.setBirthDate(originalBirthDate);
        userId = repository.saveAndFlush(user).getId();
    }

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    void postRejectsDayBeforeConfiguredMinimumBirthday() throws Exception {
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(dto(LocalDate.now().minusYears(21).plusDays(1)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.birthDate").value("User must be at least 21 years old."));
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void postAcceptsExactMinimumBirthday() throws Exception {
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(dto(LocalDate.now().minusYears(21)))))
                .andExpect(status().isCreated());
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void nullBirthDateIsHandledByNotNull() throws Exception {
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(dto(null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.birthDate").value("Birth date is required"));
    }

    @Test
    void futureBirthDateIsRejected() throws Exception {
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(dto(LocalDate.now().plusDays(1)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.birthDate").isString());
    }

    @Test
    void putUsesBeanValidationAndPreservesExistingData() throws Exception {
        mvc.perform(put("/users/{id}", userId).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(dto(LocalDate.now().minusYears(20)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.birthDate").value("User must be at least 21 years old."));
        assertThat(repository.findById(userId).orElseThrow().getBirthDate()).isEqualTo(originalBirthDate);
    }

    @Test
    void putAcceptsValidUser() throws Exception {
        mvc.perform(put("/users/{id}", userId).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(dto(LocalDate.now().minusYears(21)))))
                .andExpect(status().isOk());
        assertThat(repository.findById(userId).orElseThrow().getBirthDate())
                .isEqualTo(LocalDate.now().minusYears(21));
    }

    @Test
    void patchCannotBypassMinimumAgeAndRollsBack() throws Exception {
        mvc.perform(patch("/users/{id}", userId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"birthDate\":\"" + LocalDate.now().minusYears(20) + "\",\"firstName\":\"Changed\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.birthDate").value("User must be at least 21 years old."));
        User unchanged = repository.findById(userId).orElseThrow();
        assertThat(unchanged.getBirthDate()).isEqualTo(originalBirthDate);
        assertThat(unchanged.getFirstName()).isEqualTo("Nadia");
    }

    @Test
    void patchAcceptsValidBirthDate() throws Exception {
        LocalDate validDate = LocalDate.now().minusYears(21);
        mvc.perform(patch("/users/{id}", userId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"birthDate\":\"" + validDate + "\"}"))
                .andExpect(status().isOk());
        assertThat(repository.findById(userId).orElseThrow().getBirthDate()).isEqualTo(validDate);
    }

    @Test
    void patchValidatesMergedDtoWithoutRequiringAllFields() throws Exception {
        mvc.perform(patch("/users/{id}", userId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\":\"Kyiv\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("Kyiv"));
    }

    @Test
    void patchCannotRemoveRequiredBirthDate() throws Exception {
        mvc.perform(patch("/users/{id}", userId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"birthDate\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.birthDate").value("Birth date is required"));
        assertThat(repository.findById(userId).orElseThrow().getBirthDate()).isEqualTo(originalBirthDate);
    }

    private UserDto dto(LocalDate birthDate) {
        return new UserDto("new@example.com", "Anna", "Test", birthDate, null, null);
    }
}
