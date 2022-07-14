# rosta
Application for managing the museum's rota of volunteers and employees.

## Deployment

run as a windows service

    https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html
    https://dzone.com/articles/spring-boot-as-a-windows-service-in-5-minutes
    https://www.baeldung.com/spring-boot-app-as-a-service

SSL

    https://www.thomasvitale.com/https-spring-boot-ssl-certificate/
    https://www.baeldung.com/spring-boot-https-self-signed-certificate

Backup of the Mongo DB

    https://www.mongodb.com/docs/manual/tutorial/backup-and-restore-tools/

## TODO

new data attributes for user

    contact telephone number
    next of kin

shop info docs for users

## Bugs

duplicated notifications from crudui

## Sprint 3 Stories

notifications by SMS as well as by email?

real-time notifications of user updates to their calendars, e.g. holiday/absence/volunteer

## Sprint 4 Stories

planning of events

planning of tasks

## Done

management reports of periods worked, holidays, absences, etc.

the admin user should be able to add/delete days in the calendar on behalf of other users

remove opener concept

evening work periods

identify users as "keyholder"
- check at least one keyholder per day

checks of the rota should notify Directors

## Backlog Stories

QA

Logging

Settings
- schedule cron
- email server

Calendar
- display half days as half fills on the calendar
- investigate use of EntryProvider
- bank holidays (https://www.gov.uk/bank-holidays.json)

Styling
- buttons, etc.

Refactor "rosta" to roster

## Notes on Security

SSL

    keytool -genkeypair -alias rosta -keyalg RSA -keysize 4096 -storetype PKCS12 -keystore rosta.p12 -validity 3650 -storepass password

    keytool -import -alias rosta -file myCertificate.crt -keystore rosta.p12 -storepass password


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
