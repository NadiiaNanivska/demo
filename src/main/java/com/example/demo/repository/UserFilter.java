package com.example.demo.repository;

import com.example.demo.exceptions.InvalidUserSearchException;
import com.example.demo.model.User;
import io.github.perplexhub.rsql.QuerySupport;
import io.github.perplexhub.rsql.RSQLJPASupport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

public final class UserFilter {
    private static final List<String> FIELDS = List.of("id", "firstName", "lastName", "birthDate", "age");

    private UserFilter() {
    }

    public static Specification<User> specification(String filter, Pageable pageable) {
        if (pageable.getSort().stream().anyMatch(order -> !FIELDS.contains(order.getProperty()))) {
            throw new InvalidUserSearchException();
        }
        if (!StringUtils.hasText(filter)) {
            return (root, query, builder) -> null;
        }
        try {
            if (filter.length() > 2048 || !FIELDS.containsAll(RSQLJPASupport.toMultiValueMap(filter).keySet())) {
                throw new InvalidUserSearchException();
            }
        } catch (RuntimeException ex) {
            throw new InvalidUserSearchException();
        }
        Specification<User> specification = RSQLJPASupport.toSpecification(QuerySupport.builder()
                .rsqlQuery(filter)
                .propertyWhitelist(Map.of(User.class, FIELDS))
                .strictEquality(true)
                .build());
        return (root, query, builder) -> {
            try {
                return specification.toPredicate(root, query, builder);
            } catch (RuntimeException ex) {
                throw new InvalidUserSearchException();
            }
        };
    }
}
