package org.pjp.rosta.service;

import java.io.File;
import java.io.FileInputStream;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
import org.pjp.rosta.model.VolunteerDay;
import org.pjp.rosta.repository.AbsenceDayRepository;
import org.pjp.rosta.repository.HolidayRepository;
import org.pjp.rosta.repository.ShiftRepository;
import org.pjp.rosta.repository.UserRepository;
import org.pjp.rosta.repository.VolunteerDayRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

@Service
public class RostaService {

    private static final String TEMPLATE = "classpath:email-template.txt";

    private static final Logger LOGGER = LoggerFactory.getLogger(RostaService.class);

    private static final String TEMPLATE_DOCX = "data/rosta-template-2.docx";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    private record MissingCover(DayOfWeek dayOfWeek, PartOfDay partOfDay) {
        @Override
        public String toString() {
            return dayOfWeek + " " + partOfDay;
        }
    }

    @Value("${check.rosta.include.opener}")
    private boolean checkRostaIncludeOpener;

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

    public RostaService() {
        super();
    }

    public void initData() {
        userRepo.deleteAll();
        shiftRepo.deleteAll();
        volunteerDayRepository.deleteAll();

        LocalDate date = LocalDate.of(2022, 5, 16);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        {
            String id = UUID.randomUUID().toString();
            User user = new User(id, "admin", true, "Admin", "{bcrypt}" + passwordEncoder.encode("password"), true, "admin@gmail.com", false, false);
            userRepo.save(user);
        }

        {
            var id = UUID.randomUUID().toString();
            var user = new User(id, "fred", false, "Fred Bloggs", "{bcrypt}" + passwordEncoder.encode("password"), true, "fred@gmail.com", true, true);
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
            var user = new User(id, "bill", false, "Bill Smith", "{bcrypt}" + passwordEncoder.encode("password"), false, "bill@gmail.com", true, true);
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
            var user = new User(id, "anne", false, "Anne Boleyn", "{bcrypt}" + passwordEncoder.encode("password"), true, "anne@gmail.com", true, false);
            userRepo.save(user);

            var VolunteerDay = new VolunteerDay(UUID.randomUUID().toString(), date, true, true, id);
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

            if (checkRostaIncludeOpener) {
                if (rostaDay.getUserUuids(PartOfDay.OPENER).size() == 0) {
                    missingCover.add(new MissingCover(dayOfWeek, PartOfDay.OPENER));
                }
            }

            for (PartOfDay partOfDay : new PartOfDay[] { PartOfDay.MORNING, PartOfDay.AFTERNOON }) {
                if (rostaDay.getUserUuids(partOfDay).size() < 2) {
                    missingCover.add(new MissingCover(dayOfWeek, partOfDay));
                }
            }
        }

        LOGGER.debug("missingCover = {}", missingCover);

        if (!missingCover.isEmpty()) {
            LOGGER.info("found missing cover in rota, sending emails to all volunteers");

            Resource resource = resourceLoader.getResource(TEMPLATE);

            try (InputStream inputStream = resource.getInputStream()) {
                String templateStr = new String(FileCopyUtils.copyToByteArray(inputStream), StandardCharsets.UTF_8);
                String missingCoverStr = missingCover.stream().map(mc -> mc.toString()).collect(Collectors.joining("\n"));

                String subject = "Request for Shop Volunteers - Week of " + nextMonday.format(FORMATTER);

                userRepo.findByEmployee(false).forEach(user -> {
                    if (user.isNotifications()) {
                        String text = String.format(templateStr, user.getName()) + missingCoverStr;

                        try {
                            emailService.sendSimpleMessage(user.getEmail(), subject, text);
                        } catch (Exception e) {
                            LOGGER.warn("failed to send email to address "+ user.getEmail());
                        }
                    }
                });
            } catch (IOException e) {
                LOGGER.error("failed to read email-template.txt from classpath resources", e);
            }
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

        return rosta;
    }

    public Optional<Shift> getShiftForUser(String userUuid, LocalDate date) {
        return shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(userUuid, date);
    }

    public Optional<Shift> getShift(String shiftUuid) {
        return shiftRepo.findById(shiftUuid);
    }

    public void writeRosta(Rosta rosta, File outputFile) throws FileNotFoundException, IOException {
        try (FileInputStream is = new FileInputStream(TEMPLATE_DOCX); XWPFDocument document = new XWPFDocument(is); FileOutputStream out = new FileOutputStream(outputFile)) {
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

                    insertAtBookmark(para, bookmark, users[i].getName());
                }
            }
        }
    }

    private void insertAtBookmark(XWPFParagraph para, String bookmarkName, String insertion) {
        for (CTBookmark bookmark : para.getCTP().getBookmarkStartList()) {
            if (bookmarkName.equals(bookmark.getName())) {
                XWPFRun newRun = para.insertNewRun(0);
                newRun.setText(insertion);
            }
        }
    }

    @SuppressWarnings("unused")
    private void findAndReplace(XWPFParagraph para, String search, String replacement) {
        for (XWPFRun run : para.getRuns()) {
            String text = run.getText(0);

            if ((text != null) && text.contains(search)) {
                text = text.replace(search, replacement);
                run.setText(text, 0);
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

    public void saveDay(AbstractDay day) {
        if (day instanceof Holiday holiday) {
            holidayRepository.save(holiday);
        } else if (day instanceof AbsenceDay absenceDay) {
            absenceDayRepository.save(absenceDay);
        } else if (day instanceof VolunteerDay volunteerDay) {
            volunteerDayRepository.save(volunteerDay);
        } else {
            throw new IllegalStateException();
        }
    }

    public void removeDay(String uuid) {
        volunteerDayRepository.deleteById(uuid);
        holidayRepository.deleteById(uuid);
    }

    public void saveShift(Shift shift) {
        shiftRepo.save(shift);
    }
}
