package org.elegantobjects.jpages.LibraryAppTest;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.testingUtils.TestingUtils;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.Context;
import org.elegantobjects.jpages.App2.domain.account.Account;
import org.elegantobjects.jpages.App2.domain.account.AccountInfo;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.book.BookInfo;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LibraryTest {

    Context context;

    @Before
    public void setUp() throws Exception {
    }

    private @NotNull Context setupTestContext() {
        TestLog testLog = new TestLog();
        Context prodContext = Context.setupProductionInstance(testLog);

        // modify the Prod context into a Test context.
        Context testContext = new Context(
            prodContext.bookInfoRepo(),
            prodContext.userInfoRepo(),
            prodContext.libraryInfoRepo(),
            prodContext.accountInfoRepo(),
            prodContext.gson,
            testLog
        );
        TestingUtils testUtil = new TestingUtils(testContext);

        testUtil.PopulateFakeBookInfoInContextBookRepoDBandAPI();

        // Create fake AccountInfo
        AccountInfo accountInfo = new AccountInfo(
                UUID2.createFakeUUID2(1, Account.class),
                "User Name 1"
        );

        return testContext;
    }

    @Test
    public void Update_BookInfo_with_New_Info_is_successful() {
        // • ARRANGE
        Context context = setupTestContext();

        // Create a book object (it only has an id)
        Book book = new Book(UUID2.createFakeUUID2(1100, Book.class), null, context);
        context.log.d(this, book.fetchInfoResult().toString());

        // • ACT
        // Update info for a book
        final Result<BookInfo> bookInfoResult =
            book.updateInfo(
                new BookInfo(
                    book.id,
                    "The Updated Title",
                    "The Updated Author",
                    "The Updated Description"
                ));
        context.log.d(this, book.fetchInfoResult().toString());

        // •ASSERT
        // Get the bookInfo (null if not loaded)
        BookInfo bookInfo3 = book.fetchInfo();
        if (bookInfo3 == null) {
            context.log.d(this, "Book Missing --> book id: " + book.id() + " >> " + " is null");
            assert false;
        }

        // •ASSERT
        // Check the title for update
        assert(bookInfoResult instanceof Result.Success);
        assertEquals("The Updated Title", ((Result.Success<BookInfo>) bookInfoResult).value().title);

        // •ASSERT
        // Try to get a book id that doesn't exist - SHOULD FAIL
        Book book2 = new Book(UUID2.createFakeUUID2(99, Book.class), null, context);
        if (!(book2.fetchInfoResult() instanceof Result.Failure)) {
            context.log.d(this, "Book SHOULD NOT Exist --> " + ((Result.Success<BookInfo>) book2.fetchInfoResult()).value());
            assert false;
        }
    }
}