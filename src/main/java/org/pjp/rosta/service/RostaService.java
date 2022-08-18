package org.pjp.rosta.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.pjp.rosta.bean.PartOfDay;
import org.pjp.rosta.bean.Rosta;
import org.pjp.rosta.bean.RostaDay;
import org.pjp.rosta.model.AbsenceDay;
import org.pjp.rosta.model.AbstractDay;
import org.pjp.rosta.model.DayType;
import org.pjp.rosta.model.Holiday;
import org.pjp.rosta.model.Shift;
import org.pjp.rosta.model.User;
import org.pjp.rosta.model.UserRole;
import org.pjp.rosta.model.VolunteerDay;
import org.pjp.rosta.repository.AbsenceDayRepository;
import org.pjp.rosta.repository.AbstractDayRepository;
import org.pjp.rosta.repository.HolidayRepository;
import org.pjp.rosta.repository.ShiftRepository;
import org.pjp.rosta.repository.UserRepository;
import org.pjp.rosta.repository.VolunteerDayRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

@Service
public class RostaService {

    private static final int MIN_COVER_COUNT = 2;

    private static final String VOLUNTEER_TEMPLATE = "file:template/volunteer-template.txt";

    private static final String MANAGEMENT_TEMPLATE = "file:template/management-template.txt";

    private static final String MANAGEMENT_OK_TEMPLATE = "file:template/management-ok-template.txt";

    private static final String IMMEDIATE_TEMPLATE = "file:template/immediate-template.txt";

    private static final String TEST_TEMPLATE = "file:template/test-template.txt";

    private static final Logger LOGGER = LoggerFactory.getLogger(RostaService.class);

    private static final String ROSTA_TEMPLATE_DOCX = "file:template/rosta-template.docx";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private static String nameFromEmail(String email) {
        return email.split("@")[0].split("\\.")[0];
    }

    private static String firstName(String name) {
        return name.split(" ")[0];
    }

    static List<DayOfWeek> getDayOfWeekRange(DayOfWeek start, DayOfWeek end) {
        List<DayOfWeek> result = new ArrayList<>();

        DayOfWeek dow = start;

        for (int i = dow.getValue(); i <= end.getValue(); i++) {
            result.add(dow);
            dow = dow.plus(1);
        }

        return Collections.unmodifiableList(result);
    }

    private static void insertAtBookmark(XWPFParagraph para, String bookmarkName, String insertion) {
        for (CTBookmark bookmark : para.getCTP().getBookmarkStartList()) {
            if (bookmarkName.equals(bookmark.getName())) {
                XWPFRun newRun = para.insertNewRun(0);
                newRun.setText(insertion);
            }
        }
    }

    @SuppressWarnings("unused")
    private static void findAndReplace(XWPFParagraph para, String search, String replacement) {
        for (XWPFRun run : para.getRuns()) {
            String text = run.getText(0);

            if ((text != null) && text.contains(search)) {
                text = text.replace(search, replacement);
                run.setText(text, 0);
            }
        }
    }

    private record MissingCover(DayOfWeek dayOfWeek, PartOfDay partOfDay, int count, boolean keyholder) {
        @Override
        public String toString() {
            return String.format("%-9s %-9s %d %s", dayOfWeek, partOfDay, count, (keyholder ? "" : "(no key-holder)"));
        }
    }

    @Value("${init.data:false}")
    private boolean initData;

    @Value("${check.rosta.director.email}")
    private String checkRostaDirectorEmail;

    @Value("${test.email.to:none}")
    private String testEmailTo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ShiftRepository shiftRepo;

    @Autowired
    private VolunteerDayRepository volunteerDayRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private AbsenceDayRepository absenceDayRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ResourceLoader resourceLoader;

    @PostConstruct
    public void postConstruct() {
        if (initData) {
            LOGGER.debug("initialising shifts and days");
            shiftRepo.deleteAll();
            absenceDayRepository.deleteAll();
            holidayRepository.deleteAll();
            volunteerDayRepository.deleteAll();
        }
    }

