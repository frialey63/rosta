package org.pjp.rosta.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

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

        LocalDate day = LocalDate.of(2022, 5, 16);

        {
            String id = UUID.randomUUID().toString();
            User user = new User(id, "Fred", "fred@gmail.com", true);
            userRepo.save(user);

            Shift shift = new Shift(UUID.randomUUID().toString(), day, id);
            shift.getShiftDay(DayOfWeek.MONDAY).setMorning(false);
            shiftRepo.save(shift);

            shift = new Shift(UUID.randomUUID().toString(), day.minusWeeks(1), id);
            shiftRepo.save(shift);

            shift = new Shift(UUID.randomUUID().toString(), day.plusWeeks(1), id);
            shiftRepo.save(shift);

            Holiday holiday = new Holiday(UUID.randomUUID().toString(), LocalDate.of(2022, 5, 18), true, true, id);
            holidayRepository.save(holiday);
        }

        {
            String id = UUID.randomUUID().toString();
            User user = new User(id, "Bill", "bill@gmail.com", true);
            userRepo.save(user);

            Shift shift = new Shift(UUID.randomUUID().toString(), day, id);
            shift.getShiftDay(DayOfWeek.MONDAY).setAfternoon(false);
            shiftRepo.save(shift);

            shift = new Shift(UUID.randomUUID().toString(), day.minusWeeks(1), id);
            shiftRepo.save(shift);

            shift = new Shift(UUID.randomUUID().toString(), day.plusWeeks(1), id);
            shiftRepo.save(shift);

            Holiday holiday = new Holiday(UUID.randomUUID().toString(), LocalDate.of(2022, 5, 19), true, true, id);
            holidayRepository.save(holiday);
        }

        {
            String id = UUID.randomUUID().toString();
            User user = new User(id, "Anne", "anne@gmail.com", true);
            userRepo.save(user);

            VolunteerDay VolunteerDay = new VolunteerDay(UUID.randomUUID().toString(), day, true, true, id);
            volunteerDayRepository.save(VolunteerDay);
        }
    }

    public Rosta buildRosta(LocalDate date) {
        LocalDate rostaStartDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate rostaEndDate = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LOGGER.debug("rostaStartDate = {}", rostaStartDate);
        LOGGER.debug("rostaEndDate = {}", rostaEndDate);

        Rosta rosta = new Rosta(rostaEndDate);

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
}
