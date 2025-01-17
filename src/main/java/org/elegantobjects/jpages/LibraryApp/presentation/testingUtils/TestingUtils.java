package org.elegantobjects.jpages.LibraryApp.presentation.testingUtils;

import org.elegantobjects.jpages.LibraryApp.common.util.Result;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.domain.book.data.local.EntityBookInfo;
import org.elegantobjects.jpages.LibraryApp.domain.book.data.network.DTOBookInfo;
import org.elegantobjects.jpages.LibraryApp.domain.Context;
import org.elegantobjects.jpages.LibraryApp.domain.account.Account;
import org.elegantobjects.jpages.LibraryApp.domain.account.data.AccountInfo;
import org.elegantobjects.jpages.LibraryApp.domain.book.Book;
import org.elegantobjects.jpages.LibraryApp.domain.book.data.BookInfo;
import org.elegantobjects.jpages.LibraryApp.domain.library.Library;
import org.elegantobjects.jpages.LibraryApp.domain.library.data.LibraryInfo;
import org.elegantobjects.jpages.LibraryApp.domain.user.User;
import org.elegantobjects.jpages.LibraryApp.domain.user.data.UserInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

/**
 * Testing Utility Methods<br>
 * <br>
 * Useful for adding fake data to the Repos, DB and API for testing purposes.<br>
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

public class TestingUtils {

    final Context context;

    public
    TestingUtils(@NotNull Context context) {
        this.context = context;
    }

    ///////////////////////////
    // Book Repo, DB and API //
    ///////////////////////////

    public void populateFakeBookInfoInBookRepoDBandAPI() {
        populateDBWithFakeBookInfo();
        populateApiWithFakeBookInfo();
    }

    public void populateDBWithFakeBookInfo() {
        for (int i = 0; i < 10; i++) {
            final int id = 1000+i*100;

            Result<BookInfo> result = context.bookInfoRepo()
                    .upsertTestEntityBookInfoToDB(
                            new EntityBookInfo(
                                    UUID2.createFakeUUID2(id, Book.class),
                                    "Title " + id,
                                    "Author " + id,
                                    "Description " + id,
                                    "Some extra info from the Entity" + id,
                                    Instant.parse("2023-01-01T00:00:00.00Z").toEpochMilli(),
                                    Instant.parse("2023-01-01T00:00:00.00Z").toEpochMilli(),
                                    false
                            )
                    );

            if (result instanceof Result.Failure) {
                Exception exception = ((Result.Failure<BookInfo>) result).exception();
                context.log.d(this, exception.getMessage());
            }
        }
    }

    public void populateApiWithFakeBookInfo() {
        for (int i = 0; i < 10; i++) {
            final int id = 1000+i*100;

            Result<BookInfo> result = context.bookInfoRepo().upsertTestDTOBookInfoToApi(
                    new DTOBookInfo(
                            UUID2.createFakeUUID2(id, Book.class),
                            "Title " + id,
                            "Author " + id,
                            "Description " + id,
                            "Some extra info from the DTO" + id,
                            Instant.parse("2023-01-01T00:00:00.00Z").toEpochMilli(),
                            Instant.parse("2023-01-01T00:00:00.00Z").toEpochMilli(),
                            false
                    )
            );

            if (result instanceof Result.Failure) {
                Exception exception = ((Result.Failure<BookInfo>) result).exception();
                context.log.d(this, exception.getMessage());
            }
        }
    }

    public void printBookInfoDBandAPIEntries() {
        System.out.print("\n");
        context.log.d(this,"DB Dump");
        context.bookInfoRepo().printDB();

        System.out.print("\n");
        context.log.d(this,"API Dump");
        context.bookInfoRepo().printAPI();

        System.out.print("\n");
    }

    public Result<BookInfo> addFakeBookInfoToBookInfoRepo(final Integer id) {
        final BookInfo bookInfo = createFakeBookInfo(id);
        return context.bookInfoRepo().upsertBookInfo(bookInfo);
    }

    public BookInfo createFakeBookInfo(final Integer id) {
        Integer fakeId = id;
        if (fakeId == null) fakeId = 1;

        UUID2<Book> uuid2;
        uuid2 = UUID2.createFakeUUID2(fakeId, Book.class);

        return new BookInfo(
                uuid2,
                "Book " + fakeId,
                "Author " + fakeId,
                "Description " + fakeId,
                Instant.parse("2023-01-01T00:00:00.00Z").toEpochMilli(),
                Instant.parse("2023-01-01T00:00:00.00Z").toEpochMilli(),
                false
        );
    }

    ////////////////
    // User Repo  //
    ////////////////

    public @Nullable Result<UserInfo> createFakeUserInfoInUserInfoRepo(final Integer id) {
        Integer someNumber = id;
        if (someNumber == null) someNumber = 1;

        Result<UserInfo> upsertUserInfoResult =
            context.userInfoRepo()
                .upsertUserInfo(
                    new UserInfo(
                        UUID2.createFakeUUID2(someNumber, User.class),  // uses DOMAIN id
                        "User " + someNumber,
                        "user" + someNumber + "@gmail.com"
                    ));

        if (upsertUserInfoResult instanceof Result.Failure) {
            context.log.d(this,"User Error: " + ((Result.Failure<UserInfo>) upsertUserInfoResult).exception().getMessage());
            return null;
        }

        return upsertUserInfoResult;
    }

    ///////////////////
    // Account Repo  //
    ///////////////////

    public @Nullable Result<AccountInfo> createFakeAccountInfoInAccountRepo(final Integer id) {
        Integer someNumber = id;
        if (someNumber == null) someNumber = 1;

        Result<AccountInfo> accountInfoResult = context.accountInfoRepo()
                .upsertAccountInfo(
                        new AccountInfo(
                                UUID2.createFakeUUID2(someNumber, Account.class), // uses DOMAIN id
                                "Account for User " + someNumber
                        )
                );

        if (accountInfoResult instanceof Result.Failure) {
            context.log.d(this,"Account Error: " + ((Result.Failure<AccountInfo>) accountInfoResult).exception().getMessage());
            return null;
        }

        AccountInfo accountInfo = ((Result.Success<AccountInfo>) accountInfoResult).value();
        accountInfo.addTestAuditLogMessage("AccountInfo created for User " + someNumber);

        return accountInfoResult;
    }

    public void populateAccountWithFakeAuditMessages(@NotNull UUID2<Account> accountId, int numberOfMessagesToCreate) {
        context.log.d(this, "accountId: " + accountId + ", numberOfAccountsToCreate: " + numberOfMessagesToCreate);
        Result<AccountInfo> infoResult = context.accountInfoRepo().fetchAccountInfo(accountId);
        if (infoResult instanceof Result.Failure) {
            context.log.d(this, "Error: " + ((Result.Failure<AccountInfo>) infoResult).exception().getMessage());
            return;
        }
        AccountInfo accountInfo = ((Result.Success<AccountInfo>) infoResult).value();

        for (int i = 0; i < numberOfMessagesToCreate; i++) {
            accountInfo.addTestAuditLogMessage(
                    "Test Audit message " + i + " for account: " + accountInfo.id()
            );
        }
    }

    ///////////////////
    // Library Repo  //
    ///////////////////

    public Result<LibraryInfo> createFakeLibraryInfoInLibraryInfoRepo(final Integer id) {
        Integer someNumber = id;
        if (someNumber == null) someNumber = 1;

        return context.libraryInfoRepo()
                .upsertLibraryInfo(
                        new LibraryInfo(
                                UUID2.createFakeUUID2(someNumber, Library.class), // uses DOMAIN id
                                "Library " + someNumber
                        )
                );
    }

    public void populateLibraryWithFakeBooks(@NotNull UUID2<Library> libraryId, int numberOfBooksToCreate) {
        context.log.d(this, "libraryId: " + libraryId + ", numberOfBooksToCreate: " + numberOfBooksToCreate);
        Result<LibraryInfo> libraryInfoResult = context.libraryInfoRepo().fetchLibraryInfo(libraryId);
        if (libraryInfoResult instanceof Result.Failure) {
            context.log.d(this, "Error: " + ((Result.Failure<LibraryInfo>) libraryInfoResult).exception().getMessage());
            return;
        }
        LibraryInfo libraryInfo = ((Result.Success<LibraryInfo>) libraryInfoResult).value();

        for (int i = 0; i < numberOfBooksToCreate; i++) {
            Result<UUID2<Book>> result =
                    libraryInfo.addTestBook(UUID2.createFakeUUID2(1000+i*100, Book.class), 1);

            if (result instanceof Result.Failure) {
                Exception exception = ((Result.Failure<UUID2<Book>>) result).exception();
                context.log.d(this, exception.getMessage());
            }
        }
    }
}
