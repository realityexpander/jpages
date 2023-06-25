package org.elegantobjects.jpages.App2;

import org.elegantobjects.jpages.App2.domain.Book;
import org.elegantobjects.jpages.App2.domain.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.elegantobjects.jpages.App2.Model.*;

class LibraryApp {

    public static void main(final String... args) {

        // Setup App Context Object singletons
        Context productionContext = Context.setupProductionInstance();
        // Context productionContext = Context.setupInstance(TEST, testContext); // for testing

        new LibraryApp(productionContext);
    }

    LibraryApp(Context ctx) {
        //context = Context.setupINSTANCE(context);  // For implementing a static Context. LEAVE for reference

        ctx.log.d(this,"Populating Book DB and API");
        PopulateFakeBookInfoInContextBookRepoDBandAPI(ctx);

        Populate_And_Poke_Book:
        if(false) {
            ctx.log.d(this, "----------------------------------");
            ctx.log.d(this, "Populate_And_Poke_Book");

            // Create a book object (it only has an id)
            Book book = new Book(UUID2.createFakeUUID2(1, Book.class.getName()), ctx);
            ctx.log.d(this,book.fetchInfoResult().toString());

            // Update info for a book
            final Result<Domain.BookInfo> bookInfoResult =
                    book.updateInfo(
                            new Domain.BookInfo(
                                    book.id,
                                    "The Updated Title",
                                    "The Updated Author",
                                    "The Updated Description"
                            ));
            ctx.log.d(this,book.fetchInfoResult().toString());

            // Get the bookInfo (null if not loaded)
            Domain.BookInfo bookInfo3 = book.fetchInfo();
            if (bookInfo3 == null) {
                ctx.log.d(this,"Book Missing --> " +
                        "book id: " + bookInfo3.id() + " >> " +
                        " is null"
                );
            } else {
                ctx.log.d(this,"Book Info --> " +
                        bookInfo3.toString()
                );
            }

            // Try to get a book id that doesn't exist
            Book book2 = new Book(UUID2.createFakeUUID2(99, Book.class.getName()), ctx);
            if (book2.fetchInfoResult() instanceof Result.Failure) {
                ctx.log.d(this,"Get Book FAILURE --> " +
                        "book id: " + book2.id + " >> " +
                        ((Result.Failure<Domain.BookInfo>) book2.fetchInfoResult())
                );
            } else {
                ctx.log.d(this,"Book ERxists --> " +
                        ((Result.Success<Domain.BookInfo>) book2.fetchInfoResult()).value()
                );
            }

            DumpBookDBandAPI(ctx);
        }

        Populate_the_library_and_user_DBs:
        {
            ////////////////////////////////////////
            // Setup DB & API simulated resources //
            ////////////////////////////////////////

            // Create & populate a Library in the Library Repo
            final Result<Domain.LibraryInfo> libraryInfo = createFakeLibraryInfoInContextLibraryRepo(1, ctx);
            if (libraryInfo instanceof Result.Failure) {
                ctx.log.d(this,"Create Library FAILURE --> " +
                        ((Result.Failure<Domain.LibraryInfo>) libraryInfo)
                );

                break Populate_the_library_and_user_DBs;
            }
            UUID2<Library> libraryInfoId = ((Result.Success<Domain.LibraryInfo>) libraryInfo).value().id;
            ctx.log.d(this,"Library Created --> id: " +
                    ((Result.Success<Domain.LibraryInfo>) libraryInfo).value().id +
                    ", name: "+
                    ((Result.Success<Domain.LibraryInfo>) libraryInfo).value().name
            );

            // Populate the library
            ctx.libraryRepo().populateWithFakeBooks(libraryInfoId, 10);

            // Create & populate a User in the User Repo
            final Domain.UserInfo userInfo = createFakeUserInfoInContextUserRepo(1, ctx);

            //////////////////////////////////
            // Actual App functionality     //
            //////////////////////////////////

            // Create the App objects
            final User user1 = new User(userInfo.id(), ctx);
            final Library library1 = new Library(libraryInfoId, ctx);
            final Book book1 = new Book(UUID2.createFakeUUID2(1, Book.class.getName()), ctx);
            final Book book2 = new Book(UUID2.createFakeUUID2(2, Book.class.getName()), ctx);

            // print the user
            ctx.log.d(this,"User --> " +
                    user1.id + ", " +
                    user1.fetchInfo().toPrettyJson()
            );

            Checkout_2_books_to_a_user:
            if (false) {
                ctx.log.d(this, "----------------------------------");
                ctx.log.d(this,"Checking out 2 books to user " + user1.id);

                final Result<Book> bookResult = library1.checkOutBookToUser(book1, user1);
                if (bookResult instanceof Result.Failure) {
                    ctx.log.d(this,"Checked out book FAILURE--> " +
                            ((Result.Failure<Book>) bookResult).exception().getMessage()
                    );
                } else {
                    ctx.log.d(this,"Checked out book SUCCESS --> " +
                            ((Result.Success<Book>) bookResult).value().id
                    );
                }

                final Result<Book> bookResult2 = library1.checkOutBookToUser(book2, user1);
                if (bookResult2 instanceof Result.Failure) {
                    ctx.log.d(this,"Checked out book FAILURE--> " +
                            ((Result.Failure<Book>) bookResult2).exception().getMessage()
                    );
                } else {
                    ctx.log.d(this,"Checked out book SUCCESS --> " +
                            ((Result.Success<Book>) bookResult2).value().id
                    );
                }

                // library1.DumpDB(ctx);  // LEAVE for debugging
            }

            Get_Available_Books_And_Counts_In_Library:
            if (false) {
                ctx.log.d(this, "----------------------------------");
                ctx.log.d(this,"\nGetting available books and counts in library:");

                final Result<HashMap<Book, Integer>> availableBookToNumAvailableResult =
                        library1.calculateAvailableBookIdToNumberAvailableList();
                if (availableBookToNumAvailableResult instanceof Result.Failure) {
                    ctx.log.d(this,"AvailableBookIdCounts FAILURE! --> " +
                            ((Result.Failure<HashMap<Book, Integer>>) availableBookToNumAvailableResult)
                                    .exception().getMessage()
                    );

                    break Get_Available_Books_And_Counts_In_Library;
                }

                // create objects and populate info for available books
                assert availableBookToNumAvailableResult instanceof Result.Success;
                final HashMap<Book, Integer> availableBooks =
                        ((Result.Success<HashMap<Book, Integer>>) availableBookToNumAvailableResult).value();

                // Print out available books
                ctx.log.d(this,"\nAvailable Books in Library:");
                for (Map.Entry<Book, Integer> availableBook : availableBooks.entrySet()) {
                    final Book book3 = new Book(availableBook.getKey().id, ctx);

                    final Result<Domain.BookInfo> bookInfoResult = book3.fetchInfoResult();
                    if (bookInfoResult instanceof Result.Failure) {
                        ctx.log.d(this,
                                "Book Error: " +
                                        ((Result.Failure<Domain.BookInfo>) bookInfoResult)
                                                .exception().getMessage()
                        );
                    } else {
                        ctx.log.d(this,
                                ((Result.Success<Domain.BookInfo>) bookInfoResult).value() +
                                        " >> num available: " + availableBook.getValue()
                        );
                    }
                }
                ctx.log.d(this,"Total Available Books (unique UUIDs): " + availableBooks.size());
                ctx.log.d(this,"\n");
            }

            Get_books_checked_out_by_user:
            if (false) {
                ctx.log.d(this, "----------------------------------");
                ctx.log.d(this,"\nGetting books checked out by user " + user1.id);

                final Result<ArrayList<Book>> checkedOutBooksResult = library1.findBooksCheckedOutByUser(user1);
                if (checkedOutBooksResult instanceof Result.Failure) {
                    ctx.log.d(this,"OH NO! --> " +
                            ((Result.Failure<ArrayList<Book>>) checkedOutBooksResult)
                                    .exception().getMessage()
                    );
                }

                assert checkedOutBooksResult instanceof Result.Success;
                ArrayList<Book> checkedOutBooks = ((Result.Success<ArrayList<Book>>) checkedOutBooksResult).value();

                // Print checked out books
                ctx.log.d(this,"\nChecked Out Books for User [" + user1.fetchInfo().name() + ", " + user1.id + "]:");
                for (Book book : checkedOutBooks) {
                    final Result<Domain.BookInfo> bookInfoResult = book.fetchInfoResult();
                    if (bookInfoResult instanceof Result.Failure) {
                        ctx.log.d(this,
                                "Book Error: " +
                                        ((Result.Failure<Domain.BookInfo>) bookInfoResult)
                                                .exception().getMessage()
                        );
                    } else {
                        ctx.log.d(this,
                                ((Result.Success<Domain.BookInfo>) bookInfoResult).value().toString()
                        );
                    }
                }
                System.out.print("\n");
            }

            Check_In_the_Book_from_the_User_to_the_Library:
            if (false) {
                ctx.log.d(this, "----------------------------------");
                ctx.log.d(this,"\nCheck in book " + book1.id + " from user " + user1.id);

                final Result<Book> checkInBookResult = library1.checkInBookFromUser(book1, user1);
                if (checkInBookResult instanceof Result.Failure) {
                    ctx.log.d(this,"Check In book FAILURE --> book id:" +
                            ((Result.Failure<Book>) checkInBookResult).exception().getMessage()
                    );
                } else {
                    ctx.log.d(this,"Returned Book SUCCESS --> book id:" +
                            ((Result.Success<Book>) checkInBookResult).value()
                    );
                }

                library1.DumpDB(ctx);
            }

            // Load Library from Json
            if (true) {
                ctx.log.d(this, "----------------------------------");
                ctx.log.d(this,"Load Library from Json: ");

                // Library library2 = new Library(ctx); // uses random UUID, will cause expected error due to unknown UUID
                Library library2 = new Library(UUID2.createFakeUUID2(99), ctx);
                ctx.log.d(this, library2.toJson());

                String json =
                        "{\n" +
                                "  \"name\": \"Ronald Reagan Library\",\n" +
                                "  \"userIdToCheckedOutBookIdMap\": {\n" +
                                "    \"00000000-0000-0000-0000-000000000001\": [\n" +
                                "      {\n" +
                                "        \"uuid\": \"00000000-0000-0000-0000-000000000010\",\n" +
                                "        \"uuid2TypeStr\": \"org.elegantobjects.jpages.Model$Domain$BookInfo\"\n" +
                                "      }\n" +
                                "    ]\n" +
                                "  },\n" +
                                "  \"bookIdToNumBooksAvailableMap\": {\n" +
                                "    \"00000000-0000-0000-0000-000000000010\": 50,\n" +
                                "    \"00000000-0000-0000-0000-000000000011\": 50,\n" +
                                "    \"00000000-0000-0000-0000-000000000012\": 50,\n" +
                                "    \"00000000-0000-0000-0000-000000000013\": 50,\n" +
                                "    \"00000000-0000-0000-0000-000000000014\": 50,\n" +
                                "    \"00000000-0000-0000-0000-000000000015\": 50,\n" +
                                "    \"00000000-0000-0000-0000-000000000016\": 50,\n" +
                                "    \"00000000-0000-0000-0000-000000000017\": 50,\n" +
                                "    \"00000000-0000-0000-0000-000000000018\": 50,\n" +
                                "    \"00000000-0000-0000-0000-000000000019\": 50\n" +
                                "  },\n" +
                                "  \"id\": {\n" +
                                "    \"uuid\": \"00000000-0000-0000-0000-000000000099\"\n" +
                                "  }\n" +
                                "}";
                if(true) {
                    Result<Domain.LibraryInfo> library2Result = library2.updateDomainInfoFromJson(json);
                    if (library2Result instanceof Result.Failure) {
                        ctx.log.d(this, ((Result.Failure<Domain.LibraryInfo>) library2Result).exception().getMessage());
                    } else {
                        ctx.log.d(this, "Results of Library2 json load:");
                        ctx.log.d(this, library2.toJson());
                    }
                }

                try {
                    Domain.LibraryInfo libraryInfo3 =
                            Library.createDomainInfoFromJson(
                                    json,
                                    Domain.LibraryInfo.class,
                                    ctx
                            );

                    Library library3 = new Library(libraryInfo3, ctx);
                    if(libraryInfo3 == null) {
                        ctx.log.d(this, "Library3 is null");
                    } else {
                        ctx.log.d(this,"Results of Library3 json load:");
                        ctx.log.d(this,library3.toJson());
                    }
                } catch (Exception e) {
                    ctx.log.d(this, "Exception: " + e.getMessage());
                }
            }

            // Load Book from DTO Json
            if(false) {
                ctx.log.d(this, "----------------------------------");
                ctx.log.d(this,"Load Book from DTO Json: ");

                String json =
                        "{\n" +
                                "  \"id\": {\n" +
                                "    \"uuid\": \"00000000-0000-0000-0000-000000000010\",\n" +
                                "    \"uuid2TypeStr\": \"org.elegantobjects.jpages.Model$DTO$BookInfo\"\n" +
                                "  },\n" +
                                "  \"title\": \"The Great Gatsby\",\n" +
                                "  \"author\": \"F. Scott Fitzgerald\",\n" +
                                "  \"description\": \"The Great Gatsby is a 1925 novel written by American author F. Scott Fitzgerald that follows a cast of characters living in the fictional towns of West Egg and East Egg on prosperous Long Island in the summer of 1922. The story primarily concerns the young and mysterious millionaire Jay Gatsby and his quixotic passion and obsession with the beautiful former debutante Daisy Buchanan. Considered to be Fitzgerald's magnum opus, The Great Gatsby explores themes of decadence, idealism, resistance to change, social upheaval, and excess, creating a portrait of the Jazz Age or the Roaring Twenties that has been described as a cautionary tale regarding the American Dream.\",\n" +
                                "  \"extraFieldToShowThisIsADTO\": \"Data from JSON load\"\n" +
                                "}";

                try {
                    DTO.BookInfo bookInfo3 = new DTO.BookInfo(json, ctx);
                    Book book3 = new Book(bookInfo3.toDeepCopyDomainInfo(), ctx);

                    ctx.log.d(this,"Results of Load Book from DTO Json: " + book3.toJson());
                } catch (Exception e) {
                    ctx.log.d(this, "Exception: " + e.getMessage());
                }
            }

            Check_out_Book_via_User:
            if (false) {
                final User user2 = new User(createFakeUserInfoInContextUserRepo(2, ctx).id(), ctx);
                final Result<Domain.BookInfo> book12Result = addFakeBookInfoInContextBookRepo(12, ctx);

                if (book12Result instanceof Result.Failure) {
                    ctx.log.d(this,"Book Error: " +
                            ((Result.Failure<Domain.BookInfo>) book12Result).exception().getMessage()
                    );
                } else {

                    final UUID2<Book> book12id = ((Result.Success<Domain.BookInfo>) book12Result).value().id();
                    final Book book12 = new Book(book12id, ctx);

                    ctx.log.d(this,"\nCheck out book " + book12id + " to user " + user1.id);

                    final Result<Book> book12UpsertResult = library1.addTestBookToLibrary(book12, 1);
                    if (book12UpsertResult instanceof Result.Failure) {
                        ctx.log.d(this,"Upsert Book Error: " +
                                ((Result.Failure<Book>) book12UpsertResult).exception().getMessage()
                        );
                    }

                    final Result<UUID2<Book>> checkedOutBookResult = user2.checkoutBookFromLibrary(book12, library1);
                    if (checkedOutBookResult instanceof Result.Failure) {
                        ctx.log.d(this,"Checkout book FAILURE --> " +
                                ((Result.Failure<UUID2<Book>>) checkedOutBookResult).exception().getMessage()
                        );
                    } else {
                        ctx.log.d(this,"Checkout Book SUCCESS --> checkedOutBook:" +
                                ((Result.Success<UUID2<Book>>) checkedOutBookResult).value()
                        );
                    }
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////
    /////////////////////////// Helper Methods ///////////////////////////
    //////////////////////////////////////////////////////////////////////

    private void PopulateFakeBookInfoInContextBookRepoDBandAPI(Context context) {
        context.bookRepo().populateDatabaseWithFakeBookInfo();
        context.bookRepo().populateApiWithFakeBookInfo();
    }

    private void DumpBookDBandAPI(Context context) {
        System.out.print("\n");
        context.log.d(this,"DB Dump");
        context.bookRepo().printDB();

        System.out.print("\n");
        context.log.d(this,"API Dump");
        context.bookRepo().printAPI();

        System.out.print("\n");
    }

    private Result<Domain.LibraryInfo> createFakeLibraryInfoInContextLibraryRepo(
            final Integer id,
            Context context
    ) {
        Integer someNumber = id;
        if (someNumber == null) someNumber = 1;

        return context.libraryRepo()
                .upsertLibraryInfo(
                        new Domain.LibraryInfo(
                                UUID2.createFakeUUID2(someNumber, Domain.LibraryInfo.class.getName()),
                                "Library " + someNumber
                        )
                );
    }

    private Domain.UserInfo createFakeUserInfoInContextUserRepo(
            final Integer id,
            Context context
    ) {
        Integer someNumber = id;
        if (someNumber == null) someNumber = 1;

        return context.userRepo()
                .upsertUserInfo(
                        new Domain.UserInfo(
                                UUID2.createFakeUUID2(someNumber, Domain.UserInfo.class.getName()),
                                "User " + someNumber,
                                "user" + someNumber + "@gmail.com"
                        ));
    }

    private Result<Domain.BookInfo> addFakeBookInfoInContextBookRepo(
            final Integer id,
            Context context
    ) {
        final Domain.BookInfo bookInfo = createFakeBookInfo(null, id);

        return context.bookRepo()
                .upsertBookInfo(bookInfo);
    }

    private Domain.BookInfo createFakeBookInfo(String uuidStr, final Integer id) {
        Integer fakeId = id;
        if (fakeId == null) fakeId = 1;

        UUID2<Book> uuid;
        if (uuidStr == null)
            uuid = UUID2.createFakeUUID2(fakeId, Book.class.getName());
        else
            uuid = UUID2.fromString(uuidStr);

        return new Domain.BookInfo(
                uuid,
                "Book " + fakeId,
                "Author " + fakeId,
                "Description " + fakeId
        );
    }
}
