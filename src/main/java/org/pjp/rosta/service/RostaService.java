package org.pjp.rosta.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

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
import org.pjp.rosta.model.Holiday;
import org.pjp.rosta.model.Shift;
import org.pjp.rosta.model.User;
import org.pjp.rosta.model.VolunteerDay;
import org.pjp.rosta.repository.HolidayRepository;
import org.pjp.rosta.repository.ShiftRepository;
import org.pjp.rosta.repository.UserRepository;
import org.pjp.rosta.repository.VolunteerDayRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RostaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RostaService.class);

    private static final String TEMPLATE_DOCX = "data/rosta-template.docx";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ShiftRepository shiftRepo;

    @Autowired
    private VolunteerDayRepository volunteerDayRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    public void initData() {
        userRepo.deleteAll();
        shiftRepo.deleteAll();
        volunteerDayRepository.deleteAll();

        LocalDate date = LocalDate.of(2022, 5, 16);

        {
            String id = UUID.randomUUID().toString();
            User user = new User(id, "Fred", "fred@gmail.com", true);
            userRepo.save(user);

            Shift shift = new Shift(UUID.randomUUID().toString(), date, id);
            shift.getShiftDay(DayOfWeek.MONDAY).setMorning(false);
            shiftRepo.save(shift);

            shift = new Shift(UUID.randomUUID().toString(), date.minusWeeks(1), id);
            shiftRepo.save(shift);

            shift = new Shift(UUID.randomUUID().toString(), date.plusWeeks(1), id);
            shiftRepo.save(shift);

            Holiday holiday = new Holiday(UUID.randomUUID().toString(), LocalDate.of(2022, 5, 18), true, true, id);
            holidayRepository.save(holiday);
        }

        {
            String id = UUID.randomUUID().toString();
            User user = new User(id, "Bill", "bill@gmail.com", true);
            userRepo.save(user);

            Shift shift = new Shift(UUID.randomUUID().toString(), date, id);
            shift.getShiftDay(DayOfWeek.MONDAY).setAfternoon(false);
            shiftRepo.save(shift);

            shift = new Shift(UUID.randomUUID().toString(), date.minusWeeks(1), id);
            shiftRepo.save(shift);

            shift = new Shift(UUID.randomUUID().toString(), date.plusWeeks(1), id);
            shiftRepo.save(shift);

            Holiday holiday = new Holiday(UUID.randomUUID().toString(), LocalDate.of(2022, 5, 19), true, true, id);
            holidayRepository.save(holiday);
        }

        {
            String id = UUID.randomUUID().toString();
            User user = new User(id, "Anne", "anne@gmail.com", true);
            userRepo.save(user);

            VolunteerDay VolunteerDay = new VolunteerDay(UUID.randomUUID().toString(), date, true, true, id);
            volunteerDayRepository.save(VolunteerDay);
        }
    }

    public Rosta buildRosta(LocalDate date) {
        LocalDate rostaStartDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate rostaEndDate = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LOGGER.debug("rostaStartDate = {}", rostaStartDate);
        LOGGER.debug("rostaEndDate = {}", rostaEndDate);

        Rosta rosta = new Rosta(rostaStartDate);

        userRepo.findAll().forEach(user -> {
            LOGGER.debug("user: {}", user);

            String userUuid = user.getUuid();

            shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), rostaEndDate).ifPresent(shift -> {
                LOGGER.debug("shift: {}", shift);

                shift.getShiftDayIterator().forEachRemaining(shiftDay -> {
                    RostaDay rostaDay = rosta.getRostaDay(shiftDay.getDayOfWeek());
                    rostaDay.addUserUuid(shiftDay, userUuid);
                });
            });

            volunteerDayRepository.findAllByUserUuidAndDateBetween(userUuid, rostaStartDate, rostaEndDate).forEach(volunteerDay -> {
                LOGGER.debug("volunteerDay: {}", volunteerDay);

                RostaDay rostaDay = rosta.getRostaDay(volunteerDay.getDate().getDayOfWeek());
                rostaDay.addUserUuid(volunteerDay, userUuid);
            });


            holidayRepository.findAllByUserUuidAndDateBetween(userUuid, rostaStartDate, rostaEndDate).forEach(holiday -> {
                LOGGER.debug("holiday: {}", holiday);

                RostaDay rostaDay = rosta.getRostaDay(holiday.getDate().getDayOfWeek());
                rostaDay.removeUserUuid(holiday, userUuid);
            });
        });

        return rosta;
    }

    public void writeRosta(Rosta rosta, String outputFilename) throws FileNotFoundException, IOException {
        try (FileInputStream is = new FileInputStream(TEMPLATE_DOCX); XWPFDocument document = new XWPFDocument(is); FileOutputStream out = new FileOutputStream(outputFilename)) {
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
                String[] userUuids = rostaDay.getUserUuids(partOfDay);
                for (int i = 0; i < userUuids.length; i++) {
                    String bookmark = String.format("%s%s%d", dayOfWeek.name().toLowerCase(), partOfDay.toString(), (i + 1));

                    userRepo.findById(userUuids[i]).ifPresent(user -> {
                        insertAtBookmark(para, bookmark, user.getName());
                    });
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
}
