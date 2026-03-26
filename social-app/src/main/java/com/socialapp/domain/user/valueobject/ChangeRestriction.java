package com.socialapp.domain.user.valueobject;

import java.time.LocalDate;

/**
 * Value Object: ChangeRestriction
 * Gom các ngày giới hạn đổi thông tin để enforce business rule.
 */
public final class ChangeRestriction {

    private static final int NAME_CHANGE_DAYS     = 30;
    private static final int USERNAME_CHANGE_DAYS = 30;
    private static final int BIRTHDATE_CHANGE_DAYS = 365;

    private final LocalDate nextChangeNameDate;
    private final LocalDate nextChangeUsernameDate;
    private final LocalDate nextChangeBirthdateDate;

    private ChangeRestriction(LocalDate nextChangeNameDate,
                              LocalDate nextChangeUsernameDate,
                              LocalDate nextChangeBirthdateDate) {
        this.nextChangeNameDate      = nextChangeNameDate;
        this.nextChangeUsernameDate  = nextChangeUsernameDate;
        this.nextChangeBirthdateDate = nextChangeBirthdateDate;
    }

    public static ChangeRestriction of(LocalDate nextChangeNameDate,
                                       LocalDate nextChangeUsernameDate,
                                       LocalDate nextChangeBirthdateDate) {
        return new ChangeRestriction(nextChangeNameDate, nextChangeUsernameDate, nextChangeBirthdateDate);
    }

    public static ChangeRestriction noRestriction() {
        LocalDate past = LocalDate.now().minusDays(1);
        return new ChangeRestriction(past, past, past);
    }

    // ── Domain Logic ──────────────────────────────────────────

    public boolean canChangeName() {
        return LocalDate.now().isAfter(nextChangeNameDate) ||
                LocalDate.now().isEqual(nextChangeNameDate);
    }

    public boolean canChangeUsername() {
        return LocalDate.now().isAfter(nextChangeUsernameDate) ||
                LocalDate.now().isEqual(nextChangeUsernameDate);
    }

    public boolean canChangeBirthdate() {
        return LocalDate.now().isAfter(nextChangeBirthdateDate) ||
                LocalDate.now().isEqual(nextChangeBirthdateDate);
    }

    public ChangeRestriction afterNameChanged() {
        return new ChangeRestriction(
                LocalDate.now().plusDays(NAME_CHANGE_DAYS),
                this.nextChangeUsernameDate,
                this.nextChangeBirthdateDate);
    }

    public ChangeRestriction afterUsernameChanged() {
        return new ChangeRestriction(
                this.nextChangeNameDate,
                LocalDate.now().plusDays(USERNAME_CHANGE_DAYS),
                this.nextChangeBirthdateDate);
    }

    public ChangeRestriction afterBirthdateChanged() {
        return new ChangeRestriction(
                this.nextChangeNameDate,
                this.nextChangeUsernameDate,
                LocalDate.now().plusDays(BIRTHDATE_CHANGE_DAYS));
    }

    public LocalDate getNextChangeNameDate()      { return nextChangeNameDate; }
    public LocalDate getNextChangeUsernameDate()  { return nextChangeUsernameDate; }
    public LocalDate getNextChangeBirthdateDate() { return nextChangeBirthdateDate; }
}