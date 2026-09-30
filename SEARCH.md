# User search

Both `GET /users` and the existing `GET /users/search` use the same controller
and service method. Responses are now `Page<UserDto>`: read users from `content`,
with `number`, `size`, `totalElements`, and `totalPages` describing the page.
This replaces the old `/users/search` array response.

```http
GET /users?filter=firstName==Nadia
GET /users?filter=age=ge=18
GET /users?filter=firstName==Nadia;age=ge=18
GET /users?filter=firstName==Nadia,firstName==Anna
GET /users?filter=age=ge=18&page=0&size=20&sort=id,asc
GET /users?page=0&size=20
GET /users/search?from=1980-01-01&to=2000-12-31&filter=firstName==Nadia
```

URL-encode query parameters in an HTTP client. For example:

```sh
curl --get 'http://localhost:8080/users' \
  --data-urlencode 'filter=(firstName==Nadia,firstName==Anna);age=ge=18' \
  --data-urlencode 'page=0' --data-urlencode 'size=20'
```

An absent or blank filter returns all users, paginated. Pages are zero-based;
the default size is 20, capped at 100, with default ordering by `id`. When
providing a non-unique sort field, add `sort=id,asc` as a tie-breaker. The
existing inclusive `from`/`to` birth-date range is optional, requires both dates,
and is ANDed with the RSQL filter.

## Implementation

The existing stack is Spring Boot 3.2.5, Spring Data JPA 3.2.5, Hibernate
6.4.4.Final, Jakarta Persistence 3.1.0, and Java 17 source compatibility.
`io.github.perplexhub:rsql-jpa:6.0.34` is the JPA-only module of the maintained
[rsql-jpa-specification project](https://github.com/perplexhub/rsql-jpa-specification).
Its 6.x line uses Jakarta persistence. It provides the parser, type conversion,
AND/OR handling, and Specification conversion without the umbrella artifact's
unneeded QueryDSL integration. Boot continues to manage the Spring/Hibernate
versions. H2 is an additional test-only dependency.

The request flows through `UserController` → `UserServiceImpl` → `UserFilter`
(library parsing and selector validation) → library `Specification<User>` →
`UserRepository.findAll(specification, pageable)` → Hibernate/database.
`Page.map` uses the existing ModelMapper to return DTOs. There is no custom
RSQL parser or AST visitor.

`User.age` is a read-only Hibernate formula for completed years as of the
database's current date. It uses `birth_date`, adds no database column, and
does not change the DTO. For February 29 birthdays, the formula advances age
on March 1 in non-leap years. It is tested on H2 and PostgreSQL. Input age
validation also uses completed years; see [VALIDATION.md](VALIDATION.md). Since it is a
computed expression, large age-filtered queries may need performance tuning;
filtering directly by `birthDate` can use an ordinary date index.

## Query boundaries

Only `id`, `firstName`, `lastName`, `birthDate`, and `age` can be filtered or
sorted. Email, address, phone number, arbitrary paths, relationships, and
function selectors are excluded. Exact selector validation complements the
library's property whitelist. Equality is strict (no implicit wildcard matching).
Filters are limited to 2,048 characters. Malformed filters, invalid values,
disallowed fields/sorts, and invalid date ranges return HTTP 400 with a generic
message in the existing `InformationResponse` format, without parser details.
Database failures are not converted into invalid-filter errors.

The allowlist controls query access, not authorization: the application's
existing DTO still contains its original contact fields. This change adds no
authentication or record-level access policy.

Run `bash mvnw verify` with a full JDK and Maven connectivity. The existing
six service tests and 23 new search cases run without an external database;
the latter exercise MockMvc, the actual library, JPA, and H2 together.
