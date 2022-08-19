package org.pjp.rosta.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pjp.rosta.model.Shift;
import org.pjp.rosta.model.User;
import org.pjp.rosta.model.UserRole;
import org.pjp.rosta.repository.ShiftRepository;

@ExtendWith(MockitoExtension.class)
class RotaServiceTest {

    @Mock
    private ShiftRepository shiftRepo;

    @InjectMocks
    private RotaService rotaService;

    @Test
    void testGetDayOfWeekRange() {
        assertArrayEquals(new DayOfWeek[] { DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY }, RotaService.getDayOfWeekRange(DayOfWeek.MONDAY, DayOfWeek.FRIDAY).toArray(new DayOfWeek[0]));

        assertArrayEquals(new DayOfWeek[] { DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY }, RotaService.getDayOfWeekRange(DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY).toArray(new DayOfWeek[0]));

        assertArrayEquals(new DayOfWeek[] { DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY }, RotaService.getDayOfWeekRange(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY).toArray(new DayOfWeek[0]));
    }

    @Test
    void testCalculateShiftWorkJune() {
        // GIVEN

        User user = new User(UUID.randomUUID().toString(), "fred", UserRole.SUPERVISOR, "Fred Bloggs", null, true, "fred@gmail.com", true, true, true);

        LocalDate start = LocalDate.of(2022, 6, 1);
        LocalDate end = LocalDate.of(2022, 6, 30);

        Shift junShift = new Shift();

        Shift julShift = new Shift();
        julShift.getShiftDay(DayOfWeek.WEDNESDAY).setAfternoon(false);

        // WHEN

        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 6, 5))).thenReturn(Optional.of(junShift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 6, 12))).thenReturn(Optional.of(junShift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 6, 19))).thenReturn(Optional.of(junShift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 6, 26))).thenReturn(Optional.of(junShift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 7, 3))).thenReturn(Optional.of(julShift));

        // THEN

        assertEquals((22.0 - 0.5), rotaService.calculateShiftWork(user, start, end), 0.1);
    }

    @Test
    void testCalculateShiftWorkJuly() {
        // GIVEN

        User user = new User(UUID.randomUUID().toString(), "fred", UserRole.SUPERVISOR, "Fred Bloggs", null, true, "fred@gmail.com", true, true, true);

        LocalDate start = LocalDate.of(2022, 7, 1);
        LocalDate end = LocalDate.of(2022, 7, 31);

        Shift shift = new Shift();
        shift.getShiftDay(DayOfWeek.WEDNESDAY).setAfternoon(false);

        // WHEN

        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 7, 3))).thenReturn(Optional.of(shift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 7, 10))).thenReturn(Optional.of(shift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 7, 17))).thenReturn(Optional.of(shift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 7, 24))).thenReturn(Optional.of(shift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 7, 31))).thenReturn(Optional.of(shift));

        // THEN

        assertEquals((21.0 - 4 * 0.5), rotaService.calculateShiftWork(user, start, end), 0.1);
    }

    @Test
    void testCalculateShiftWorkAugust() {
        // GIVEN

        User user = new User(UUID.randomUUID().toString(), "fred", UserRole.SUPERVISOR, "Fred Bloggs", null, true, "fred@gmail.com", true, true, true);

        LocalDate start = LocalDate.of(2022, 8, 1);
        LocalDate end = LocalDate.of(2022, 8, 31);

        Shift augShift = new Shift();
        augShift.getShiftDay(DayOfWeek.MONDAY).setMorning(false);
        augShift.getShiftDay(DayOfWeek.MONDAY).setAfternoon(false);
        augShift.getShiftDay(DayOfWeek.TUESDAY).setMorning(false);
        augShift.getShiftDay(DayOfWeek.TUESDAY).setAfternoon(false);
        augShift.getShiftDay(DayOfWeek.SATURDAY).setMorning(true);
        augShift.getShiftDay(DayOfWeek.SATURDAY).setAfternoon(true);
        augShift.getShiftDay(DayOfWeek.SUNDAY).setMorning(true);
        augShift.getShiftDay(DayOfWeek.SUNDAY).setAfternoon(true);

        Shift sepShift = new Shift();

        // WHEN

        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 8, 7))).thenReturn(Optional.of(augShift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 8, 14))).thenReturn(Optional.of(augShift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 8, 21))).thenReturn(Optional.of(augShift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 8, 28))).thenReturn(Optional.of(augShift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 9, 4))).thenReturn(Optional.of(sepShift));

        // THEN

        assertEquals((20.0 + 3.0), rotaService.calculateShiftWork(user, start, end), 0.1);
    }

    @Test
    void testCalculateShiftWorkJuneJulyAugust() {
        // GIVEN

        User user = new User(UUID.randomUUID().toString(), "fred", UserRole.SUPERVISOR, "Fred Bloggs", null, true, "fred@gmail.com", true, true, true);

        LocalDate start = LocalDate.of(2022, 6, 1);
        LocalDate end = LocalDate.of(2022, 8, 31);

        Shift junShift = new Shift();

        Shift julShift = new Shift();
        julShift.getShiftDay(DayOfWeek.WEDNESDAY).setAfternoon(false);

        Shift augShift = new Shift();
        augShift.getShiftDay(DayOfWeek.MONDAY).setMorning(false);
        augShift.getShiftDay(DayOfWeek.MONDAY).setAfternoon(false);
        augShift.getShiftDay(DayOfWeek.TUESDAY).setMorning(false);
        augShift.getShiftDay(DayOfWeek.TUESDAY).setAfternoon(false);
        augShift.getShiftDay(DayOfWeek.SATURDAY).setMorning(true);
        augShift.getShiftDay(DayOfWeek.SATURDAY).setAfternoon(true);
        augShift.getShiftDay(DayOfWeek.SUNDAY).setMorning(true);
        augShift.getShiftDay(DayOfWeek.SUNDAY).setAfternoon(true);

        Shift sepShift = new Shift();

        // WHEN

        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 6, 5))).thenReturn(Optional.of(junShift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 6, 12))).thenReturn(Optional.of(junShift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 6, 19))).thenReturn(Optional.of(junShift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 6, 26))).thenReturn(Optional.of(junShift));

        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 7, 3))).thenReturn(Optional.of(julShift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 7, 10))).thenReturn(Optional.of(julShift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 7, 17))).thenReturn(Optional.of(julShift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 7, 24))).thenReturn(Optional.of(julShift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 7, 31))).thenReturn(Optional.of(julShift));

        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 8, 7))).thenReturn(Optional.of(augShift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 8, 14))).thenReturn(Optional.of(augShift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 8, 21))).thenReturn(Optional.of(augShift));
        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 8, 28))).thenReturn(Optional.of(augShift));

        when(shiftRepo.findFirstByUserUuidAndFromDateBeforeOrderByFromDateDesc(user.getUuid(), LocalDate.of(2022, 9, 4))).thenReturn(Optional.of(sepShift));

        // THEN

        assertEquals((22.0 - 0.5) + (21.0 - 4 * 0.5) + (20.0 + 3.0), rotaService.calculateShiftWork(user, start, end), 0.1);
    }

}
