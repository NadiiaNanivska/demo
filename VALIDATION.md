# Jakarta Bean Validation evidence

The application uses `spring-boot-starter-validation` (already present), Jakarta
Bean Validation, and its Hibernate Validator implementation. No new dependency
is needed.

`@MinimumAge` declares a custom constraint with
`@Constraint(validatedBy = MinimumAgeValidator.class)`. Its validator implements
`ConstraintValidator<MinimumAge, LocalDate>` and is applied to
`UserDto.birthDate`, alongside the standard `@NotNull` and `@Past` constraints.

Spring injects `user.min.age` into the validator (18 by default). The validator
calculates completed years, honors Bean Validation's `ClockProvider`, accepts
the exact minimum birthday, and leaves null handling to `@NotNull`.

POST and PUT use `@Valid @RequestBody UserDto`: Spring MVC invokes validation
before the controller calls the service. PATCH accepts a map, so the service
validates the complete resulting DTO with the framework's `Validator`. Failed
PATCH validation rolls back the transaction, preserving the stored user.

The existing exception advice returns HTTP 400 with field-level messages:

```json
{"birthDate": "User must be at least 18 years old."}
```

`UserValidationTests` provides HTTP-to-database evidence: successful and rejected
POST/PUT/PATCH requests, null and future dates, birthday boundaries, partial
updates, and rollback on failure. Tests override `user.min.age` to 21 to prove
that the constraint uses configuration rather than a hardcoded threshold.
Run the suite with `bash mvnw verify` using a full JDK.

This demonstrates custom constraint implementation, declarative DTO validation,
framework dependency injection, standard constraints, and explicit validation
for partial updates. Whether it meets a particular assessment's evidence
requirements depends on that assessment's criteria.
