# rosta
Application for managing the museum's rota of volunteers and employees.

## TODO Stories

Profile

Settings

Security
- login and session management

    https://vaadin.com/docs/latest/flow/security/enabling-security
    https://www.baeldung.com/spring-security-authentication-with-a-database
- password (first-time change, expiry, lock-out, reset)

    https://www.baeldung.com/spring-security-5-default-password-encoder
    https://www.concretepage.com/spring-5/spring-security-5-default-password-encoder
- signup and password reset

    https://vaadin.com/blog/create-a-registration-form-in-pure-java
    https://vaadin.com/docs/v14/ds/components/password-field
    https://github.com/vaadin/vaadin-form-example
    https://github.com/anton-johansson/vaadin-recaptcha
    https://github.com/OWASP/passfault

Calendar
- investigate use of EntryProvider
- bank holidays (https://www.gov.uk/bank-holidays.json)

Refactor "rosta" to roster

Deployment to PI

SSL

Backup of the Mongo DB

QA
