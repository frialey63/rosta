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

Configure the email server

Backup of the Mongo DB

    https://www.mongodb.com/docs/manual/tutorial/backup-and-restore-tools/

## TODO

rationalise employee and volunteer capabilities

should an employee take bank holidays?

## Bugs

## Sprint 3 Stories

push notification of changes by other users to the calendar

notifications by SMS as well as by email?

register attendance via QR code or bluetooth proxity

## Sprint 4 Stories

volunteer attendance and reliability stats/predictions

planning of events

planning of tasks

## Done

better sitrep view for manager on calendar using background events (overview rota for next 3 months?)

improved rules for add >1 entry on a day

ordering of block events on calendar, https://fullcalendar.io/docs/eventOrder

refresh button for calendar

mobile friendly

bank holidays (https://www.gov.uk/bank-holidays.json)

real-time notifications of user updates to their calendars, e.g. holiday/absence/volunteer

one week calendar view

user roles

tooltips on Calendar, see https://vaadin.com/directory/component/full-calendar-flow/samples

idle notification

new data attributes for user
- contact telephone
- emergency name and telephone

shop info docs for users

session inactivity timeouts

management reports of periods worked, holidays, absences, etc.

the supervisor/manager should be able to add/delete days in the calendar on behalf of other users

remove opener concept

evening work periods

identify users as "keyholder"
- check at least one keyholder per day

checks of the rota should notify managers and Directors

## Backlog Stories

QA

Logging

Calendar
- display half days as half fills on the calendar
- investigate use of EntryProvider

Styling
- buttons, etc.

Refactor "rosta" to roster

## Notes on Security

SSL

    keytool -genkeypair -alias rosta -keyalg RSA -keysize 4096 -storetype PKCS12 -keystore rosta.p12 -validity 3650 -storepass password
    keytool -import -alias rosta -file myCertificate.crt -keystore rosta.p12 -storepass password

Security
- idle notification

    https://stackoverflow.com/questions/72689660/idle-notification-not-showing-vaadin-14
    https://vaadin.com/directory/component/idle-notification/discussions
- session inactivity timeouts

    http://doc.sibvisions.com/applications/timeouts_vaadin
    https://stackoverflow.com/questions/48741820/vaadin-8-set-session-timeout
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
- account management: first-time login, password expiry, account lock-out, time of last login (notification)

    https://www.codejava.net/frameworks/spring-boot/spring-security-limit-login-attempts-example
- remember me authentication

- auditing


