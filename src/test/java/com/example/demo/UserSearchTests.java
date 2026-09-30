package com.example.demo;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:user-search;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Transactional
class UserSearchTests {
    @Autowired private MockMvc mvc;
    @Autowired private UserRepository repository;
    @Autowired private JdbcTemplate jdbc;

    private LocalDate today;

    @BeforeEach
    void seedUsers() {
        today = jdbc.queryForObject("select current_date", LocalDate.class);
        save("Nadia", "adult@example.com", today.minusYears(18));
        save("Nadia", "minor@example.com", today.minusYears(18).plusDays(1));
        save("Anna", "anna@example.com", today.minusYears(30));
        save("John", "john@example.com", today.minusYears(10));
        repository.flush();
    }

    @Test
    void filtersByOneFieldAndReturnsDtos() throws Exception {
        mvc.perform(get("/users/search").param("filter", "firstName==Nadia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].email", containsInAnyOrder("adult@example.com", "minor@example.com")))
                .andExpect(jsonPath("$.content[0].id").doesNotExist());
    }

    @Test
    void numericAgeComparisonIncludesBirthdayAndExcludesTomorrow() throws Exception {
        mvc.perform(get("/users").param("filter", "age=ge=18"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].email", containsInAnyOrder("adult@example.com", "anna@example.com")));
    }

    @Test
    void combinesWithAnd() throws Exception {
        mvc.perform(get("/users").param("filter", "firstName==Nadia;age=ge=18"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email").value("adult@example.com"));
    }

    @Test
    void combinesWithOr() throws Exception {
        mvc.perform(get("/users").param("filter", "firstName==Nadia,firstName==Anna"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void supportsGroupedExpressions() throws Exception {
        mvc.perform(get("/users").param("filter", "(firstName==Nadia,firstName==Anna);age=lt=18"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email").value("minor@example.com"));
    }

    @Test
    void returnsPageWithoutFilter() throws Exception {
        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.size").value(20));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void treatsBlankFilterAsUnfiltered(String filter) throws Exception {
        mvc.perform(get("/users").param("filter", filter))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4));
    }

    @Test
    void paginatesFilteredResults() throws Exception {
        mvc.perform(get("/users").param("filter", "age=ge=18")
                        .param("page", "1").param("size", "1").param("sort", "firstName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("adult@example.com"));
    }

    @Test
    void capsPageSize() throws Exception {
        mvc.perform(get("/users").param("size", "10000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100));
    }

    @Test
    void combinesExistingInclusiveDateRangeWithFilter() throws Exception {
        mvc.perform(get("/users/search").param("filter", "firstName==Nadia")
                        .param("from", today.minusYears(18).toString())
                        .param("to", today.minusYears(18).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"firstName==", "(firstName==Nadia", "age=ge=invalid", "birthDate==invalid",
            "password==secret", "email==adult@example.com", "address==anything", "firstName.class==String",
            "manager.firstName==Nadia"})
    void rejectsMalformedExpressionsInvalidValuesAndDisallowedSelectors(String filter) throws Exception {
        mvc.perform(get("/users").param("filter", filter))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Invalid user search. Check the filter, fields, values, sort, and date range."))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void rejectsOversizedFilter() throws Exception {
        mvc.perform(get("/users").param("filter", "firstName==" + "a".repeat(2048)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUnapprovedSortField() throws Exception {
        mvc.perform(get("/users").param("sort", "email,asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsIncompleteOrReversedDateRange() throws Exception {
        mvc.perform(get("/users/search").param("from", "2000-01-01"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/users/search").param("from", "2000-01-02").param("to", "2000-01-01"))
                .andExpect(status().isBadRequest());
    }

    private void save(String name, String email, LocalDate birthDate) {
        User user = new User();
        user.setFirstName(name);
        user.setLastName("Test");
        user.setEmail(email);
        user.setBirthDate(birthDate);
        repository.save(user);
    }
}
