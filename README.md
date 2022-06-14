# rosta
Application for managing the museum's rota of volunteers and employees.

## TODO Stories

Security
- login and session management

    https://vaadin.com/docs/latest/flow/security/enabling-security
    https://www.baeldung.com/spring-security-authentication-with-a-database
- password encryption

    https://www.baeldung.com/spring-security-5-default-password-encoder
    https://www.concretepage.com/spring-5/spring-security-5-default-password-encoder
- change password
- reset password (generate & email)
- signup (recapcha)

    https://vaadin.com/blog/create-a-registration-form-in-pure-java
    https://github.com/vaadin/vaadin-form-example
    https://github.com/anton-johansson/vaadin-recaptcha
    https://github.com/nulab/zxcvbn4j#using-this-library
    https://crunchify.com/java-generate-strong-random-password-securerandom/
- password management: first-time change, expiry, lock-out
- auditting

Settings

Calendar
- investigate use of EntryProvider
- bank holidays (https://www.gov.uk/bank-holidays.json)

Logging

Styling
- buttons

Refactor "rosta" to roster

Deployment to PI

SSL

Backup of the Mongo DB

QA
