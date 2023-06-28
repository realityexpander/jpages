package org.elegantobjects.jpages.App2.domain.account;

import org.elegantobjects.jpages.App2.common.Model;
import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.common.DomainInfo;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

// AccountInfo contains data about a single User's account status in the LibraryApp system.
// - Status of Account (active, inactive, suspended, etc.)
// - Current Fine Amount
// - Max books allowed to be checked out
// Domain objects contain the "Model.{XXX}.{Domain}Info" and the associated business logic to manipulate it
public class AccountInfo extends DomainInfo
        implements
        Model.ToInfoDomain<AccountInfo>
{
    public final UUID2<Account> id;  // note this is a UUID2<Account> or UUID2<AccountInfo>,
                                      // - it's UUID matches the UUID of the UUID2<User> for this Account. // todo should this be a UUID2<User> instead?
    // final UUID2<User> userId;      // note this is a UUID2<User> not a UUID2<UserInfo>, it is the id of the User. // should this be used?
    public final String name;
    public final AccountStatus accountStatus;  // status of account (active, inactive, suspended, etc.)
    public final int currentFinePennies;       // current fine amount in pennies
    public final int maxBooks;                 // max books allowed to be checked out
    public final int maxFinePennies;           // max fine amount allowed before account is suspended

    final private HashMap<Long, String> accountAuditLog; // timestamp_ms -> message as json

    // final int maxDays;               // max number of days a book can be checked out
    // final int maxRenewals;           // max number of renewals (per book)
    // final int maxRenewalDays;        // max number days for each renewal (per book)
    // final int maxFineAmountPennies;  // max dollar amount of all fines allowed before account is suspended
    // final int maxFineDays;           // max number of days to pay fine before account is suspended

    enum AccountStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        CLOSED;
    }

    public AccountInfo(
        @NotNull UUID2<Account> id,  // UUID should match User's UUID
        String name,
        AccountStatus accountStatus,
        int currentFinePennies,
        int maxBooks,
        int maxFinePennies,
        HashMap<Long, String> accountAuditLog
    ) {
        super(id);
        this.id = id;
        this.name = name;
        this.accountStatus = accountStatus;
        this.currentFinePennies = currentFinePennies;
        this.maxBooks = maxBooks;
        this.maxFinePennies = maxFinePennies;
        this.accountAuditLog = accountAuditLog;
    }
    public AccountInfo(UUID2<Account> id, String name) { // sensible defaults
        this(
            id,
            name,
            AccountStatus.ACTIVE,
            0,
            5,
            1000,
            new HashMap<>()
        );
    }
    public AccountInfo(@NotNull AccountInfo accountInfo) {
        this(
            accountInfo.id,
            accountInfo.name,
            accountInfo.accountStatus,
            accountInfo.currentFinePennies,
            accountInfo.maxBooks,
            accountInfo.maxFinePennies,
            accountInfo.accountAuditLog
        );
    }
    public AccountInfo(UUID uuid, String name) {
        this(new UUID2<Account>(uuid, Account.class), name);
    }
    public AccountInfo(String id, String name) {
        this(UUID.fromString(id), name);
    }

    @Override
    public String toString() {
        return this.toPrettyJson();
    }

    ///////////////////////////////
    // Published Simple Getters  //
    ///////////////////////////////

    @Override
    public UUID2<Account> id() {
        return id;
    }
    public String name() {
        return this.name;
    }

    /////////////////////////////////////////////
    // Published Domain Business Logic Methods //
    /////////////////////////////////////////////

    public Result<AccountInfo> activateAccountByStaff(String reason, String staffMemberName) {
        if (reason == null || reason.isEmpty())
            return new Result.Failure<>(new IllegalArgumentException("reason is null or empty"));
        if (staffMemberName == null || staffMemberName.isEmpty())
            return new Result.Failure<>(new IllegalArgumentException("staffMemberName is null or empty"));

        addAuditLogEntry("activateAccountByStaff", "reason", reason, "staffMemberName", staffMemberName);

        return new Result.Success<>(new AccountInfo(this.id,
                this.name,
                AccountStatus.ACTIVE,
                this.currentFinePennies,
                this.maxBooks,
                this.maxFinePennies,
                this.accountAuditLog
        ));
    }
    public Result<AccountInfo> deactivateAccountByStaff(String reason, String staffMemberName) {
        if (reason == null || reason.isEmpty())
            return new Result.Failure<>(new IllegalArgumentException("reason is null or empty"));
        if (staffMemberName == null || staffMemberName.isEmpty())
            return new Result.Failure<>(new IllegalArgumentException("staffMemberName is null or empty"));

        addAuditLogEntry("deactivateAccountByStaff", "reason", reason, "staffMemberName", staffMemberName);

        return new Result.Success<>(new AccountInfo(this.id,
                this.name,
                AccountStatus.INACTIVE,
                this.currentFinePennies,
                this.maxBooks,
                this.maxFinePennies,
                this.accountAuditLog
        ));
    }
    public Result<AccountInfo> suspendAccountByStaff(String reason, String staffMemberName) {
        if (reason == null || reason.isEmpty())
            return new Result.Failure<>(new IllegalArgumentException("reason is null or empty"));
        if (staffMemberName == null || staffMemberName.isEmpty())
            return new Result.Failure<>(new IllegalArgumentException("staffMemberName is null or empty"));

        addAuditLogEntry("suspendAccountByStaff", "reason", reason, "staffMemberName", staffMemberName);

        return new Result.Success<>(new AccountInfo(this.id,
                this.name,
                AccountStatus.SUSPENDED,
                this.currentFinePennies,
                this.maxBooks,
                this.maxFinePennies,
                this.accountAuditLog
        ));
    }
    public Result<AccountInfo> closeAccountByStaff(String reason, String staffMemberName) {
        if (reason == null || reason.isEmpty())
            return new Result.Failure<>(new IllegalArgumentException("reason is null or empty"));
        if (staffMemberName == null || staffMemberName.isEmpty())
            return new Result.Failure<>(new IllegalArgumentException("staffMemberName is null or empty"));

        addAuditLogEntry("closeAccountByStaff", "reason", reason, "staffMemberName", staffMemberName);

        return new Result.Success<>(new AccountInfo(this.id,
                this.name,
                AccountStatus.CLOSED,
                this.currentFinePennies,
                this.maxBooks,
                this.maxFinePennies,
                this.accountAuditLog
        ));
    }

    public Result<AccountInfo> addFineForBook(int fineAmountPennies, UUID2<Book> bookId) {
        if (fineAmountPennies < 0)
            return new Result.Failure<>(new IllegalArgumentException("fineAmountPennies is negative"));
        if (bookId == null)
            return new Result.Failure<>(new IllegalArgumentException("book is null"));

        addAuditLogEntry("addFine", "fineAmountPennies", fineAmountPennies, "bookId", bookId);

        AccountStatus updatedAccountStatus = this.accountStatus;
        if (calculateAccountStatus() != this.accountStatus)
            updatedAccountStatus = calculateAccountStatus();

        return new Result.Success<>(new AccountInfo(this.id,
                this.name,
                updatedAccountStatus,
                this.currentFinePennies + fineAmountPennies,
                this.maxBooks,
                this.maxFinePennies,
                this.accountAuditLog
        ));
    }
    public Result<AccountInfo> payFine(int fineAmountPennies) {
        if (fineAmountPennies < 0)
            return new Result.Failure<>(new IllegalArgumentException("fineAmountPennies is negative"));

        addAuditLogEntry("payFine", fineAmountPennies);

        AccountStatus updatedAccountStatus = this.accountStatus;
        if (calculateAccountStatus() != this.accountStatus)
            updatedAccountStatus = calculateAccountStatus();

        return new Result.Success<>(new AccountInfo(this.id,
                this.name,
                updatedAccountStatus,
                this.currentFinePennies - fineAmountPennies,
                this.maxBooks,
                this.maxFinePennies,
                this.accountAuditLog
        ));
    }
    public Result<AccountInfo> adjustFineByStaff(int newCurrentFineAmount, String reason, String staffMemberName) { // todo make staffMemberName a User.Staff object
        if (newCurrentFineAmount < 0)
            return new Result.Failure<>(new IllegalArgumentException("newCurrentFineAmount is negative"));

        addAuditLogEntry("adjustFineByStaff", "reason", reason, "staffMember", staffMemberName);

        AccountStatus updatedAccountStatus = this.accountStatus;
        if (calculateAccountStatus() != this.accountStatus)
            updatedAccountStatus = calculateAccountStatus();

        return new Result.Success<>(new AccountInfo(this.id,
                this.name,
                updatedAccountStatus,
                newCurrentFineAmount,
                this.maxBooks,
                this.maxFinePennies,
                this.accountAuditLog
        ));
    }

    public Result<AccountInfo> changeMaxBooksByStaff(int maxBooks, String reason, String staffMemberName) { // todo make staffMemberName a User.Staff object
        if (maxBooks < 0)
            return new Result.Failure<>(new IllegalArgumentException("maxBooks is negative"));

        addAuditLogEntry("changeMaxBooksByStaff", "reason", reason, "staffMember", staffMemberName);

        return new Result.Success<>(new AccountInfo(this.id,
                this.name,
                this.accountStatus,
                this.currentFinePennies,
                maxBooks,
                this.maxFinePennies,
                this.accountAuditLog));
    }
    public Result<AccountInfo> changeMaxFineByStaff(int maxFine, String reason, String staffMemberName) { // todo make staffMemberName a User.Staff object
        if (maxFine < 0)
            return new Result.Failure<>(new IllegalArgumentException("maxFine is negative"));

        addAuditLogEntry("changeMaxFineByStaff", "reason", reason, "staffMember", staffMemberName);

        return new Result.Success<>(new AccountInfo(this.id,
                this.name,
                this.accountStatus,
                this.currentFinePennies,
                this.maxBooks,
                maxFine,
                this.accountAuditLog));
    }

    /////////////////////////////////////////
    // Published Domain Reporting Methods  //
    /////////////////////////////////////////

    public int calculateFineAmountPennies() {
        return this.currentFinePennies;
    }

    public String[] getAuditLogStrings() {
        return this.accountAuditLog
                .entrySet()
                .stream()
                .map(entry ->  // Convert audit log to array of `timestamp:{action:"data"}` strings
                        convertTimeStampLongMillisToIsoDateTimeString(entry.getKey()) +
                        ": " +
                        entry.getValue()
                )
                .toArray(String[]::new);
    }

    private String convertTimeStampLongMillisToIsoDateTimeString(long timeStampMillis) {
        return DateTimeFormatter.ISO_DATE_TIME.format(
                Instant.ofEpochMilli(timeStampMillis)
        );
    }

    /////////////////////////////////
    // Published Helper Methods    //
    /////////////////////////////////

    public boolean isAccountActive() {
        return this.accountStatus == AccountStatus.ACTIVE;
    }
    public boolean isAccountInactive() {
        return this.accountStatus == AccountStatus.INACTIVE;
    }
    public boolean isAccountSuspended() {
        return this.accountStatus == AccountStatus.SUSPENDED;
    }
    public boolean isAccountClosed() {
        return this.accountStatus == AccountStatus.CLOSED;
    }
    public boolean isAccountInGoodStanding() {
        return this.accountStatus == AccountStatus.ACTIVE || this.accountStatus == AccountStatus.INACTIVE;
    }
    public boolean isAccountInBadStanding() {
        return this.accountStatus == AccountStatus.SUSPENDED || this.accountStatus == AccountStatus.CLOSED;
    }
    public boolean isAccountInGoodStandingWithNoFines() {
        return this.accountStatus == AccountStatus.ACTIVE || this.accountStatus == AccountStatus.INACTIVE && this.currentFinePennies == 0;
    }
    public boolean isAccountInGoodStandingWithFines() {
        return this.accountStatus == AccountStatus.ACTIVE || this.accountStatus == AccountStatus.INACTIVE && this.currentFinePennies > 0;
    }

    public boolean hasFines() {
        return this.currentFinePennies > 0;
    }
    public boolean hasNoFines() {
        return this.currentFinePennies <= 0;
    }
    public boolean isMaxFineExceeded() {
        return this.currentFinePennies >= this.maxFinePennies;
    }
    public boolean hasReachedMaxBooks(int numberOfBooksInPosession) {
        return numberOfBooksInPosession >= this.maxBooks;
    }

    /////////////////////////////////////////
    // Published Testing Helper Methods    //
    /////////////////////////////////////////

    public void addTestAuditLogMessage(String message) {
        addAuditLogEntry("addTestAuditLogMessage", message);
    }

    //////////////////////////////
    // Private Helper Functions //
    //////////////////////////////

    public Result<AccountInfo> activateAccount() {
        // Note: this status will be overridden if User pays a fine or Library adds a fine
        return this.withAccountStatus(AccountStatus.ACTIVE);
    }
    public Result<AccountInfo> deactivateAccount() {
        return this.withAccountStatus(AccountStatus.INACTIVE);
    }
    public Result<AccountInfo> suspendAccount() {
        // Note: this status will be overridden if User pays a fine or Library adds a fine
        return this.withAccountStatus(AccountStatus.SUSPENDED);
    }
    public Result<AccountInfo> closeAccount() {
        return this.withAccountStatus(AccountStatus.CLOSED);
    }

    private AccountStatus calculateAccountStatus() {
        if (this.accountStatus == AccountStatus.CLOSED)
            return AccountStatus.CLOSED;

        if (this.accountStatus == AccountStatus.INACTIVE)
            return AccountStatus.INACTIVE;

        if (this.currentFinePennies > this.maxFinePennies) {
            return AccountStatus.SUSPENDED;
        }

        return AccountStatus.ACTIVE;
    }

    private void addAuditLogEntry(String operation) {
        accountAuditLog.put(System.currentTimeMillis(), operation);
    }
    private void addAuditLogEntry(String operation, Object value) {
        accountAuditLog.put(System.currentTimeMillis(), "{ \"" + operation + "\": " + value + " }");
    }
    private void addAuditLogEntry(String operation, String key1, Object value1, String key2, Object value2) {
        accountAuditLog.put(System.currentTimeMillis(),
            "{ \"" + operation + "\": " +
                "{" +
                    "\"" + key1 + "\": " + value1 + "," +
                    "\"" + key2 + "\": " + value2 +
                "}" +
            "}"
        );

    }

    private Result<AccountInfo> withName(String newName) {
        if (newName == null || newName.isEmpty())
            return new Result.Failure<>(new IllegalArgumentException("newName is null or empty"));

        return new Result.Success<>(new AccountInfo(this.id, newName));
    }
    private Result<AccountInfo> withAccountStatus(AccountStatus newAccountStatus) {
        if (newAccountStatus == null)
            return new Result.Failure<>(new IllegalArgumentException("newAccountStatus is null"));

        return new Result.Success<>(new AccountInfo(
                this.id,
                this.name,
                newAccountStatus,
                this.currentFinePennies,
                this.maxBooks,
                this.maxFinePennies,
                this.accountAuditLog
        ));
    }
    private Result<AccountInfo> withCurrentFineAmountPennies(int newCurrentFineAmountPennies) {
        if (newCurrentFineAmountPennies < 0)
            return new Result.Failure<>(new IllegalArgumentException("newCurrentFineAmountPennies is negative"));

        return new Result.Success<>(new AccountInfo(
                this.id,
                this.name,
                this.accountStatus,
                newCurrentFineAmountPennies,
                this.maxBooks,
                this.maxFinePennies,
                this.accountAuditLog
        ));
    }
    private Result<AccountInfo> withMaxBooks(int newMaxBooks) {
        if (newMaxBooks < 0)
            return new Result.Failure<>(new IllegalArgumentException("newMaxBooks is negative"));

        return new Result.Success<>(new AccountInfo(
                this.id,
                this.name,
                this.accountStatus,
                this.currentFinePennies,
                newMaxBooks,
                this.maxFinePennies,
                this.accountAuditLog
        ));
    }

    /////////////////////////////////
    // ToDomainInfo implementation //
    /////////////////////////////////

    // note: currently no DB or API for UserInfo (so no .ToEntity() or .ToDTO())
    @Override
    public AccountInfo toDeepCopyDomainInfo() {
        // Note: *MUST* return a deep copy
        return new AccountInfo(
                this.id,
                this.name,
                this.accountStatus,
                this.currentFinePennies,
                this.maxBooks,
                this.maxFinePennies,
                new HashMap<>(this.accountAuditLog)
        );
    }

    @Override
    public UUID2<?> getDomainInfoId() {
        return this.id;
    }
}
