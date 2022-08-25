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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import org.pjp.rosta.bean.Rota;
import org.pjp.rosta.bean.RotaDay;
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
import org.pjp.rosta.util.UuidStr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.concurrent.ListenableFuture;

@Service
public class RotaService {

    public record MissingCover(DayOfWeek dayOfWeek, PartOfDay partOfDay, int count, boolean keyholder) {
        @Override
        public String toString() {
            return String.format("%-9s %-9s %d %s", dayOfWeek, partOfDay, count, (keyholder ? "" : "(no key-holder)"));
        }
    }

    private static final int MIN_COVER_COUNT = 2;

    private static final String VOLUNTEER_TEMPLATE = "file:template/volunteer-template.txt";

    private static final String MANAGEMENT_TEMPLATE = "file:template/management-template.txt";

    private static final String MANAGEMENT_OK_TEMPLATE = "file:template/management-ok-template.txt";

    private static final String IMMEDIATE_TEMPLATE = "file:template/immediate-template.txt";

    private static final String IMMEDIATE_SHIFT_TEMPLATE = "file:template/immediate-shift-template.txt";

    private static final String TEST_TEMPLATE = "file:template/test-template.txt";

    private static final Logger LOGGER = LoggerFactory.getLogger(RotaService.class);

    private static final String ROTA_TEMPLATE_DOCX = "file:template/rota-template.docx";

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

    @Value("${init.data:false}")
    private boolean initData;

