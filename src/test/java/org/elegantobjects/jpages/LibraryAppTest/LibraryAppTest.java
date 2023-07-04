package org.elegantobjects.jpages.LibraryAppTest;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.testingUtils.TestingUtils;
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
import org.elegantobjects.jpages.LibraryAppTest.testFakes.TestLog;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class LibraryAppTest {

//    @Before
//    public void setUp() throws Exception {
//    }

    private @NotNull Context setupTestContext() {
        TestLog testLog = new TestLog(false); // true = print all logs to console
        Context prodContext = Context.setupProductionInstance(testLog);

        // Change the Prod context into a Test context.
        Context testContext = new Context(
            prodContext.bookInfoRepo(),
            prodContext.userInfoRepo(),
            prodContext.libraryInfoRepo(),
            prodContext.accountInfoRepo(),
            prodContext.gson,
            testLog
        );

        return testContext;
    }

    static class TestRoles {
        public final Account account1;
        public final Account account2;
        public final User user1;
        public final Library library1;
        public final Book book1100;
        public final Book book1200;

        public
        TestRoles(
                Account account1,
                Account account2,
                User user1,
                Library library1,
                Book book1100,
                Book book1200
        ) {
            this.account1 = account1;
            this.account2 = account2;
            this.user1 = user1;
            this.library1 = library1;
            this.book1100 = book1100;
            this.book1200 = book1200;
        }
    }
    private @NotNull TestRoles setupDefaultScenarioAndRoles(@NotNull Context ctx) {

        TestingUtils testUtil = new TestingUtils(ctx);

        ////////////////////////////////////////
        // Setup DB & API simulated resources //
        ////////////////////////////////////////

        // • Put some fake BookInfo into the DB & API for BookInfo's
        testUtil.PopulateFakeBookInfoInContextBookRepoDBandAPI();

        // • Create & populate a Library in the Library Repo
        final Result<LibraryInfo> libraryInfo = testUtil.createFakeLibraryInfoInContextLibraryRepo(1);
        if (libraryInfo instanceof Result.Failure) {
            ctx.log.e(this,"Create Library FAILURE --> " + ((Result.Failure<LibraryInfo>) libraryInfo));
            assert false;
        }
        assertTrue(libraryInfo instanceof Result.Success);
        UUID2<Library> library1InfoId = ((Result.Success<LibraryInfo>) libraryInfo).value().id();
        ctx.log.d(this,"Library Created --> id: " + ((Result.Success<LibraryInfo>) libraryInfo).value().id() + ", name: "+ ((Result.Success<LibraryInfo>) libraryInfo).value().name);

        // Populate the library with 10 books
        ctx.libraryInfoRepo().populateWithFakeBooks(library1InfoId, 10);

        /////////////////////////////////
        // • Create Accounts for Users //
        /////////////////////////////////

        // Create fake AccountInfo for User 1
        AccountInfo accountInfo = new AccountInfo(
            UUID2.createFakeUUID2(1, Account.class),
            "User Name 1"
        );
        final Result<AccountInfo> accountInfo1Result = testUtil.createFakeAccountInfoInContextAccountRepo(1);
        final Result<AccountInfo> accountInfo2Result = testUtil.createFakeAccountInfoInContextAccountRepo(2);
        assertNotNull(accountInfo1Result);
        assertNotNull(accountInfo2Result);
        assertTrue(accountInfo1Result instanceof Result.Success);
        assertTrue(accountInfo2Result instanceof Result.Success);
        final AccountInfo accountInfo1 = ((Result.Success<AccountInfo>) accountInfo1Result).value();
        final AccountInfo accountInfo2 = ((Result.Success<AccountInfo>) accountInfo2Result).value();

        // Create & populate User1 in the User Repo for the Context
        final Result<UserInfo> user1InfoResult = testUtil.createFakeUserInfoInContextUserInfoRepo(1);
        assertNotNull(user1InfoResult);
        assertTrue(user1InfoResult instanceof Result.Success);
        final UserInfo user1Info = ((Result.Success<UserInfo>) user1InfoResult).value();

        ///////////////////////////
        // Create Default Roles  //
        ///////////////////////////

        Account account1 = new Account(accountInfo1, ctx);
        assertNotNull(account1);
        Library library1 = new Library(library1InfoId, ctx);
        assertNotNull(library1);

        // Create the App objects
        TestRoles testRoles = new TestRoles(
            account1,
            new Account(accountInfo2, ctx),
            new User(user1Info, account1, ctx),
            library1,
            new Book(UUID2.createFakeUUID2(1100, Book.class), null, ctx), // create ORPHANED book
            new Book(UUID2.createFakeUUID2(1200, Book.class), library1, ctx)
        );
        assertNotNull(testRoles);

        // print User1
        System.out.println();
        ctx.log.d(this,"User --> " + testRoles.user1.id + ", " + testRoles.user1.fetchInfo().toPrettyJson());

        return testRoles;
    }

    @Test
    public void Update_BookInfo_is_Success() {
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

        // Check the title for updated info
        assert(bookInfoResult instanceof Result.Success);
        assertEquals("The Updated Title", ((Result.Success<BookInfo>) bookInfoResult).value().title);
    }

    @Test
    public void Fetch_NonExisting_Book_is_Failure() {
        // • ARRANGE
        Context context = setupTestContext();

        // • ACT
        // Try to get a book id that doesn't exist - SHOULD FAIL
        Book book2 = new Book(UUID2.createFakeUUID2(99, Book.class), null, context);

        // • ASSERT
        assertFalse("Book SHOULD NOT Exist --> " + book2.id, book2.fetchInfoResult() instanceof Result.Success);
    }

    @Test
    public void Checkout_2_Books_to_User_is_Success() {
        // • ARRANGE
        Context ctx = setupTestContext();
        TestRoles roles = setupDefaultScenarioAndRoles(ctx);

        // • ACT
        final Result<Book> bookResult = roles.library1.checkOutBookToUser(roles.book1100, roles.user1);
        final Result<Book> bookResult2 = roles.library1.checkOutBookToUser(roles.book1200, roles.user1);

        // • ASSERT
        assertTrue("Checked out book FAILURE, bookId: " + roles.book1100.id, bookResult instanceof Result.Success);
        assertTrue("Checked out book FAILURE, bookId: " + roles.book1200.id, bookResult2 instanceof Result.Success);

        roles.library1.DumpDB(ctx);  // LEAVE for debugging
    }

    @Test
    public void List_Books_checked_out_by_User(){  // note: relies on Checkout_2_books_to_User
        // • ARRANGE
        Context ctx = setupTestContext();
        TestRoles roles = setupDefaultScenarioAndRoles(ctx);

        // Checkout 2 books
        final Result<Book> bookResult = roles.library1.checkOutBookToUser(roles.book1100, roles.user1);
        final Result<Book> bookResult2 = roles.library1.checkOutBookToUser(roles.book1200, roles.user1);

        // • ACT
        final Result<ArrayList<Book>> checkedOutBooksResult = roles.library1.findBooksCheckedOutByUser(roles.user1);
        assertTrue("findBooksCheckedOutByUser FAILURE for userId" + roles.user1.id, checkedOutBooksResult instanceof Result.Success);
        ArrayList<Book> checkedOutBooks = ((Result.Success<ArrayList<Book>>) checkedOutBooksResult).value();

        // Print checked out books
        System.out.println();
        ctx.log.d(this,"Checked Out Books for User [" + roles.user1.fetchInfo().name + ", " + roles.user1.id + "]:");
        for (Book book : checkedOutBooks) {
            final Result<BookInfo> bookInfoResult = book.fetchInfoResult();

            assertTrue("Book Error: bookId" + book.id,  bookInfoResult instanceof Result.Success);
            ctx.log.d(this, ((Result.Success<BookInfo>) bookInfoResult).value().toString());
        }

        int acceptedBookCount = ((Result.Success<ArrayList<Book>>) roles.user1.findAllAcceptedBooks()).value().size();
        assertEquals("acceptedBookCount != 2", 2, acceptedBookCount);
    }

}