    public void testData() {
        userRepo.deleteAll();
        shiftRepo.deleteAll();
        absenceDayRepository.deleteAll();
        holidayRepository.deleteAll();
        volunteerDayRepository.deleteAll();

        {
            String id = UUID.randomUUID().toString();
            User user = new User(id, "manager", UserRole.MANAGER, "Manager", ("{bcrypt}" + PASSWORD_ENCODER.encode("password")), true, "manager@gmail.com", true, true, false);
            userRepo.save(user);
        }

        LocalDate date = LocalDate.of(2022, 5, 16);

        {
            var id = UUID.randomUUID().toString();
            var user = new User(id, "fred", UserRole.SUPERVISOR, "Fred Bloggs", ("{bcrypt}" + PASSWORD_ENCODER.encode("password")), true, "fred@gmail.com", true, true, true);
            userRepo.save(user);

            var shift = new Shift(UUID.randomUUID().toString(), date, id);
            shift.getShiftDay(DayOfWeek.MONDAY).setMorning(false);
            shiftRepo.save(shift);

            shift = new Shift(UUID.randomUUID().toString(), date.minusWeeks(1), id);
            shiftRepo.save(shift);

            shift = new Shift(UUID.randomUUID().toString(), date.plusWeeks(1), id);
            shiftRepo.save(shift);

            var holiday = new Holiday(UUID.randomUUID().toString(), LocalDate.of(2022, 5, 18), true, true, id);
            holidayRepository.save(holiday);
        }

        {
            var id = UUID.randomUUID().toString();
            var user = new User(id, "bill", UserRole.WORKER, "Bill Smith", ("{bcrypt}" + PASSWORD_ENCODER.encode("password")), true, "bill@gmail.com", true, true, false);
            userRepo.save(user);

            var shift = new Shift(UUID.randomUUID().toString(), date, id);
            shift.getShiftDay(DayOfWeek.MONDAY).setAfternoon(false);
            shiftRepo.save(shift);

            shift = new Shift(UUID.randomUUID().toString(), date.minusWeeks(1), id);
            shiftRepo.save(shift);

            shift = new Shift(UUID.randomUUID().toString(), date.plusWeeks(1), id);
            shiftRepo.save(shift);

            var holiday = new Holiday(UUID.randomUUID().toString(), LocalDate.of(2022, 5, 19), true, true, id);
            holidayRepository.save(holiday);
        }

        {
            var id = UUID.randomUUID().toString();
            var user = new User(id, "anne", UserRole.WORKER, "Anne Boleyn", ("{bcrypt}" + PASSWORD_ENCODER.encode("password")), true, "anne@gmail.com", true, false, false);
            userRepo.save(user);

            var VolunteerDay = new VolunteerDay(UUID.randomUUID().toString(), date, true, true, true, id);
            volunteerDayRepository.save(VolunteerDay);
        }
    }

    public void checkRosta() {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        Rosta rosta = buildRosta(nextMonday);

        LOGGER.info("checking rosta for {}", nextMonday);

        List<MissingCover> missingCover = new ArrayList<>();

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            RostaDay rostaDay = rosta.getRostaDay(dayOfWeek);

            for (PartOfDay partOfDay : new PartOfDay[] { PartOfDay.MORNING, PartOfDay.AFTERNOON }) {
                Set<String> userUuids = rostaDay.getUserUuids(partOfDay);

                int count = userUuids.size();
                boolean keyholder = userUuids.stream().map(uuid -> userRepo.findById(uuid)).flatMap(Optional::stream).filter(User::isKeyholder).findFirst().isPresent();

                if ((count < MIN_COVER_COUNT) || !keyholder) {
                    missingCover.add(new MissingCover(dayOfWeek, partOfDay, count, keyholder));
                }
            }
        }

        LOGGER.debug("missingCover = {}", missingCover);

