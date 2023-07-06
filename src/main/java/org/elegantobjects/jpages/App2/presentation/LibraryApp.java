package org.elegantobjects.jpages.App2.presentation;


import org.elegantobjects.jpages.App2.common.util.log.Log;
import org.elegantobjects.jpages.App2.common.util.testingUtils.TestingUtils;
import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.book.network.DTOBookInfo;
import org.elegantobjects.jpages.App2.domain.account.Account;
import org.elegantobjects.jpages.App2.domain.account.AccountInfo;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.Context;
import org.elegantobjects.jpages.App2.domain.book.BookInfo;
import org.elegantobjects.jpages.App2.domain.library.LibraryInfo;
import org.elegantobjects.jpages.App2.domain.user.UserInfo;
import org.elegantobjects.jpages.App2.domain.library.Library;
import org.elegantobjects.jpages.App2.domain.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.*;


final class LibraryApp {

    final Context ctx;

    public static void main(final String... args) throws Exception {

        // Setup App Context Object singletons
        Context productionContext = Context.setupProductionInstance(new Log());

        // Note: Check the tests!
        new LibraryApp(productionContext);
    }

    // Library App - Domain Layer Root Object
    // Note: Most of this has been moved to the tests. This is just a reference for how to use the Domain Layer.
    public LibraryApp(@NotNull Context ctx) throws Exception {
        //context = Context.setupINSTANCE(context);  // For implementing a static Context. LEAVE for reference
        this.ctx = ctx;
        TestingUtils testUtil = new TestingUtils(ctx);

        ctx.log.d(this,"Populating Book DB and API");
        testUtil.PopulateFakeBookInfoInContextBookRepoDBandAPI();

        // Create fake AccountInfo
        AccountInfo accountInfo = new AccountInfo(
            UUID2.createFakeUUID2(1, Account.class),
            "User Name 1"
        );

        Populate_And_Poke_Book:
        if (true)
        {
            System.out.println();
            ctx.log.d(this, "Populate_And_Poke_Book");
            ctx.log.d(this, "----------------------------------");

            // Create a book object (it only has an id)
            Book book = new Book(UUID2.createFakeUUID2(1100, Book.class), null, ctx);
            ctx.log.d(this, book.fetchInfoResult().toString());

            // Update info for a book
            final Result<BookInfo> bookInfoResult =
                book.updateInfo(
                    new BookInfo(
                        book.id(),
                        "The Updated Title",
                        "The Updated Author",
                        "The Updated Description"
                    ));
            ctx.log.d(this, book.fetchInfoResult().toString());

            // Get the bookInfo (null if not loaded)
            BookInfo bookInfo3 = book.fetchInfo();
            if (bookInfo3 == null) {
                ctx.log.d(this, "Book Missing --> book id: " + book.id() + " >> " + " is null");
                assert false;
            } else
                ctx.log.d(this, "Book Info --> " + bookInfo3);

            // Try to get a book id that doesn't exist
            Book book2 = new Book(UUID2.createFakeUUID2(1200, Book.class), null, ctx);
            if (book2.fetchInfoResult() instanceof Result.Failure) {
                ctx.log.d(this, "Get Book Should fail : FAILURE --> book id: " + book2.id() + " >> " + ((Result.Failure<BookInfo>) book2.fetchInfoResult()));
                assert true; // should fail
            } else
                ctx.log.d(this, "Book Exists --> " + ((Result.Success<BookInfo>) book2.fetchInfoResult()).value());

            testUtil.DumpBookDBandAPI();
        }

        Populate_the_library_and_user_DBs:
        {
            ////////////////////////////////////////
            // Setup DB & API simulated resources //
            ////////////////////////////////////////

            // Create & populate a Library in the Library Repo
            final Result<LibraryInfo> libraryInfo = testUtil.createFakeLibraryInfoInContextLibraryRepo(1);
            if (libraryInfo instanceof Result.Failure) {
                ctx.log.d(this,"Create Library FAILURE --> " + ((Result.Failure<LibraryInfo>) libraryInfo));

                break Populate_the_library_and_user_DBs;
            }
            UUID2<Library> library1InfoId = ((Result.Success<LibraryInfo>) libraryInfo).value().id();
            ctx.log.d(this,"Library Created --> id: " + ((Result.Success<LibraryInfo>) libraryInfo).value().id() + ", name: "+ ((Result.Success<LibraryInfo>) libraryInfo).value().name);

            // Populate the library
            ctx.libraryInfoRepo().populateWithFakeBooks(library1InfoId, 10);

            // create Accounts for Users
            final Result<AccountInfo> accountInfo1Result = testUtil.createFakeAccountInfoInContextAccountRepo(1);
            final Result<AccountInfo> accountInfo2Result = testUtil.createFakeAccountInfoInContextAccountRepo(2);
            assert accountInfo1Result != null;  // assume success
            assert accountInfo2Result != null;  // assume success
            final AccountInfo accountInfo1 = ((Result.Success<AccountInfo>) accountInfo1Result).value(); // assume success
            final AccountInfo accountInfo2 = ((Result.Success<AccountInfo>) accountInfo2Result).value(); // assume success

            // Create & populate User1 in the User Repo for the Context
            final Result<UserInfo> user1InfoResult = testUtil.createFakeUserInfoInContextUserInfoRepo(1);
            if(user1InfoResult == null) throw new Exception("user1InfoResult is null");
            final UserInfo user1Info = ((Result.Success<UserInfo>) user1InfoResult).value(); // assume success

            //////////////////////////////////
            // Actual App functionality     //
            //////////////////////////////////

            // Create the App objects
            final Account account1 = new Account(accountInfo1, ctx);
            final Account account2 = new Account(accountInfo2, ctx);
            final User user1 = new User(user1Info, account1, ctx);
            final Library library1 = new Library(library1InfoId, ctx);
            final Book book1100 = new Book(UUID2.createFakeUUID2(1100, Book.class), null, ctx); // create ORPHANED book
            final Book book1200 = new Book(UUID2.createFakeUUID2(1200, Book.class), library1, ctx);

            // print User 1
            System.out.println();
            ctx.log.d(this,"User --> " + user1.id() + ", " + user1.fetchInfo().toPrettyJson(ctx));

            Checkout_2_Books_to_User:
            if (true) {
                System.out.println();
                ctx.log.d(this,"Checking out 2 books to user " + user1.id());
                ctx.log.d(this, "----------------------------------");

                final Result<Book> bookResult = library1.checkOutBookToUser(book1100, user1);
                if (bookResult instanceof Result.Failure) {
                    ctx.log.e(this, "Checked out book FAILURE--> " + ((Result.Failure<Book>) bookResult).exception().getMessage());
                    throw new Exception("Checked out book FAILURE--> " + ((Result.Failure<Book>) bookResult).exception().getMessage());
                } else
                    ctx.log.d(this, "Checked out book SUCCESS --> " + ((Result.Success<Book>) bookResult).value().id());

                System.out.println();
                final Result<Book> bookResult2 = library1.checkOutBookToUser(book1200, user1);
                if (bookResult2 instanceof Result.Failure) {
                    ctx.log.e(this, "Checked out book FAILURE--> " + ((Result.Failure<Book>) bookResult2).exception().getMessage());
                    throw new Exception("Checked out book FAILURE--> " + ((Result.Failure<Book>) bookResult2).exception().getMessage());
                } else
                    ctx.log.d(this, "Checked out book SUCCESS --> " + ((Result.Success<Book>) bookResult2).value().id());

                library1.DumpDB(ctx);  // LEAVE for debugging
            }

            List_Books_checked_out_by_User:  // note: relies on Checkout_2_books_to_User
            if (true) {
                System.out.println();
                ctx.log.d(this,"Getting books checked out by user " + user1.id());
                ctx.log.d(this, "----------------------------------");

                final Result<ArrayList<Book>> checkedOutBooksResult = library1.findBooksCheckedOutByUser(user1);
                if (checkedOutBooksResult instanceof Result.Failure) {
                    ctx.log.d(this, "OH NO! --> " + ((Result.Failure<ArrayList<Book>>) checkedOutBooksResult).exception().getMessage());
                    throw new Exception("OH NO! --> " + ((Result.Failure<ArrayList<Book>>) checkedOutBooksResult).exception().getMessage());
                }

                assert checkedOutBooksResult instanceof Result.Success;
                ArrayList<Book> checkedOutBooks = ((Result.Success<ArrayList<Book>>) checkedOutBooksResult).value();

                // Print checked out books
                System.out.println();
                ctx.log.d(this,"Checked Out Books for User [" + user1.fetchInfo().name + ", " + user1.id() + "]:");
                for (Book book : checkedOutBooks) {
                    final Result<BookInfo> bookInfoResult = book.fetchInfoResult();
                    if (bookInfoResult instanceof Result.Failure)
                        ctx.log.e(this, "Book Error: " + ((Result.Failure<BookInfo>) bookInfoResult).exception().getMessage());
                    else
                        ctx.log.d(this, ((Result.Success<BookInfo>) bookInfoResult).value().toString());
                }

                int acceptedBookCount = ((Result.Success<ArrayList<Book>>) user1.findAllAcceptedBooks()).value().size();
                if(acceptedBookCount != 2) throw new Exception("acceptedBookCount != 2");
            }

            List_available_Books_and_Inventory_Counts_in_Library:
            if (true)  {
                System.out.println();
                ctx.log.d(this,"\nGetting available books and counts in library:");
                ctx.log.d(this, "----------------------------------");

                final Result<HashMap<Book, Long>> availableBookToNumAvailableResult =
                        library1.calculateAvailableBookIdToNumberAvailableList();
                if (availableBookToNumAvailableResult instanceof Result.Failure) {
                    ctx.log.d(this,"AvailableBookIdCounts FAILURE! --> " + ((Result.Failure<HashMap<Book, Long>>) availableBookToNumAvailableResult).exception().getMessage());
                    throw new Exception("AvailableBookIdCounts FAILURE! --> " + ((Result.Failure<HashMap<Book, Long>>) availableBookToNumAvailableResult).exception().getMessage());
                }

                // create objects and populate info for available books
                assert availableBookToNumAvailableResult instanceof Result.Success;
                final HashMap<Book, Long> availableBooks =
                        ((Result.Success<HashMap<Book, Long>>) availableBookToNumAvailableResult).value();
                if(availableBooks == null) throw new Exception("availableBooks is null");

                // Print out available books
                System.out.println();
                ctx.log.d(this,"Available Books in Library:");
                for (Map.Entry<Book, Long> availableBook : availableBooks.entrySet()) {

                    final Result<BookInfo> bookInfoResult =
                            availableBook.getKey()
                                    .fetchInfoResult();
                    if (bookInfoResult instanceof Result.Failure)
                        ctx.log.e(this, "Book Error: " + ((Result.Failure<BookInfo>) bookInfoResult).exception().getMessage());
                    else
                        ctx.log.d(this, ((Result.Success<BookInfo>) bookInfoResult).value() + " >> num available: " + availableBook.getValue());
                }
                ctx.log.d(this,"Total Available Books (unique UUIDs): " + availableBooks.size());
                if(availableBooks.size() != 10) throw new Exception("availableBooks.size() != 10");
            }

            Check_Out_and_check_In_Book_from_User_to_Library:
            if (true) {
                System.out.println();
                ctx.log.d(this,"Check in book:" + book1200.id() + ", from user: " + user1.id() + ", to library:" + library1.id());
                ctx.log.d(this, "----------------------------------");

                int acceptedBookCount = ((Result.Success<ArrayList<Book>>) user1.findAllAcceptedBooks()).value().size();

                // First check out a book
                if(!user1.hasAcceptedBook(book1200)) {
                    Result<UUID2<Book>> checkoutResult = user1.checkOutBookFromLibrary(book1200, library1);
                    if (checkoutResult instanceof Result.Success)
                        ctx.log.d(this, "Checked out book SUCCESS --> book id:" + ((Result.Success<UUID2<Book>>) checkoutResult).value());
                    else
                        ctx.log.e(this, "Checked out book FAILURE --> book id:" + ((Result.Failure<UUID2<Book>>) checkoutResult).exception().getMessage());

                    int afterCheckOutBookCount = ((Result.Success<ArrayList<Book>>) user1.findAllAcceptedBooks()).value().size();
                    if(afterCheckOutBookCount != acceptedBookCount+1) throw new Exception("afterCheckOutBookCount != numBooksAccepted+1");
                }

                acceptedBookCount = ((Result.Success<ArrayList<Book>>) user1.findAllAcceptedBooks()).value().size();


                final Result<Book> checkInBookResult = library1.checkInBookFromUser(book1200, user1);
                if (checkInBookResult instanceof Result.Failure)
                    ctx.log.e(this, "Check In book FAILURE --> book id:" + ((Result.Failure<Book>) checkInBookResult).exception().getMessage());
                else
                    ctx.log.d(this, "Returned Book SUCCESS --> book id:" + ((Result.Success<Book>) checkInBookResult).value().id());

                int afterCheckInBookCount = ((Result.Success<ArrayList<Book>>) user1.findAllAcceptedBooks()).value().size();
                if(afterCheckInBookCount != acceptedBookCount-1) throw new Exception("afterNumBooksAccepted != acceptedBookCount-1");

                library1.DumpDB(ctx);
            }

            // Load Library from Json
            if (true) {
                System.out.println();
                ctx.log.d(this,"Load Library from Json: ");
                ctx.log.d(this, "----------------------------------");

                // Create the "unknown" library with just an id.
                Library library2 = new Library(UUID2.createFakeUUID2(99, Library.class), ctx);

                // Show empty info object.
                ctx.log.d(this, library2.toJson());
                if(!Objects.equals(library2.toJson(), "{}")) throw new Exception("library2.toJson() != {}");

                String json =
                    "{\n" +
                    "  \"_id\": {\n" +
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

                // Check JSON loaded properly
                if(true) {
                    System.out.println();
                    ctx.log.d(this,"Check JSON loaded properly: ");

                    Result<LibraryInfo> library2Result = library2.updateInfoFromJson(json);
                    if (library2Result instanceof Result.Failure) {
                        // NOTE: FAILURE IS EXPECTED HERE
                        ctx.log.d(this, "^^^^^^^^ warning is expected and normal.");

                        // Since the library2 was not saved in the central database, we will get a "library not found error" which is expected
                        ctx.log.d(this, ((Result.Failure<LibraryInfo>) library2Result).exception().getMessage());

                        // The JSON was still loaded properly
                        ctx.log.d(this, "Results of Library2 json load:" + library2.toJson());

                        // Can't just check json as the ordering of the bookIdToNumBooksAvailableMap is random - LEAVE FOR REFERENCE
                        // assert library2.toJson().equals(json);
                        // if(!library2.toJson().equals(json)) throw new Exception("Library2 JSON not equal to expected JSON");

                        // check for same number of items
                        if( ((Result.Success<HashMap<Book, Long>>) library2.calculateAvailableBookIdToNumberAvailableList()).value().size() != 10)
                            throw new Exception("Library2 should have 10 books");

                        // check existence of a particular book
                        if (library2.isUnknownBook(new Book(UUID2.createFakeUUID2(1500, Book.class), null, ctx)))
                            throw new Exception("Library2 should have known Book with id 1500");

                    } else {
                        // Intentionally Wont see this branch bc the library2 was never saved to the central database/api.
                        ctx.log.d(this, "Results of Library2 json load:");
                        ctx.log.d(this, library2.toJson());
                        throw new Exception("Library2 JSON load should have failed");
                    }
                }

                // Create a Library Domain Object from the Info
                if(true) {
                    System.out.println();
                    ctx.log.d(this,"Create Library from LibraryInfo: ");
                    ctx.log.d(this, "----------------------------------");

                    try {
                        LibraryInfo libraryInfo3 =
                            Library.createInfoFromJson(
                                json,
                                LibraryInfo.class,
                                ctx
                            );

                        assert libraryInfo3 != null;
                        Library library3 = new Library(libraryInfo3, ctx);
                        ctx.log.d(this, "Results of Library3 json load:" + library3.toJson());

                        // check for same number of items
                        if( ((Result.Success<HashMap<Book, Long>>) library3.calculateAvailableBookIdToNumberAvailableList()).value().size() != 10)
                            throw new Exception("Library2 should have 10 books");

                        // check existence of a particular book
                        if (library3.isUnknownBook(new Book(UUID2.createFakeUUID2(1900, Book.class), null, ctx)))
                            throw new Exception("Library2 should have known Book with id 1900");


                    } catch (Exception e) {
                        ctx.log.e(this, "Exception: " + e.getMessage());
                        throw e;
                    }
                }
            }

            // Load Book from DTO Json
            if (true) {
                System.out.println();
                ctx.log.d(this,"Load BookInfo from DTO Json: ");
                ctx.log.d(this, "----------------------------------");

                String json =
                    "{\n" +
                    "  \"_id\": {\n" +
                    "    \"uuid\": \"00000000-0000-0000-0000-000000000010\",\n" +
                    "    \"uuid2TypeStr\": \"Model.DTOInfo.BookInfo\"\n" +
                    "  },\n" +
                    "  \"title\": \"The Great Gatsby\",\n" +
                    "  \"author\": \"F. Scott Fitzgerald\",\n" +
                    "  \"description\": \"The Great Gatsby is a 1925 novel written by American author F. Scott Fitzgerald that follows a cast of characters living in the fictional towns of West Egg and East Egg on prosperous Long Island in the summer of 1922. The story primarily concerns the young and mysterious millionaire Jay Gatsby and his quixotic passion and obsession with the beautiful former debutante Daisy Buchanan. Considered to be Fitzgerald's magnum opus, The Great Gatsby explores themes of decadence, idealism, resistance to change, social upheaval, and excess, creating a portrait of the Jazz Age or the Roaring Twenties that has been described as a cautionary tale regarding the American Dream.\",\n" +
                    "  \"extraFieldToShowThisIsADTO\": \"Extra Unneeded Data from JSON payload load\"\n" +
                    "}";

                try {
                    DTOBookInfo dtoBookInfo3 = new DTOBookInfo(json, ctx);
                    Book book3 = new Book(new BookInfo(dtoBookInfo3), null, ctx);

                    ctx.log.d(this,"Results of load BookInfo from DTO Json: " + book3.toJson());
                } catch (Exception e) {
                    ctx.log.e(this, "Exception: " + e.getMessage());
                    throw e;
                }
            }

            // Load Book from DTO Json using DTO Book constructor
            if (true) {
                System.out.println();
                ctx.log.d(this,"Load Book from DTO Json using DTO Book constructor: ");
                ctx.log.d(this, "----------------------------------");

                String json =
                        "{\n" +
                        "  \"_id\": {\n" +
                        "    \"uuid\": \"00000000-0000-0000-0000-000000000010\",\n" +
                        "    \"uuid2TypeStr\": \"Model.DTOInfo.DTOBookInfo\"\n" +
                        "  },\n" +
                        "  \"title\": \"The Great Gatsby\",\n" +
                        "  \"author\": \"F. Scott Fitzgerald\",\n" +
                        "  \"description\": \"The Great Gatsby is a 1925 novel written by American author F. Scott Fitzgerald that follows a cast of characters living in the fictional towns of West Egg and East Egg on prosperous Long Island in the summer of 1922. The story primarily concerns the young and mysterious millionaire Jay Gatsby and his quixotic passion and obsession with the beautiful former debutante Daisy Buchanan. Considered to be Fitzgerald's magnum opus, The Great Gatsby explores themes of decadence, idealism, resistance to change, social upheaval, and excess, creating a portrait of the Jazz Age or the Roaring Twenties that has been described as a cautionary tale regarding the American Dream.\",\n" +
                        "  \"extraFieldToShowThisIsADTO\": \"Extra Unneeded Data from JSON payload load\"\n" +
                        "}";

                try {
                    DTOBookInfo dtoBookInfo3 = new DTOBookInfo(json, ctx);
                    Book book3 = new Book(dtoBookInfo3, null, ctx); // passing in DTO directly to Book constructor

                    ctx.log.d(this,"Results of load BookInfo from DTO Json: " + book3.toJson());
                } catch (Exception e) {
                    ctx.log.e(this, "Exception: " + e.getMessage());
                    throw e;
                }
            }

            Check_out_Book_via_User:
            if (true) {
                System.out.println();
                ctx.log.d(this,"Check_out_Book_via_User: ");
                ctx.log.d(this, "----------------------------------");

                final Result<UserInfo> user2InfoResult = testUtil.createFakeUserInfoInContextUserInfoRepo(2);
                assert user2InfoResult != null;
                final User user2 = new User(((Result.Success<UserInfo>) user2InfoResult).value(), account2 , ctx);
                final Result<BookInfo> book12Result = testUtil.addFakeBookInfoInContextBookInfoRepo(12);

                if (book12Result instanceof Result.Failure) {
                    ctx.log.e(this,"Book Error: " + ((Result.Failure<BookInfo>) book12Result).exception().getMessage());
                    throw ((Result.Failure<BookInfo>) book12Result).exception();
                } else {

                    final UUID2<Book> book12id = ((Result.Success<BookInfo>) book12Result).value().id();
                    final Book book12 = new Book(book12id, null, ctx);

                    System.out.println();
                    ctx.log.d(this,"Check out book " + book12id + " to user " + user1.id());

                    final Result<Book> book12UpsertResult = library1.addTestBookToLibrary(book12, 1);
                    if (book12UpsertResult instanceof Result.Failure) {
                        ctx.log.d(this, "Upsert Book Error: " + ((Result.Failure<Book>) book12UpsertResult).exception().getMessage());
                        throw ((Result.Failure<Book>) book12UpsertResult).exception();
                    }

                    final Result<UUID2<Book>> checkedOutBookResult = user2.checkOutBookFromLibrary(book12, library1);
                    if (checkedOutBookResult instanceof Result.Failure) {
                        ctx.log.d(this, "Checkout book FAILURE --> " + ((Result.Failure<UUID2<Book>>) checkedOutBookResult).exception().getMessage());
                        throw ((Result.Failure<UUID2<Book>>) checkedOutBookResult).exception();
                    }
                    else
                        ctx.log.d(this, "Checkout Book SUCCESS --> checkedOutBook:" + ((Result.Success<UUID2<Book>>) checkedOutBookResult).value());
                }
            }

            Give_Book_To_User:
            if (true) {
                System.out.println();
                ctx.log.d(this,"Give_Book_To_User: ");
                ctx.log.d(this, "----------------------------------");

                final Result<UserInfo> user01InfoResult = testUtil.createFakeUserInfoInContextUserInfoRepo(1);
                assert user01InfoResult != null;
                final User user01 = new User(((Result.Success<UserInfo>) user01InfoResult).value(), account1 , ctx);

                final Result<UserInfo> user2InfoResult = testUtil.createFakeUserInfoInContextUserInfoRepo(2);
                assert user2InfoResult != null;
                final User user2 = new User(((Result.Success<UserInfo>) user2InfoResult).value(), account2, ctx);

                final Result<BookInfo> book12InfoResult = testUtil.addFakeBookInfoInContextBookInfoRepo(12);

                if (book12InfoResult instanceof Result.Failure) {
                    ctx.log.e(this,"Book Error: " +
                        ((Result.Failure<BookInfo>) book12InfoResult).exception().getMessage()
                    );
                } else {

                    final UUID2<Book> book12id = ((Result.Success<BookInfo>) book12InfoResult).value().id();
                    final Book book12 = new Book(book12id, null, ctx);

                    user2.acceptBook(book12); // no library involved.

                    ctx.log.d(this,"User (2):" + user2.id() + " Give Book:" + book12id + " to User(1):" + user01.id());

                    final Result<ArrayList<UUID2<Book>>> giveBookToUserResult = user2.giveBookToUser(book12, user01);
                    if (giveBookToUserResult instanceof Result.Failure) {
                        ctx.log.d(this, "Give book FAILURE --> Book:" + ((Result.Failure<ArrayList<UUID2<Book>>>) giveBookToUserResult).exception().getMessage());
                        throw ((Result.Failure<ArrayList<UUID2<Book>>>) giveBookToUserResult).exception();
                    } else
                        ctx.log.d(this, "Give Book SUCCESS --> Book:" + ((Result.Success<ArrayList<UUID2<Book>>>) giveBookToUserResult).value());
                }
            }

            Give_Checked_Out_Book_From_User_To_User:
            if (true) {
                System.out.println();
                ctx.log.d(this,"Transfer_Checked_Out_Book_From_User_To_User: ");
                ctx.log.d(this, "----------------------------------");

                final Result<UserInfo> user01InfoResult = testUtil.createFakeUserInfoInContextUserInfoRepo(1);
                assert user01InfoResult != null;
                final User user01 = new User(((Result.Success<UserInfo>) user01InfoResult).value(), account1 , ctx);

                final Result<UserInfo> user2InfoResult = testUtil.createFakeUserInfoInContextUserInfoRepo(2);
                assert user2InfoResult != null;
                final User user2 = new User(((Result.Success<UserInfo>) user2InfoResult).value(), account2, ctx);

                final Result<BookInfo> book12InfoResult = testUtil.addFakeBookInfoInContextBookInfoRepo(12);
                assert book12InfoResult != null;
                final UUID2<Book> book12id = ((Result.Success<BookInfo>) book12InfoResult).value().id();
                final Book book12 = new Book(book12id, library1, ctx);

                // Add book12 to library1
                final Result<Book> book12UpsertResult = library1.addTestBookToLibrary(book12, 1);
                if (book12UpsertResult instanceof Result.Failure) {
                    ctx.log.e(this, "Upsert Book Error: " + ((Result.Failure<Book>) book12UpsertResult).exception().getMessage());
                    throw ((Result.Failure<Book>) book12UpsertResult).exception();
                }

                // Register user1 to library1
                final Result<UUID2<User>> user01UpsertResult = library1.info().registerUser(user01.id());
                if (user01UpsertResult instanceof Result.Failure) {
                    ctx.log.e(this, "Upsert User Error: " + ((Result.Failure<UUID2<User>>) user01UpsertResult).exception().getMessage());
                    throw ((Result.Failure<UUID2<User>>) user01UpsertResult).exception();
                }

                // Make user2 checkout book12 from library1
                final Result<UUID2<Book>> checkedOutBookResult = user2.checkOutBookFromLibrary(book12, library1);
                if (checkedOutBookResult instanceof Result.Failure) {
                    ctx.log.e(this, "Checkout book FAILURE --> " + ((Result.Failure<UUID2<Book>>) checkedOutBookResult).exception().getMessage());
                    throw ((Result.Failure<UUID2<Book>>) checkedOutBookResult).exception();
                } else
                    ctx.log.d(this, "Checkout Book SUCCESS --> checkedOutBook:" + ((Result.Success<UUID2<Book>>) checkedOutBookResult).value());

                ctx.log.d(this,"User (2):" + user2.id() + " Transfer Checked-Out Book:" + book12id + " to User(1):" + user01.id());

                // Give book from user2 to user01
                // Note: The Library that the book is checked out from transfers the checkout to the new user.
                // Will only allow the transfer to complete if the receiving user has an account in good standing (ie: no fines, etc.)
                final Result<ArrayList<UUID2<Book>>> transferBookToUserResult = user2.giveBookToUser(book12, user01);
                if (transferBookToUserResult instanceof Result.Failure) {
                    ctx.log.e(this, "Transfer book FAILURE --> Book:" + ((Result.Failure<ArrayList<UUID2<Book>>>) transferBookToUserResult).exception().getMessage());
                    throw ((Result.Failure<ArrayList<UUID2<Book>>>) transferBookToUserResult).exception();
                } else
                    ctx.log.d(this, "Transfer Book SUCCESS --> Book:" + ((Result.Success<ArrayList<UUID2<Book>>>) transferBookToUserResult).value());
            }

            Give_Book_From_User_To_User:
            if (true) {
                System.out.println();
                ctx.log.d(this, "Give_Book_From_User_To_User: ");
                ctx.log.d(this, "----------------------------------");

                final Result<UserInfo> user01InfoResult = testUtil.createFakeUserInfoInContextUserInfoRepo(1);
                assert user01InfoResult != null;
                final User user01 = new User(((Result.Success<UserInfo>) user01InfoResult).value(), account1, ctx);

                final Result<UserInfo> user2InfoResult = testUtil.createFakeUserInfoInContextUserInfoRepo(2);
                assert user2InfoResult != null;
                final User user2 = new User(((Result.Success<UserInfo>) user2InfoResult).value(), account2, ctx);

                Result<ArrayList<Book>> acceptBookResult = user2.acceptBook(book1100);
                if(acceptBookResult instanceof Result.Failure) {
                    ctx.log.e(this, "Accept Book FAILURE --> Book:" + ((Result.Failure<ArrayList<Book>>) acceptBookResult).exception().getMessage());
                    throw ((Result.Failure<ArrayList<Book>>) acceptBookResult).exception();
                } else
                    ctx.log.d(this, "Accept Book SUCCESS --> Book:" + ((Result.Success<ArrayList<Book>>) acceptBookResult).value());

                Result<ArrayList<UUID2<Book>>> giveBookResult = user2.giveBookToUser(book1100, user01);
                if(giveBookResult instanceof Result.Failure) {
                    ctx.log.e(this, "Give Book FAILURE --> Book:" + ((Result.Failure<ArrayList<UUID2<Book>>>) giveBookResult).exception().getMessage());
                    throw ((Result.Failure<ArrayList<UUID2<Book>>>) giveBookResult).exception();
                } else
                    ctx.log.d(this, "Give Book SUCCESS --> Book:" + ((Result.Success<ArrayList<UUID2<Book>>>) giveBookResult).value());
            }

            Transfer_Checked_out_Book_Source_Library_to_Destination_Library:
            if (true) {
                System.out.println();
                ctx.log.d(this, "Transfer_Checked_out_Book_Source_Library_to_Destination_Library: ");
                ctx.log.d(this, "----------------------------------");

                final Result<UserInfo> user2InfoResult = testUtil.createFakeUserInfoInContextUserInfoRepo(2);
                assert user2InfoResult != null;
                final User user2 = new User(((Result.Success<UserInfo>) user2InfoResult).value(), account2, ctx);

                // Book12 represents a found book that is not in the library
                final Result<BookInfo> book13InfoResult = testUtil.addFakeBookInfoInContextBookInfoRepo(13);
                assert book13InfoResult != null;
                final UUID2<Book> book13id = ((Result.Success<BookInfo>) book13InfoResult).value().id();
                final Book book13 = new Book(book13id, null, ctx); // note: sourceLibrary is null, so this book comes from an ORPHAN Library

                ctx.log.d(this, "OLD Source Library: name=" + book13.sourceLibrary().info().name);

                // Simulate a User "finding" a Book and checking it out from its ORPHAN Private Library (ie: itself)
                final Result<UUID2<Book>> checkoutResult = user2.checkOutBookFromLibrary(book13, book13.sourceLibrary());
                if (checkoutResult instanceof Result.Failure) {
                    ctx.log.e(this, "Checkout Book FAILURE --> Book:" + ((Result.Failure<UUID2<Book>>) checkoutResult).exception().getMessage());
                    throw ((Result.Failure<UUID2<Book>>) checkoutResult).exception();
                } else
                    ctx.log.d(this, "Checkout Book SUCCESS --> Book:" + ((Result.Success<UUID2<Book>>) checkoutResult).value());

                // Represents a User assigning the "found" Book to a Library, while the Book is still checked out to the User.
                Result<Book> transferResult1 = book13.transferToLibrary(library1);
                if (transferResult1 instanceof Result.Failure) {
                    ctx.log.e(this, "Transfer Book FAILURE --> Book:" + ((Result.Failure<Book>) transferResult1).exception().getMessage());
                    throw ((Result.Failure<Book>) transferResult1).exception();
                } else {
                    ctx.log.d(this, "Transfer Book SUCCESS --> Book:" + ((Result.Success<Book>) transferResult1).value());

                    Book transferredBook13 = ((Result.Success<Book>) transferResult1).value();
                    ctx.log.d(this, "NEW Source Library: name=" + transferredBook13.sourceLibrary().info().name);

                    if(transferredBook13.sourceLibrary().info().name.equals(library1.info().name))
                        ctx.log.d(this, "SUCCESS: Book was transferred to the new Library");
                    else {
                        ctx.log.e(this, "FAILURE: Book was NOT transferred to the new Library");
                        throw new Exception("FAILURE: Book was NOT transferred to the new Library");
                    }
                }
            }

            Test_UUID2_HashMap:
            if (true) {
                System.out.println();
                ctx.log.d(this, "Test_UUID2_HashMap: ");
                ctx.log.d(this, "----------------------------------");

                UUID2.HashMap<UUID2<Book>, UUID2<User>> uuid2ToEntityMap = new UUID2.HashMap<>();

                UUID2<Book> book1 = new UUID2<>(UUID2.createFakeUUID2(1200, Book.class));
                UUID2<Book> book2 = new UUID2<>(UUID2.createFakeUUID2(1300, Book.class));

                UUID2<User> user01 = new UUID2<>(UUID2.createFakeUUID2(1, User.class));
                UUID2<User> user02 = new UUID2<>(UUID2.createFakeUUID2(2, User.class));

                uuid2ToEntityMap.put(book1, user01);
                uuid2ToEntityMap.put(book2, user02);

                UUID2<User> user = uuid2ToEntityMap.get(book1);
                ctx.log.d(this, "user=" + user);

                UUID2<Book> book1a = ((Result.Success<Book>) Book.fetchBook(UUID2.createFakeUUID2(1200, Book.class), ctx)).value().id();
                UUID2<User> user2 = uuid2ToEntityMap.get(book1a);
                ctx.log.d(this, "user=" + user);
                assert user2 != null;
                assert user2.equals(user);

                uuid2ToEntityMap.remove(book1);
                user = uuid2ToEntityMap.get(book1);
                ctx.log.d(this, "user=" + user);
                assert user == null;

                // put it back
                uuid2ToEntityMap.put(book1, user01);

                // check keySet
                Set<UUID2<Book>> keySet = uuid2ToEntityMap.keySet();
                assert keySet.size() == 2;

                // check values
                Collection<UUID2<User>> values = uuid2ToEntityMap.values();
                assert values.size() == 2;

                // check entrySet
                Set<Map.Entry<UUID2<Book>, UUID2<User>>> entrySet = uuid2ToEntityMap.entrySet();
                assert entrySet.size() == 2;

                // check containsKey
                if(!uuid2ToEntityMap.containsKey(book1))
                    throw new RuntimeException("containsKey(book1) failed");
                if(!uuid2ToEntityMap.containsKey(book2))
                    throw new RuntimeException("containsKey(book2) failed");
                if(uuid2ToEntityMap.containsKey(UUID2.createFakeUUID2(1400, Book.class)))
                    throw new RuntimeException("containsKey(Book 1400) should have failed");
            }
        }

        ctx.log.d(this,
    "\n\n" +
            "*****************************\n" +
            "Trials Completed Successfully\n" +
            "*****************************\n"
        );
    }
}
