package org.elegantobjects.jpages.App2.common.util.testingUtils;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.Context;
import org.elegantobjects.jpages.App2.domain.account.Account;
import org.elegantobjects.jpages.App2.domain.account.AccountInfo;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.book.BookInfo;
import org.elegantobjects.jpages.App2.domain.library.Library;
import org.elegantobjects.jpages.App2.domain.library.LibraryInfo;
import org.elegantobjects.jpages.App2.domain.user.User;
import org.elegantobjects.jpages.App2.domain.user.UserInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestingUtils {

    //////////////////////////////////////////////////////////////////////
    //////////////////////// TESTING Helper Methods //////////////////////
    //////////////////////////////////////////////////////////////////////

    final Context context;

    public
    TestingUtils(@NotNull Context context) {
        this.context = context;
    }

    public void PopulateFakeBookInfoInContextBookRepoDBandAPI() {
        context.bookInfoRepo().populateDatabaseWithFakeBookInfo();
        context.bookInfoRepo().populateApiWithFakeBookInfo();
    }

    public void DumpBookDBandAPI() {
        System.out.print("\n");
        context.log.d(this,"DB Dump");
        context.bookInfoRepo().printDB();

        System.out.print("\n");
        context.log.d(this,"API Dump");
        context.bookInfoRepo().printAPI();

        System.out.print("\n");
    }

    public Result<LibraryInfo> createFakeLibraryInfoInContextLibraryRepo(
        final Integer id
    ) {
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


    public @Nullable Result<AccountInfo> createFakeAccountInfoInContextAccountRepo(
        final Integer id
    ) {
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
    public @Nullable Result<UserInfo> createFakeUserInfoInContextUserInfoRepo(
        final Integer id
    ) {
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

    public Result<BookInfo> addFakeBookInfoInContextBookInfoRepo(
        final Integer id
    ) {
        final BookInfo bookInfo = createFakeBookInfo(id);

        return context.bookInfoRepo()
                .upsertBookInfo(bookInfo);
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
                "Description " + fakeId
        );
    }
}
