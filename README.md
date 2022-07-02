# rosta
Application for managing the museum's rota of volunteers and employees.

## Deployment

SSL

Backup of the Mongo DB


## Sprint Stories

remove opener concept

evening work periods

identify users as "keyholder"
    check at least one keyholder per day

management reports of periods worked, holidays, absences, etc.

checks of the rota should notify Directors

notifications by SMS as well as by email?

planning of events

planning of tasks


## Backlog Stories

QA

Logging

Settings
- schedule cron
- email server

Calendar
- investigate use of EntryProvider
- bank holidays (https://www.gov.uk/bank-holidays.json)

Styling
- buttons, etc.

Refactor "rosta" to roster


## Notes on Security

Security
- login and session management

    https://vaadin.com/docs/latest/flow/security/enabling-security
    https://www.baeldung.com/spring-security-authentication-with-a-database
- password encryption

    https://www.baeldung.com/spring-security-5-default-password-encoder
    https://www.concretepage.com/spring-5/spring-security-5-default-password-encoder
- change password

    https://github.com/nulab/zxcvbn4j#using-this-library
- reset password (generate & email)

    https://crunchify.com/java-generate-strong-random-password-securerandom/
- signup

    https://vaadin.com/blog/create-a-registration-form-in-pure-java
- password management: first-time change, expiry, lock-out

    https://www.codejava.net/frameworks/spring-boot/spring-security-limit-login-attempts-example
- auditing