        if (!missingCover.isEmpty()) {
            LOGGER.info("found missing cover in rota, sending emails to all volunteers and informing management");

            String missingCoverStr = missingCover.stream().map(MissingCover::toString).collect(Collectors.joining("\n"));
            List<String> notified = new ArrayList<>();

            sendVolunteerEmail(nextMonday, missingCoverStr, notified);

            sendManagementEmail(nextMonday, missingCoverStr, notified);
        } else {
            LOGGER.info("the rota is covered, sending emails to management only");

            sendManagementOkEmail(nextMonday);
        }
    }

    private void sendManagementOkEmail(LocalDate date) {
        try (InputStream inputStream = resourceLoader.getResource(MANAGEMENT_OK_TEMPLATE).getInputStream()) {
            String templateStr = new String(FileCopyUtils.copyToByteArray(inputStream), StandardCharsets.UTF_8);

            String subject = "Shop Rota Sitrep (OK) - Week of " + date.format(FORMATTER);

            userRepo.findAllByUserRole(new UserRole[] { UserRole.MANAGER }).forEach(user -> {
                if (user.isNotifications()) {
                    String text = String.format(templateStr, firstName(user.getName()));

                    emailService.sendSimpleMessage(user.getEmail(), subject, text);
                }
            });

            for (String directorEmail : checkRostaDirectorEmail.split(",")) {
                String text = String.format(templateStr, nameFromEmail(directorEmail));

                emailService.sendSimpleMessage(directorEmail.trim(), subject, text);
            }
        } catch (IOException e) {
            LOGGER.error("failed to read email template from classpath resources", e);
        }
    }

    private void sendManagementEmail(LocalDate date, String missingCoverStr, List<String> notified) {
        try (InputStream inputStream = resourceLoader.getResource(MANAGEMENT_TEMPLATE).getInputStream()) {
            String templateStr = new String(FileCopyUtils.copyToByteArray(inputStream), StandardCharsets.UTF_8);

            String subject = "Shop Rota Sitrep - Week of " + date.format(FORMATTER);
            String notifiedStr = notified.stream().collect(Collectors.joining(", "));

            userRepo.findAllByUserRole(new UserRole[] { UserRole.MANAGER }).forEach(user -> {
                if (user.isNotifications()) {
                    String text = String.format(templateStr, firstName(user.getName()), notifiedStr) + missingCoverStr;

                    emailService.sendSimpleMessage(user.getEmail(), subject, text);
                }
            });

            for (String directorEmail : checkRostaDirectorEmail.split(",")) {
                String text = String.format(templateStr, nameFromEmail(directorEmail), notifiedStr) + missingCoverStr;

                emailService.sendSimpleMessage(directorEmail.trim(), subject, text);
            }
        } catch (IOException e) {
            LOGGER.error("failed to read email template from classpath resources", e);
        }
    }

    private void sendVolunteerEmail(LocalDate date, String missingCoverStr, List<String> notified) {
        try (InputStream inputStream = resourceLoader.getResource(VOLUNTEER_TEMPLATE).getInputStream()) {
            String templateStr = new String(FileCopyUtils.copyToByteArray(inputStream), StandardCharsets.UTF_8);

            String subject = "Request for Shop Volunteers - Week of " + date.format(FORMATTER);

            userRepo.findAllByUserRoleAndEmployee(new UserRole[] { UserRole.SUPERVISOR, UserRole.WORKER }, false).forEach(user -> {
                if (user.isNotifications()) {
                    String text = String.format(templateStr, firstName(user.getName())) + missingCoverStr;

                    emailService.sendSimpleMessage(user.getEmail(), subject, text);

                    notified.add(user.getName());
                }
            });
        } catch (IOException e) {
            LOGGER.error("failed to read email template from classpath resources", e);
        }
    }

    public Rosta buildRosta(LocalDate date) {
        assert date.getDayOfWeek() == DayOfWeek.MONDAY;

        LocalDate rostaStartDate = date;
        LocalDate rostaEndDate = date.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

        LOGGER.debug("rostaStartDate = {}", rostaStartDate);
        LOGGER.debug("rostaEndDate = {}", rostaEndDate);

        Rosta rosta = new Rosta(rostaStartDate);

        userRepo.findAll().forEach(user -> {
            LOGGER.debug("user: {}", user);

            String userUuid = user.getUuid();

            if (user.isEmployee()) {
                getShiftForUser(userUuid, rostaEndDate).ifPresent(shift -> {
                    LOGGER.debug("shift: {}", shift);

                    shift.getShiftDayIterator().forEachRemaining(shiftDay -> {
                        RostaDay rostaDay = rosta.getRostaDay(shiftDay.getDayOfWeek());
                        rostaDay.addUserUuid(shiftDay, userUuid);
                    });
                });

                holidayRepository.findAllByUserUuidAndDateBetween(userUuid, rostaStartDate, rostaEndDate).forEach(holiday -> {
                    LOGGER.debug("holiday: {}", holiday);

                    RostaDay rostaDay = rosta.getRostaDay(holiday.getDate().getDayOfWeek());
                    rostaDay.removeUserUuid(holiday, userUuid);
                });

                absenceDayRepository.findAllByUserUuidAndDateBetween(userUuid, rostaStartDate, rostaEndDate).forEach(absenceDay -> {
                    LOGGER.debug("absenceDay: {}", absenceDay);

                    RostaDay rostaDay = rosta.getRostaDay(absenceDay.getDate().getDayOfWeek());
                    rostaDay.removeUserUuid(absenceDay, userUuid);
                });
            } else {
                volunteerDayRepository.findAllByUserUuidAndDateBetween(userUuid, rostaStartDate, rostaEndDate).forEach(volunteerDay -> {
                    LOGGER.debug("volunteerDay: {}", volunteerDay);

                    RostaDay rostaDay = rosta.getRostaDay(volunteerDay.getDate().getDayOfWeek());
                    rostaDay.addUserUuid(volunteerDay, userUuid);
                });
            }
        });

        LOGGER.debug("rosta: {}", rosta);

        return rosta;
    }

    public Optional<Shift> getShiftForUser(String userUuid, LocalDate date) {
        return shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(userUuid, date);
    }

    public Optional<Shift> getShift(String shiftUuid) {
        return shiftRepo.findById(shiftUuid);
    }

    public float calculateShiftWork(User user, LocalDate start, LocalDate end) {
        class MutableFloat {
            private float number;

            public float get() {
                return number;
            }

            public void add(float other) {
                number += other;
            }
        }

        MutableFloat work = new MutableFloat();

        LocalDate sunday = start.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        if (start.getDayOfWeek() != DayOfWeek.MONDAY) {
            getShiftForUser(user.getUuid(), sunday).ifPresent(shift -> {
                for (DayOfWeek day : getDayOfWeekRange(start.getDayOfWeek(), DayOfWeek.SUNDAY)) {
                    work.add(shift.getShiftDay(day).getPartCount());
                }
            });

            sunday = sunday.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        }

        LocalDate lastSunday = end.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        while (!sunday.isAfter(lastSunday)) {
            getShiftForUser(user.getUuid(), sunday).ifPresent(shift -> {
                work.add(shift.getPartCount());
            });

            sunday = sunday.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        }

        if (end.getDayOfWeek() != DayOfWeek.SUNDAY) {
            getShiftForUser(user.getUuid(), sunday).ifPresent(shift -> {
                for (DayOfWeek day : getDayOfWeekRange(DayOfWeek.MONDAY, end.getDayOfWeek())) {
                    work.add(shift.getShiftDay(day).getPartCount());
                }
            });
        }

        return work.get();
    }

    public void writeRosta(Rosta rosta, File outputFile) throws FileNotFoundException, IOException {
        try (InputStream is = resourceLoader.getResource(ROSTA_TEMPLATE_DOCX).getInputStream(); XWPFDocument document = new XWPFDocument(is); FileOutputStream out = new FileOutputStream(outputFile)) {
            for (XWPFParagraph para : document.getParagraphs()) {
                performParagraphInsertions(rosta, para);
            }

            for (XWPFTable tbl : document.getTables()) {
                for (XWPFTableRow row : tbl.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph para : cell.getParagraphs()) {
                            performParagraphInsertions(rosta, para);
                        }
                    }
                }
            }

            document.write(out);
        }
    }

    private void performParagraphInsertions(Rosta rosta, XWPFParagraph para) {
        String startDate = rosta.getRostaDate().format(FORMATTER);
        String endDate = rosta.getRostaDate().with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).format(FORMATTER);

        insertAtBookmark(para, "startDate", startDate);
        insertAtBookmark(para, "endDate", endDate);

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            RostaDay rostaDay = rosta.getRostaDay(dayOfWeek);

            for (PartOfDay partOfDay : PartOfDay.values()) {
                User[] users = getUsers(rostaDay, partOfDay);

                for (int i = 0; i < users.length; i++) {
                    String bookmark = String.format("%s%s%d", dayOfWeek.name().toLowerCase(), partOfDay.toString(), (i + 1));

                    insertAtBookmark(para, bookmark, users[i].getDisplayName());
                }
            }
        }
    }

    public User[] getUsers(RostaDay rostaDay, PartOfDay partOfDay) {
        return rostaDay.getUserUuids(partOfDay).stream().map(userUuid -> userRepo.findById(userUuid)).flatMap(Optional::stream).sorted().toArray(size -> new User[size]);
    }

    public List<AbstractDay> getDays(User user, Set<DayType> dayTypes, LocalDate dateStart, LocalDate dateEnd) {
        List<AbstractDay> result = new ArrayList<>();

        if (dayTypes.contains(DayType.ABSENCE)) {
            result.addAll(absenceDayRepository.findAllByUserUuidAndDateBetween(user.getUuid(), dateStart, dateEnd));
        }
        if (dayTypes.contains(DayType.HOLIDAY)) {
            result.addAll(holidayRepository.findAllByUserUuidAndDateBetween(user.getUuid(), dateStart, dateEnd));
        }
        if (dayTypes.contains(DayType.VOLUNTARY)) {
            result.addAll(volunteerDayRepository.findAllByUserUuidAndDateBetween(user.getUuid(), dateStart, dateEnd));
        }

        return result;
    }

    public List<AbstractDay> getDays(Set<DayType> dayTypes, LocalDate dateStart, LocalDate dateEnd) {
        List<AbstractDay> result = new ArrayList<>();

        if (dayTypes.contains(DayType.ABSENCE)) {
            result.addAll(absenceDayRepository.findAllByDateBetween(dateStart, dateEnd));
        }
        if (dayTypes.contains(DayType.HOLIDAY)) {
            result.addAll(holidayRepository.findAllByDateBetween(dateStart, dateEnd));
        }
        if (dayTypes.contains(DayType.VOLUNTARY)) {
            result.addAll(volunteerDayRepository.findAllByDateBetween(dateStart, dateEnd));
        }

        return result;
    }

    public boolean saveDay(AbstractDay day, String bookerUuid) {
        boolean result = false;

        if (day instanceof Holiday holiday) {
            if (result = (checkConflict(holidayRepository, holiday) && checkConflict(absenceDayRepository, holiday))) {
                holidayRepository.save(holiday);
            }
        } else if (day instanceof AbsenceDay absenceDay) {
            if (result = (checkConflict(absenceDayRepository, absenceDay) && checkConflict(holidayRepository, absenceDay))) {
                absenceDayRepository.save(absenceDay);
            }
        } else if (day instanceof VolunteerDay volunteerDay) {
            if (result = checkConflict(volunteerDayRepository, volunteerDay)) {
                volunteerDayRepository.save(volunteerDay);
            }
        } else {
            throw new IllegalStateException();
        }

        sendImmediateEmail("ADD", day, bookerUuid);

        return result;
    }

    private static <U extends AbstractDay, V extends AbstractDay> boolean checkConflict(AbstractDayRepository<V> repository, U day) {
        boolean result = true;

        List<V> allOthers = repository.findAllByUserUuidAndDate(day.getUserUuid(), day.getDate());

        for (V other : allOthers) {
            if (day.overlapsWith(other)) {
                result = false;
            }
        }

        return result;
    }

    public void removeDay(String uuid, String bookerUuid) {
        holidayRepository.findById(uuid).ifPresent(day -> {
            holidayRepository.deleteById(uuid);

            sendImmediateEmail("REMOVE", day, bookerUuid);
        });

        absenceDayRepository.findById(uuid).ifPresent(day -> {
            absenceDayRepository.deleteById(uuid);

            sendImmediateEmail("REMOVE", day, bookerUuid);
        });

        volunteerDayRepository.findById(uuid).ifPresent(day -> {
            volunteerDayRepository.deleteById(uuid);

            sendImmediateEmail("REMOVE", day, bookerUuid);
        });
    }

    private void sendImmediateEmail(String changeType, AbstractDay day, String bookerUuid) {
        if (Objects.equals(day.getUserUuid(), bookerUuid)) {
            LocalDate date = day.getDate();

            LocalDate today = LocalDate.now();
            LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            if (!date.isBefore(monday) && !date.isAfter(sunday)) {
                LOGGER.info("sending immediate notification email for {}", day);

                userRepo.findById(day.getUserUuid()).ifPresent(bookee -> {
                    DayType dayType = AbstractDay.getDayType(day.getClass().getCanonicalName());
                    DayOfWeek dayOfWeek = day.getDate().getDayOfWeek();
                    String title = org.pjp.rosta.model.PartOfDay.getTitle(day);
                    String bookeeName = bookee.getName();

                    try (InputStream inputStream = resourceLoader.getResource(IMMEDIATE_TEMPLATE).getInputStream()) {
                        String templateStr = new String(FileCopyUtils.copyToByteArray(inputStream), StandardCharsets.UTF_8);

                        String subject = "Shop Rota Change - Week of " + monday.format(FORMATTER);

                        userRepo.findAllByUserRole(new UserRole[] { UserRole.SUPERVISOR, UserRole.MANAGER }).forEach(user -> {
                            if (user.isNotifications()) {
                                String text = String.format(templateStr, firstName(user.getName()),
                                        changeType, dayType, dayOfWeek, title, bookeeName);

                                emailService.sendSimpleMessage(user.getEmail(), subject, text);
                            }
                        });
                    } catch (IOException e) {
                        LOGGER.error("failed to read email template from classpath resources", e);
                    }
                });
            }
        }
    }

    public void saveShift(Shift shift) {
        shiftRepo.save(shift);
    }

    public void deleteUser(User user) {
        if (user.isEmployee()) {
            List<String> uuids = holidayRepository.findAllByUserUuid(user.getUuid()).stream().map(AbstractDay::getUuid).collect(Collectors.toList());
            holidayRepository.deleteAllById(uuids);

            uuids = absenceDayRepository.findAllByUserUuid(user.getUuid()).stream().map(AbstractDay::getUuid).collect(Collectors.toList());
            absenceDayRepository.deleteAllById(uuids);

            uuids = shiftRepo.findAllByUserUuid(user.getUuid()).stream().map(Shift::getUuid).collect(Collectors.toList());
            shiftRepo.deleteAllById(uuids);;

        } else {
            List<String> uuids = volunteerDayRepository.findAllByUserUuid(user.getUuid()).stream().map(AbstractDay::getUuid).collect(Collectors.toList());
            volunteerDayRepository.deleteAllById(uuids);
        }

        userRepo.delete(user);
    }

    public void sendTestEmail() {
        if (!"none".equals(testEmailTo)) {
            LOGGER.info("attempting to send test email...");

            try (InputStream inputStream = resourceLoader.getResource(TEST_TEMPLATE).getInputStream()) {
                String templateStr = new String(FileCopyUtils.copyToByteArray(inputStream), StandardCharsets.UTF_8);

                emailService.sendSimpleMessage(testEmailTo, "Test Email", templateStr);
            } catch (IOException e) {
                LOGGER.error("failed to read email template from classpath resources", e);
            }
        }
    }
}
