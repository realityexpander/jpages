package org.elegantobjects.jpages.App2.domain.repoData;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.common.Model;
import org.elegantobjects.jpages.App2.domain.Book;
import org.elegantobjects.jpages.App2.domain.User;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

public class DomainUserInfo extends Domain
    implements
        Model.ToDomain<DomainUserInfo>
{
    private final UUID2<User> id;  // note this is a UUID2<User> not a UUID2<UserInfo>, it is the id of the User.
    private final String name;
    private final String email;
    private final ArrayList<UUID2<Book>> acceptedBooks;
    private final Account account;

    static class Account {

        final Account.AccountStatus accountStatus;
        final int currentFineAmountPennies;
        final int maxBooks;             // max books allowed to be checked out
        final int maxDays;              // max number of days a book can be checked out
        final int maxRenewals;          // max number of renewals (per book)
        final int maxRenewalDays;       // max number days for each renewal (per book)
        final int maxFineAmountPennies; // max dollar amount of all fines allowed before account is suspended
        final int maxFineDays;          // max number of days to pay fine before account is suspended

        Account(Account.AccountStatus accountStatus,
                int currentFineAmountPennies,
                int maxBooks,
                int maxDays,
                int maxRenewals,
                int maxRenewalDays,
                int maxFineAmountPennies,
                int maxFineDays
        ) {
            this.accountStatus = accountStatus;
            this.currentFineAmountPennies = currentFineAmountPennies;
            this.maxBooks = maxBooks;
            this.maxDays = maxDays;
            this.maxRenewals = maxRenewals;
            this.maxRenewalDays = maxRenewalDays;
            this.maxFineAmountPennies = maxFineAmountPennies;
            this.maxFineDays = maxFineDays;
        }

        Account() {
            this.accountStatus = Account.AccountStatus.ACTIVE;
            this.currentFineAmountPennies = 0;
            maxBooks = 3;
            maxDays = 30;
            maxRenewals = 1;
            maxRenewalDays = 30;
            maxFineAmountPennies = 2000;
            maxFineDays = 30;
        }

        enum AccountStatus {
            ACTIVE,
            INACTIVE,
            SUSPENDED,
            CLOSED;
        }

        @Override
        public String toString() {
            return "Account (" +
                    this.accountStatus + ") : " +
                    "currentFineAmountPennies=" + this.currentFineAmountPennies + ", " +
                    "maxBooks=" + this.maxBooks;
        }

        // Use Builder pattern to create Account
        static class Builder {
            Account.AccountStatus accountStatus;
            int maxBooks;
            int maxDays;
            int maxRenewals;
            int maxRenewalDays;
            int maxFines;
            int maxFineDays;
            int maxFineAmount;

            Builder() {
                this.accountStatus = Account.AccountStatus.ACTIVE;
            } // default values

            Builder(Account account) {
                this.accountStatus = account.accountStatus;
                this.maxBooks = account.maxBooks;
                this.maxDays = account.maxDays;
                this.maxRenewals = account.maxRenewals;
                this.maxRenewalDays = account.maxRenewalDays;
                this.maxFines = account.maxFineAmountPennies;
                this.maxFineDays = account.maxFineDays;
                this.maxFineAmount = account.maxFineAmountPennies;
            }

            Account.Builder accountStatus(Account.AccountStatus accountStatus) {
                this.accountStatus = accountStatus;
                return this;
            }

            Account.Builder maxBooks(int maxBooks) {
                this.maxBooks = maxBooks;
                return this;
            }

            Account.Builder maxDays(int maxDays) {
                this.maxDays = maxDays;
                return this;
            }

            Account.Builder maxRenewals(int maxRenewals) {
                this.maxRenewals = maxRenewals;
                return this;
            }

            Account.Builder maxRenewalDays(int maxRenewalDays) {
                this.maxRenewalDays = maxRenewalDays;
                return this;
            }

            Account.Builder maxFines(int maxFines) {
                this.maxFines = maxFines;
                return this;
            }

            Account.Builder maxFineDays(int maxFineDays) {
                this.maxFineDays = maxFineDays;
                return this;
            }

            Account.Builder maxFineAmount(int maxFineAmount) {
                this.maxFineAmount = maxFineAmount;
                return this;
            }

            Account build() {
                return new Account(
                        this.accountStatus,
                        this.maxBooks,
                        this.maxDays,
                        this.maxRenewals,
                        this.maxRenewalDays,
                        this.maxFines,
                        this.maxFineDays,
                        this.maxFineAmount
                );
            }
        }
    }

    DomainUserInfo(
            @NotNull
            UUID2<User> id,
            String name,
            String email,
            ArrayList<UUID2<Book>> acceptedBooks,
            Account account
    ) {
        super(id.toDomainUUID2(), DomainUserInfo.class.getName());
        this.id = id;
        this.name = name;
        this.email = email;
        this.acceptedBooks = acceptedBooks;
        this.account = account;
    }
    DomainUserInfo(DomainUserInfo userInfo) {
        this(userInfo.id,
                userInfo.name,
                userInfo.email,
                userInfo.acceptedBooks,
                userInfo.account);
    }
    DomainUserInfo(UUID uuid, String name, String email, ArrayList<UUID2<Book>> acceptedBooks, Account account) {
        this(new UUID2<User>(uuid), name, email, acceptedBooks, account);
    }
    DomainUserInfo(String id, String name, String email, ArrayList<UUID2<Book>> acceptedBooks, Account account) {
        this(UUID.fromString(id), name, email, acceptedBooks, account);
    }
    public DomainUserInfo(UUID2<User> id, String name, String email) {
        this(id, name, email, new ArrayList<UUID2<Book>>(), new Account());
    }
    DomainUserInfo(UUID uuid, String name, String email) {
        this(new UUID2<User>(uuid), name, email);
    }
    DomainUserInfo(String id, String name, String email) {
        this(UUID.fromString(id), name, email);
    }

    ///////////////////////////////
    // Published Simple Getters  //
    ///////////////////////////////

    @Override
    public UUID2<User> id() {
        return id;
    }

    public String name() {
        return this.name;
    }

    public String email() {
        return this.email;
    }

    @Override
    public String toString() {
        return "User: " + this.name + " (" + this.email + "), acceptedBooks: " + this.acceptedBooks + ", borrowerStatus: " + this.account;
    }

    ////////////////////////////////////////
    // User Info Business Logic Methods   //
    ////////////////////////////////////////

    public Result<ArrayList<UUID2<Book>>> acceptBook(UUID2<Book> bookId) {
        if (this.acceptedBooks.contains(bookId)) {
            return new Result.Failure<>(new Exception("Book already accepted by user, book id:" + bookId));
        }

        try {
            this.acceptedBooks.add(bookId);
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }

        return new Result.Success<>(findAllAcceptedBooks());
    }

    public Result<ArrayList<UUID2<Book>>> unacceptBook(UUID2<Book> bookId) {
        if (!this.acceptedBooks.contains(bookId)) {
            return new Result.Failure<>(new Exception("Book not in acceptedBooks List for user, book id:" + bookId));
        }

        try {
            this.acceptedBooks.remove(bookId);
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }

        return new Result.Success<>(findAllAcceptedBooks());
    }

    public ArrayList<UUID2<Book>> findAllAcceptedBooks() {
        return new ArrayList<UUID2<Book>>(this.acceptedBooks);
    }

    public boolean isBookAcceptedByUser(UUID2<Book> bookId) {
        return this.acceptedBooks.contains(bookId);
    }

    /////////////////////////////
    // ToInfo implementation   //
    /////////////////////////////

    // note: no DB or API for UserInfo (so no .ToEntity() or .ToDTO())
    @Override
    public DomainUserInfo toDeepCopyDomainInfo() {
        // Note: Must return a deep copy (no original references)
        DomainUserInfo domainInfoCopy = new DomainUserInfo(this);

        // deep copy of acceptedBooks
        domainInfoCopy.acceptedBooks.clear();
        for (UUID2<Book> bookId : this.acceptedBooks) {
            domainInfoCopy.acceptedBooks.add(new UUID2<Book>(bookId.uuid()));
        }

        return domainInfoCopy;
    }

    @Override
    public UUID2<?> getDomainInfoId() {
        return this.id;
    }
}
