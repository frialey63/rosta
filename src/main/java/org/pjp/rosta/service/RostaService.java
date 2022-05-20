package org.pjp.rosta.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

import org.pjp.rosta.bean.Rosta;
import org.pjp.rosta.bean.RostaDay;
import org.pjp.rosta.model.Shift;
import org.pjp.rosta.model.User;
import org.pjp.rosta.model.VolunteerDay;
import org.pjp.rosta.repository.ShiftRepository;
import org.pjp.rosta.repository.UserRepository;
import org.pjp.rosta.repository.VolunteerDayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RostaService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ShiftRepository shiftRepo;

    @Autowired
    private VolunteerDayRepository volunteerDayRepository;

    public void initData() {
        userRepo.deleteAll();
        shiftRepo.deleteAll();
        volunteerDayRepository.deleteAll();

        LocalDate day = LocalDate.of(2022, 5, 16);
        System.out.println("day = " + day);

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

        System.out.println("rostaStartDate = " + rostaStartDate);
        System.out.println("rostaEndDate = " + rostaEndDate);

        Rosta rosta = new Rosta(rostaEndDate);

        userRepo.findAll().forEach(user -> {
            System.out.println("-------------");
            System.out.println(user);

            String userUuid = user.getUuid();

            shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), rostaEndDate).ifPresent(shift -> {
                System.out.println(shift);

                shift.getShiftDayIterator().forEachRemaining(shiftDay -> {
                    RostaDay rostaDay = rosta.getRostaDay(shiftDay.getDayOfWeek());
                    rostaDay.addUserUuid(shiftDay, userUuid);
                });
            });

            volunteerDayRepository.findAllByUserUuidAndDateBetween(userUuid, rostaStartDate, rostaEndDate).forEach(volunteerDay -> {
                System.out.println(volunteerDay);

                RostaDay rostaDay = rosta.getRostaDay(volunteerDay.getDate().getDayOfWeek());
                rostaDay.addUserUuid(volunteerDay, userUuid);
            });

            // TODO incorporate holidays into the rosta
        });

        return rosta;
    }
}