    @Value("${check.rota.director.email}")
    private String checkRotaDirectorEmail;

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
            String id = UuidStr.random();
            User user = new User(id, "manager", UserRole.MANAGER, "Manager", ("{bcrypt}" + PASSWORD_ENCODER.encode("password")), true, "manager@gmail.com", true, true, false);
            userRepo.save(user);
        }

        LocalDate date = LocalDate.of(2022, 5, 16);

        {
            var id = UuidStr.random();
            var user = new User(id, "fred", UserRole.SUPERVISOR, "Fred Bloggs", ("{bcrypt}" + PASSWORD_ENCODER.encode("password")), true, "fred.bloggs@gmail.com", true, true, true);
            userRepo.save(user);

            var shift = new Shift(UuidStr.random(), date, id);
            shift.getShiftDay(DayOfWeek.MONDAY).setMorning(false);
            shiftRepo.save(shift);

            shift = new Shift(UuidStr.random(), date.minusWeeks(1), id);
            shiftRepo.save(shift);

            shift = new Shift(UuidStr.random(), date.plusWeeks(1), id);
            shiftRepo.save(shift);

            var holiday = new Holiday(UuidStr.random(), LocalDate.of(2022, 5, 18), true, true, id);
            holidayRepository.save(holiday);
        }

        {
            var id = UuidStr.random();
            var user = new User(id, "bill", UserRole.WORKER, "Bill Smith", ("{bcrypt}" + PASSWORD_ENCODER.encode("password")), true, "bill.smith@gmail.com", true, true, false);
            userRepo.save(user);

            var shift = new Shift(UuidStr.random(), date, id);
            shift.getShiftDay(DayOfWeek.MONDAY).setAfternoon(false);
            shiftRepo.save(shift);

            shift = new Shift(UuidStr.random(), date.minusWeeks(1), id);
            shiftRepo.save(shift);

            shift = new Shift(UuidStr.random(), date.plusWeeks(1), id);
            shiftRepo.save(shift);

            var holiday = new Holiday(UuidStr.random(), LocalDate.of(2022, 5, 19), true, true, id);
            holidayRepository.save(holiday);
        }

        {
            var id = UuidStr.random();
            var user = new User(id, "anne", UserRole.WORKER, "Anne Boleyn", ("{bcrypt}" + PASSWORD_ENCODER.encode("password")), true, "anne.boleyn@gmail.com", true, false, true);
            userRepo.save(user);

            var VolunteerDay = new VolunteerDay(UuidStr.random(), date, true, true, true, id);
            volunteerDayRepository.save(VolunteerDay);
        }

        {
            var id = UuidStr.random();
            var user = new User(id, "jane", UserRole.WORKER, "Jane Seymour", ("{bcrypt}" + PASSWORD_ENCODER.encode("password")), true, "jane.seymour@gmail.com", true, false, false);
            userRepo.save(user);

            var VolunteerDay = new VolunteerDay(UuidStr.random(), date, true, true, true, id);
            volunteerDayRepository.save(VolunteerDay);
        }
    }

    public void checkRotaAndNotify() {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        List<MissingCover> missingCover = checkRota(nextMonday, MIN_COVER_COUNT);

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

    @Async
    public ListenableFuture<Map<DayOfWeek, MissingCover[]>> checkRota(LocalDate date) {
        Map<DayOfWeek, MissingCover[]> result = new HashMap<>();

        for (MissingCover missingCover : checkRota(date, MIN_COVER_COUNT)) {
            if (result.containsKey(missingCover.dayOfWeek)) {
                MissingCover temp = result.get(missingCover.dayOfWeek)[0];
                result.put(missingCover.dayOfWeek, new MissingCover[] { temp, missingCover });
            } else {
                result.put(missingCover.dayOfWeek, new MissingCover[] { missingCover });
            }
        }

        return AsyncResult.forValue(result);
    }

    private List<MissingCover> checkRota(LocalDate date, int minCoverCount) {
        Rota rota = buildRota(date);

        LOGGER.info("checking rota for {}", date);

        List<MissingCover> missingCover = new ArrayList<>();

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            RotaDay rotaDay = rota.getRotaDay(dayOfWeek);

            for (PartOfDay partOfDay : new PartOfDay[] { PartOfDay.MORNING, PartOfDay.AFTERNOON }) {
                Set<String> userUuids = rotaDay.getUserUuids(partOfDay);

                int count = userUuids.size();
                boolean keyholder = userUuids.stream().map(uuid -> userRepo.findById(uuid)).flatMap(Optional::stream).filter(User::isKeyholder).findFirst().isPresent();

                if ((count < minCoverCount) || !keyholder) {
                    missingCover.add(new MissingCover(dayOfWeek, partOfDay, count, keyholder));
                }
            }
        }

        return missingCover;
    }

    private void sendManagementOkEmail(LocalDate date) {
        try (InputStream inputStream = resourceLoader.getResource(MANAGEMENT_OK_TEMPLATE).getInputStream()) {
            String templateStr = new String(FileCopyUtils.copyToByteArray(inputStream), StandardCharsets.UTF_8);

            String subject = "Shop Rota Sitrep (OK) - Week of " + date.format(FORMATTER);

            userRepo.findAllByUserRole(new UserRole[] { UserRole.MANAGER }).stream().filter(User::isNotifications).forEach(mgr -> {
                String text = String.format(templateStr, firstName(mgr.getName()));

                emailService.sendSimpleMessage(mgr.getEmail(), subject, text);
            });

            for (String directorEmail : checkRotaDirectorEmail.split(",")) {
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

            userRepo.findAllByUserRole(new UserRole[] { UserRole.MANAGER }).stream().filter(User::isNotifications).forEach(mgr -> {
                String text = String.format(templateStr, firstName(mgr.getName()), notifiedStr) + missingCoverStr;

                emailService.sendSimpleMessage(mgr.getEmail(), subject, text);
            });

            for (String directorEmail : checkRotaDirectorEmail.split(",")) {
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

            userRepo.findAllByUserRoleAndEmployee(new UserRole[] { UserRole.SUPERVISOR, UserRole.WORKER }, false).stream().filter(User::isNotifications).forEach(supMgr -> {
                String text = String.format(templateStr, firstName(supMgr.getName())) + missingCoverStr;

                emailService.sendSimpleMessage(supMgr.getEmail(), subject, text);

                notified.add(supMgr.getName());
            });
        } catch (IOException e) {
            LOGGER.error("failed to read email template from classpath resources", e);
        }
    }

    public Rota buildRota(LocalDate date) {
        assert date.getDayOfWeek() == DayOfWeek.MONDAY;

        LocalDate rotaStartDate = date;
        LocalDate rotaEndDate = date.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

        LOGGER.debug("rotaStartDate = {}", rotaStartDate);
        LOGGER.debug("rotaEndDate = {}", rotaEndDate);

        Rota rota = new Rota(rotaStartDate);

        userRepo.findAll().forEach(user -> {
            LOGGER.debug("user: {}", user);

            String userUuid = user.getUuid();

            if (user.isEmployee()) {
                getShiftForUser(userUuid, rotaEndDate).ifPresent(shift -> {
                    LOGGER.debug("shift: {}", shift);

                    shift.getShiftDayIterator().forEachRemaining(shiftDay -> {
                        RotaDay rotaDay = rota.getRotaDay(shiftDay.getDayOfWeek());
                        rotaDay.addUserUuid(shiftDay, userUuid);
                    });
                });

                holidayRepository.findAllByUserUuidAndDateBetween(userUuid, rotaStartDate, rotaEndDate).forEach(holiday -> {
                    LOGGER.debug("holiday: {}", holiday);

                    RotaDay rotaDay = rota.getRotaDay(holiday.getDate().getDayOfWeek());
                    rotaDay.removeUserUuid(holiday, userUuid);
                });

                absenceDayRepository.findAllByUserUuidAndDateBetween(userUuid, rotaStartDate, rotaEndDate).forEach(absenceDay -> {
                    LOGGER.debug("absenceDay: {}", absenceDay);

                    RotaDay rotaDay = rota.getRotaDay(absenceDay.getDate().getDayOfWeek());
                    rotaDay.removeUserUuid(absenceDay, userUuid);
                });
            } else {
                volunteerDayRepository.findAllByUserUuidAndDateBetween(userUuid, rotaStartDate, rotaEndDate).forEach(volunteerDay -> {
                    LOGGER.debug("volunteerDay: {}", volunteerDay);

                    RotaDay rotaDay = rota.getRotaDay(volunteerDay.getDate().getDayOfWeek());
                    rotaDay.addUserUuid(volunteerDay, userUuid);
                });
            }
        });

        LOGGER.debug("rota: {}", rota);

        return rota;
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

    public void writeRota(Rota rota, File outputFile) throws FileNotFoundException, IOException {
        try (InputStream is = resourceLoader.getResource(ROTA_TEMPLATE_DOCX).getInputStream(); XWPFDocument document = new XWPFDocument(is); FileOutputStream out = new FileOutputStream(outputFile)) {
            for (XWPFParagraph para : document.getParagraphs()) {
                performParagraphInsertions(rota, para);
            }

            for (XWPFTable tbl : document.getTables()) {
                for (XWPFTableRow row : tbl.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph para : cell.getParagraphs()) {
                            performParagraphInsertions(rota, para);
                        }
                    }
                }
            }

            document.write(out);
        }
    }

    private void performParagraphInsertions(Rota rota, XWPFParagraph para) {
        String startDate = rota.getRotaDate().format(FORMATTER);
        String endDate = rota.getRotaDate().with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).format(FORMATTER);

        insertAtBookmark(para, "startDate", startDate);
        insertAtBookmark(para, "endDate", endDate);

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            RotaDay rotaDay = rota.getRotaDay(dayOfWeek);

            for (PartOfDay partOfDay : PartOfDay.values()) {
                User[] users = getUsers(rotaDay, partOfDay);

                for (int i = 0; i < users.length; i++) {
                    String bookmark = String.format("%s%s%d", dayOfWeek.name().toLowerCase(), partOfDay.toString(), (i + 1));

                    insertAtBookmark(para, bookmark, users[i].getDisplayName());
                }
            }
        }
    }

    public User[] getUsers(RotaDay rotaDay, PartOfDay partOfDay) {
        return rotaDay.getUserUuids(partOfDay).stream().map(userUuid -> userRepo.findById(userUuid)).flatMap(Optional::stream).sorted().toArray(size -> new User[size]);
    }

    public List<AbstractDay> getDays(User user, Set<DayType> dayTypes, LocalDate dateStart, LocalDate dateEnd) {
        List<AbstractDay> result = new ArrayList<>();

        if (dayTypes.contains(DayType.ABSENCE)) {
            result.addAll(absenceDayRepository.findAllByUserUuidAndDateBetween(user.getUuid(), dateStart, dateEnd));
        }
        if (dayTypes.contains(DayType.HOLIDAY)) {
            result.addAll(holidayRepository.findAllByUserUuidAndDateBetween(user.getUuid(), dateStart, dateEnd));
        }
        if (dayTypes.contains(DayType.VOLUNTEER)) {
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

        if (dayTypes.contains(DayType.VOLUNTEER)) {
            result.addAll(volunteerDayRepository.findAllByDateBetween(dateStart, dateEnd));
        }

        return result;
    }

    public LocalDate saveDays(List<AbstractDay> days, String bookerUuid) {
        LocalDate result = null;

        for (AbstractDay day : days) {
            if (day instanceof Holiday holiday) {
                if (hasConflict(holidayRepository, holiday) || hasConflict(absenceDayRepository, holiday)) {
                    result = day.getDate();
                    break;
                }
            } else if (day instanceof AbsenceDay absenceDay) {
                if (hasConflict(absenceDayRepository, absenceDay) || hasConflict(holidayRepository, absenceDay)) {
                    result = day.getDate();
                    break;
                }
            } else if (day instanceof VolunteerDay volunteerDay) {
                if (hasConflict(volunteerDayRepository, volunteerDay)) {
                    result = day.getDate();
                    break;
                }
            }
        }

        if (result == null) {
            for (AbstractDay day : days) {
                if (day instanceof Holiday holiday) {
                    holidayRepository.save(holiday);
                } else if (day instanceof AbsenceDay absenceDay) {
                    absenceDayRepository.save(absenceDay);
                } else if (day instanceof VolunteerDay volunteerDay) {
                    volunteerDayRepository.save(volunteerDay);
                }

                // note this only sends for those days which fall in the current week
                sendImmediateEmail("ADD", day, bookerUuid);
            }
        }


        return result;
    }

    private static <U extends AbstractDay, V extends AbstractDay> boolean hasConflict(AbstractDayRepository<V> repository, U day) {
        boolean result = false;

        List<V> others = repository.findAllByUserUuidAndDate(day.getUserUuid(), day.getDate());

        for (V other : others) {
            if (day.overlapsWith(other)) {
                result = true;
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

    public void removeDays(String repeatUuid, String bookerUuid) {
        volunteerDayRepository.findAllByRepeatUuid(repeatUuid).forEach(day -> {
            volunteerDayRepository.deleteById(day.getUuid());

            // note this only sends for those days which fall in the current week
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

                        userRepo.findAllByUserRole(new UserRole[] { UserRole.SUPERVISOR, UserRole.MANAGER }).stream().filter(User::isNotifications).forEach(supMgr -> {
                            String text = String.format(templateStr, firstName(supMgr.getName()), changeType, dayType, dayOfWeek, title, bookeeName);

                            emailService.sendSimpleMessage(supMgr.getEmail(), subject, text);
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

        sendImmediateShiftEmail(shift);
    }

    private void sendImmediateShiftEmail(Shift shift) {
        LocalDate fromDate = shift.getFromDate();

        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        if (!fromDate.isAfter(sunday)) {
            LOGGER.info("sending immediate (shift) notification email for {}", fromDate);

            userRepo.findById(shift.getUserUuid()).ifPresent(user -> {
                try (InputStream inputStream = resourceLoader.getResource(IMMEDIATE_SHIFT_TEMPLATE).getInputStream()) {
                    String templateStr = new String(FileCopyUtils.copyToByteArray(inputStream), StandardCharsets.UTF_8);

                    String subject = "Shop Rota (Shift) Change - Week of " + monday.format(FORMATTER);

                    userRepo.findAllByUserRole(new UserRole[] { UserRole.SUPERVISOR, UserRole.MANAGER }).stream().filter(User::isNotifications).forEach(supMgr -> {
                        String text = String.format(templateStr, firstName(supMgr.getName()), user, shift.toString());

                        emailService.sendSimpleMessage(supMgr.getEmail(), subject, text);
                    });
                } catch (IOException e) {
                    LOGGER.error("failed to read email template from classpath resources", e);
                }
            });
        }
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
