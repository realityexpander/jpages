package org.elegantobjects.jpages.LibraryAppTest;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.testingUtils.TestingUtils;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.book.network.DTOBookInfo;
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
import java.util.HashMap;
import java.util.Map;

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
            ctx.log.e(this,"Create Library FAILURE --> " + libraryInfo);
            fail("Create Library FAILURE --> " + libraryInfo);
        }
        assertTrue(libraryInfo instanceof Result.Success);
        UUID2<Library> library1InfoId = ((Result.Success<LibraryInfo>) libraryInfo).value().id();
        ctx.log.d(this,"Library Created --> id: " + ((Result.Success<LibraryInfo>) libraryInfo).value().id() + ", name: "+ ((Result.Success<LibraryInfo>) libraryInfo).value().name);

        // Populate the library with 10 books
        ctx.libraryInfoRepo().populateWithFakeBooks(library1InfoId, 10);

        /////////////////////////////////
        // • Create Accounts for Users //
        /////////////////////////////////

//        // Create fake AccountInfo for User 1
//        AccountInfo accountInfo = new AccountInfo(
//            UUID2.createFakeUUID2(1, Account.class),
//            "User Name 1"
//        );
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
            fail("Book Missing --> book id: " + book.id() + " >> " + " is null");
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
        assertTrue("Book SHOULD NOT Exist, but does! --> " + book2.id, book2.fetchInfoResult() instanceof Result.Failure);
    }

    @Test
    public void CheckOut_2_Books_to_User_is_Success() {
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
    public void Find_Books_checkedOut_by_User_is_Success() {  // note: relies on Checkout_2_books_to_User
        // • ARRANGE
        Context ctx = setupTestContext();
        TestRoles roles = setupDefaultScenarioAndRoles(ctx);

        // Checkout 2 books
        final Result<Book> bookResult1 = roles.library1.checkOutBookToUser(roles.book1100, roles.user1);
        final Result<Book> bookResult2 = roles.library1.checkOutBookToUser(roles.book1200, roles.user1);

        // • ACT
        final Result<ArrayList<Book>> checkedOutBooksResult = roles.library1.findBooksCheckedOutByUser(roles.user1);
        assertTrue("findBooksCheckedOutByUser FAILURE for userId" + roles.user1.id, checkedOutBooksResult instanceof Result.Success);
        ArrayList<Book> checkedOutBooks = ((Result.Success<ArrayList<Book>>) checkedOutBooksResult).value();

        // • ASSERT
        ctx.log.d(this,"Checked Out Books for User [" + roles.user1.fetchInfo().name + ", " + roles.user1.id + "]:");
        for (Book book : checkedOutBooks) {
            final Result<BookInfo> bookInfoResult = book.fetchInfoResult();

            assertTrue("Book Error: bookId" + book.id,  bookInfoResult instanceof Result.Success);
            ctx.log.d(this, ((Result.Success<BookInfo>) bookInfoResult).value().toString());
        }

        int acceptedBookCount = ((Result.Success<ArrayList<Book>>) roles.user1.findAllAcceptedBooks()).value().size();
        assertEquals("acceptedBookCount != 2", 2, acceptedBookCount);
    }

    @Test
    public void Calculate_availableBook_To_numAvailable_Map_is_Success() {
        // • ARRANGE
        Context ctx = setupTestContext();
        TestRoles roles = setupDefaultScenarioAndRoles(ctx);

        // Checkout 2 books
        final Result<Book> bookResult1 = roles.library1.checkOutBookToUser(roles.book1100, roles.user1);
        final Result<Book> bookResult2 = roles.library1.checkOutBookToUser(roles.book1200, roles.user1);

        final Result<HashMap<Book, Long>> availableBookToNumAvailableResult =
                roles.library1.calculateAvailableBookIdToNumberAvailableList();
        assertTrue("findBooksCheckedOutByUser FAILURE for libraryId" + roles.library1.id,
            availableBookToNumAvailableResult instanceof Result.Success
        );

        // create objects and populate info for available books
        final HashMap<Book, Long> availableBooks =
                ((Result.Success<HashMap<Book, Long>>) availableBookToNumAvailableResult).value();
        assertNotNull(availableBooks);

        // Print out available books
        System.out.println();
        ctx.log.d(this,"Available Books in Library:");
        for (Map.Entry<Book, Long> availableBook : availableBooks.entrySet()) {

            final Result<BookInfo> bookInfoResult =
                    availableBook.getKey()
                            .fetchInfoResult();

            assertTrue("Book Error: bookId" + availableBook.getKey().id,  bookInfoResult instanceof Result.Success);
            ctx.log.d(this, ((Result.Success<BookInfo>) bookInfoResult).value().toString());
        }
        ctx.log.d(this,"Total Available Books (unique UUIDs): " + availableBooks.size());
        assertEquals("availableBooks.size() != 10", 10, availableBooks.size());
    }

    @Test
    public void CheckOut_and_CheckIn_Book_to_Library_is_Success() {
        // • ARRANGE
        Context ctx = setupTestContext();
        TestRoles roles = setupDefaultScenarioAndRoles(ctx);
        int initialBookCount = ((Result.Success<ArrayList<Book>>) roles.user1.findAllAcceptedBooks()).value().size();

        // • ACT & ASSERT

        // First check out book
        Result<UUID2<Book>> checkoutResult = roles.user1.checkOutBookFromLibrary(roles.book1200, roles.library1);
        assertTrue("Checked out book FAILURE --> book id:" + roles.book1200.id, checkoutResult instanceof Result.Success);
        int afterCheckOutBookCount = ((Result.Success<ArrayList<Book>>) roles.user1.findAllAcceptedBooks()).value().size();
        assertEquals("afterCheckOutBookCount != initialAcceptedBookCount+1", afterCheckOutBookCount, initialBookCount + 1);

        // Now check in Book
        final Result<Book> checkInBookResult = roles.library1.checkInBookFromUser(roles.book1200, roles.user1);
        assertTrue("Checked out book FAILURE --> book id:" + roles.book1200.id, checkInBookResult instanceof Result.Success);
        int afterCheckInBookCount = ((Result.Success<ArrayList<Book>>) roles.user1.findAllAcceptedBooks()).value().size();
        assertEquals("afterCheckInBookCount != initialBookCount", afterCheckInBookCount, initialBookCount);

        roles.library1.DumpDB(ctx);
    }

    private String getRonaldReaganLibraryJson() {
        return
            "{\n" +
            "  \"id\": {\n" +
            "    \"uuid\": \"00000000-0000-0000-0000-000000000099\",\n" +
            "    \"_uuid2Type\": \"Role.Library\"\n" +
            "  },\n" +
            "  \"name\": \"Ronald Reagan Library\",\n" +
            "  \"registeredUserIdToCheckedOutBookIdMap\": {\n" +
            "    \"uuid2ToEntityMap\": {\n" +
            "      \"UUID2:Role.User@00000000-0000-0000-0000-000000000001\": []\n" +
            "    }\n" +
            "  },\n" +
            "  \"bookIdToNumBooksAvailableMap\": {\n" +
            "    \"uuid2ToEntityMap\": {\n" +
            "      \"UUID2:Role.Book@00000000-0000-0000-0000-000000001400\": 25,\n" +
            "      \"UUID2:Role.Book@00000000-0000-0000-0000-000000001000\": 25,\n" +
            "      \"UUID2:Role.Book@00000000-0000-0000-0000-000000001300\": 25,\n" +
            "      \"UUID2:Role.Book@00000000-0000-0000-0000-000000001200\": 25,\n" +
            "      \"UUID2:Role.Book@00000000-0000-0000-0000-000000001500\": 25,\n" +
            "      \"UUID2:Role.Book@00000000-0000-0000-0000-000000001600\": 25,\n" +
            "      \"UUID2:Role.Book@00000000-0000-0000-0000-000000001700\": 25,\n" +
            "      \"UUID2:Role.Book@00000000-0000-0000-0000-000000001800\": 25,\n" +
            "      \"UUID2:Role.Book@00000000-0000-0000-0000-000000001900\": 25,\n" +
            "      \"UUID2:Role.Book@00000000-0000-0000-0000-000000001100\": 25\n" +
            "    }\n" +
            "  }\n" +
            "}";
    }

    @Test
    public void Update_LibraryInfo_by_updateInfoFromJson_is_Success() {
        // • ARRANGE
        Context ctx = setupTestContext();
        String json = getRonaldReaganLibraryJson();

        // Create the "unknown" library with just an id.
        Library library2 = new Library(UUID2.createFakeUUID2(99, Library.class), ctx);
        Book book1500 = new Book(UUID2.createFakeUUID2(1500, Book.class), null, ctx);

        // • ASSERT
        // Get empty info object.
        ctx.log.d(this, library2.toJson());
        assertEquals("afterCheckInBookCount != initialBookCount", "{}", library2.toJson());

        // Check JSON loaded properly
        Result<LibraryInfo> library2Result = library2.updateInfoFromJson(json);
        if (library2Result instanceof Result.Failure) {
            // NOTE: FAILURE IS EXPECTED HERE
            ctx.log.d(this, "^^^^^^^^ warning is expected and normal.");

            // Since the library2 was not saved in the central database, we will get a "library not found error" which is expected
            ctx.log.d(this, ((Result.Failure<LibraryInfo>) library2Result).exception().getMessage());

            // The JSON was still loaded properly
            ctx.log.d(this, "Results of Library2 json load:" + library2.toJson());

            // LEAVE FOR REFERENCE
            // Note: Can't just do simple "text equality" check on Json as the ordering of the `bookIdToNumBooksAvailableMap` is random
            // // assert library2.toJson().equals(json);
            // // if(!library2.toJson().equals(json)) throw new Exception("Library2 JSON not equal to expected JSON");

            // check for same number of items
            assertEquals("Library2 should have 10 books", 10, ((Result.Success<HashMap<Book, Long>>)
                    library2.calculateAvailableBookIdToNumberAvailableList()).value().size());

            // check existence of a particular book
            assertTrue("Library2 should have known Book with id 1500",
                    library2.isKnownBook(book1500));

        } else {
            // Intentionally should NOT see this branch bc the library2 was never saved to the central database/api.
            ctx.log.d(this, "Results of Library2 json load:");
            ctx.log.d(this, library2.toJson());
            fail("Library2 JSON load should have failed");
        }
    }

    @Test
    public void Create_Library_Role_from_createInfoFromJson_is_Success() {
        // • ARRANGE
        Context ctx = setupTestContext();
        String json = getRonaldReaganLibraryJson();
        Book expectedBook1900 = new Book(UUID2.createFakeUUID2(1900, Book.class), null, ctx);

        // Create a Library Domain Object from the Info
        try {

            // • ACT
            LibraryInfo libraryInfo =
                Library.createInfoFromJson(
                    json,
                    LibraryInfo.class,
                    ctx
                );
            assertNotNull(libraryInfo);

            Library library = new Library(libraryInfo, ctx);
            ctx.log.d(this, "Results of Library3 json load:" + library.toJson());

            // • ASSERT
            // check for same number of items
            assertEquals("Library2 should have 10 books",
                    10, ((Result.Success<HashMap<Book, Long>>)
                            library.calculateAvailableBookIdToNumberAvailableList()).value().size());

            // check existence of a particular book
            assertTrue("Library2 should have known Book with id="+ expectedBook1900.id, library.isKnownBook(expectedBook1900));
        } catch (Exception e) {
            ctx.log.e(this, "Exception: " + e.getMessage());
            fail(e.getMessage());
        }

    }

    private String getGreatGatsbyBookJson() {
        return
            "{\n" +
            "  \"id\": {\n" +
            "    \"uuid\": \"00000000-0000-0000-0000-000000000010\",\n" +
            "    \"uuid2Type\": \"Model.DTOInfo.BookInfo\"\n" +
            "  },\n" +
            "  \"title\": \"The Great Gatsby\",\n" +
            "  \"author\": \"F. Scott Fitzgerald\",\n" +
            "  \"description\": \"The Great Gatsby is a 1925 novel written by American author F. Scott Fitzgerald that follows a cast of characters living in the fictional towns of West Egg and East Egg on prosperous Long Island in the summer of 1922. The story primarily concerns the young and mysterious millionaire Jay Gatsby and his quixotic passion and obsession with the beautiful former debutante Daisy Buchanan. Considered to be Fitzgerald's magnum opus, The Great Gatsby explores themes of decadence, idealism, resistance to change, social upheaval, and excess, creating a portrait of the Jazz Age or the Roaring Twenties that has been described as a cautionary tale regarding the American Dream.\",\n" +
            "  \"extraFieldToShowThisIsADTO\": \"Extra Unneeded Data from JSON payload load\"\n" +
            "}";
    }

    @Test
    public void Create_Book_Role_from_DTOInfo_Json() {
        // • ARRANGE
        Context ctx = setupTestContext();
        String json = getGreatGatsbyBookJson();
        String expectedTitle = "The Great Gatsby";
        String expectedAuthor = "F. Scott Fitzgerald";
        UUID2<Book> expectedUUID2 = UUID2.createFakeUUID2(10, Book.class);
        String expectedUuid2Type = expectedUUID2.uuid2TypeStr();

        // • ACT & ASSERT
        try {
            DTOBookInfo dtoBookInfo3 = new DTOBookInfo(json, ctx);
            assertNotNull(dtoBookInfo3);

            Book book3 = new Book(new BookInfo(dtoBookInfo3), null, ctx);
            assertNotNull(book3);

            ctx.log.d(this,"Results of load BookInfo from DTO Json: " + book3.toJson());

            assertEquals("Book3 should have title:" + expectedTitle,
                    expectedTitle, book3.info().title);
            assertEquals("Book3 should have author:" + expectedAuthor,
                    expectedAuthor, book3.info().author);
            assertEquals("Book3 should have id: " + expectedUUID2,
                    expectedUUID2, book3.id);
            assertEquals("Book3 should have UUID2 Type of:" + expectedUuid2Type,
                    expectedUuid2Type, book3.id.uuid2TypeStr());
        } catch (Exception e) {
            ctx.log.e(this, "Exception: " + e.getMessage());
            fail(e.getMessage());
        }
    }

}