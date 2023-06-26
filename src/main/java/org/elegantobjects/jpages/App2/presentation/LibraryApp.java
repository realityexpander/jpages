package org.elegantobjects.jpages.App2.presentation;


import org.elegantobjects.jpages.App2.data.network.DTOBookInfo;
import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.*;
import org.elegantobjects.jpages.App2.domain.domainInfo.DomainBookInfo;
import org.elegantobjects.jpages.App2.domain.domainInfo.DomainLibraryInfo;
import org.elegantobjects.jpages.App2.domain.domainInfo.DomainUserInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Notes:
// - Intentionally using multiple returns. Makes error handling easier.


class LibraryApp {

    public static void main(final String... args) {

        // Setup App Context Object singletons
        Context productionContext = Context.setupProductionInstance();
        // Context productionContext = Context.setupInstance(TEST, testContext); // for testing

        new LibraryApp(productionContext);
    }

    LibraryApp(@NotNull Context ctx) {
        //context = Context.setupINSTANCE(context);  // For implementing a static Context. LEAVE for reference

        ctx.log.d(this,"Populating Book DB and API");
        PopulateFakeBookInfoInContextBookRepoDBandAPI(ctx);

        Populate_And_Poke_Book:
        if (true) {
            ctx.log.d(this, "----------------------------------");
            ctx.log.d(this, "Populate_And_Poke_Book");

            // Create a book object (it only has an id)
            Book book = new Book(UUID2.createFakeUUID2(1, Book.class.getName()), ctx);
            ctx.log.d(this,book.fetchInfoResult().toString());

            // Update info for a book
            final Result<DomainBookInfo> bookInfoResult =
                    book.updateInfo(
                            new DomainBookInfo(
                                    book.id,
                                    "The Updated Title",
                                    "The Updated Author",
                                    "The Updated Description"
                            ));
            ctx.log.d(this,book.fetchInfoResult().toString());

            // Get the bookInfo (null if not loaded)
            DomainBookInfo bookInfo3 = book.fetchInfo();
            if (bookInfo3 == null) {
                ctx.log.d(this,"Book Missing --> " +
                        "book id: " + book.id() + " >> " +
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
                        ((Result.Failure<DomainBookInfo>) book2.fetchInfoResult())
                );
            } else {
                ctx.log.d(this,"Book ERxists --> " +
                        ((Result.Success<DomainBookInfo>) book2.fetchInfoResult()).value()
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
            final Result<DomainLibraryInfo> libraryInfo = createFakeLibraryInfoInContextLibraryRepo(1, ctx);
            if (libraryInfo instanceof Result.Failure) {
                ctx.log.d(this,"Create Library FAILURE --> " +
                        ((Result.Failure<DomainLibraryInfo>) libraryInfo)
                );

                break Populate_the_library_and_user_DBs;
            }
            UUID2<Library> library1InfoId = ((Result.Success<DomainLibraryInfo>) libraryInfo).value().id();
            ctx.log.d(this,"Library Created --> id: " +
                ((Result.Success<DomainLibraryInfo>) libraryInfo).value().id() +
                ", name: "+
                ((Result.Success<DomainLibraryInfo>) libraryInfo).value().name()
            );

            // Populate the library
            ctx.libraryRepo().populateWithFakeBooks(library1InfoId, 10);

            // Create & populate User 1 in the User Repo
            final DomainUserInfo user1Info = createFakeUserInfoInContextUserRepo(1, ctx);

            //////////////////////////////////
            // Actual App functionality     //
            //////////////////////////////////

            // Create the App objects
            final User user1 = new User(user1Info, ctx);
            final Library library1 = new Library(library1InfoId, ctx);
            final Book book1 = new Book(UUID2.createFakeUUID2(1, Book.class.getName()), ctx);
            final Book book2 = new Book(UUID2.createFakeUUID2(2, Book.class.getName()), ctx);

            // print User 1
            ctx.log.d(this,"User --> " + user1.id + ", " + user1.fetchInfo().toPrettyJson());

            Checkout_2_Books_to_User:
            if (true) {
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

            Get_available_Books_and_Inventory_Counts_in_Library:
            if (true) {
                ctx.log.d(this, "----------------------------------");
                ctx.log.d(this,"\nGetting available books and counts in library:");

                final Result<HashMap<Book, Integer>> availableBookToNumAvailableResult =
                        library1.calculateAvailableBookIdToNumberAvailableList();
                if (availableBookToNumAvailableResult instanceof Result.Failure) {
                    ctx.log.d(this,"AvailableBookIdCounts FAILURE! --> " +
                            ((Result.Failure<HashMap<Book, Integer>>) availableBookToNumAvailableResult)
                                    .exception().getMessage()
                    );

                    break Get_available_Books_and_Inventory_Counts_in_Library;
                }

                // create objects and populate info for available books
                assert availableBookToNumAvailableResult instanceof Result.Success;
                final HashMap<Book, Integer> availableBooks =
                        ((Result.Success<HashMap<Book, Integer>>) availableBookToNumAvailableResult).value();

                // Print out available books
                ctx.log.d(this,"\nAvailable Books in Library:");
                for (Map.Entry<Book, Integer> availableBook : availableBooks.entrySet()) {
                    final UUID2<Book> bookId = availableBook.getKey().id;
                    final Book book3 = new Book(bookId, ctx);

                    final Result<DomainBookInfo> bookInfoResult = book3.fetchInfoResult();
                    if (bookInfoResult instanceof Result.Failure) {
                        ctx.log.d(this,
                                "Book Error: " +
                                        ((Result.Failure<DomainBookInfo>) bookInfoResult)
                                                .exception().getMessage()
                        );
                    } else {
                        ctx.log.d(this,
                                ((Result.Success<DomainBookInfo>) bookInfoResult).value() +
                                        " >> num available: " + availableBook.getValue()
                        );
                    }
                }
                ctx.log.d(this,"Total Available Books (unique UUIDs): " + availableBooks.size());
                ctx.log.d(this,"\n");
            }

            Get_Books_checked_out_by_User:
            if (true) {
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
                    final Result<DomainBookInfo> bookInfoResult = book.fetchInfoResult();
                    if (bookInfoResult instanceof Result.Failure) {
                        ctx.log.d(this,
                                "Book Error: " +
                                        ((Result.Failure<DomainBookInfo>) bookInfoResult)
                                                .exception().getMessage()
                        );
                    } else {
                        ctx.log.d(this,
                                ((Result.Success<DomainBookInfo>) bookInfoResult).value().toString()
                        );
                    }
                }
                System.out.print("\n");
            }

            Check_In_Book_from_User_to_Library:
            if (true) {
                ctx.log.d(this, "----------------------------------");
                ctx.log.d(this,"\nCheck in book:" + book1.id + ", from user: " + user1.id + ", to library:" + library1.id);

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
                Library library2 = new Library(UUID2.createFakeUUID2(99, Library.class.getName()), ctx);
                ctx.log.d(this, library2.toJson());

                String json =
                        "{\n" +
                                "  \"name\": \"Ronald Reagan Library\",\n" +
                                "  \"userIdToCheckedOutBookIdMap\": {\n" +
                                "    \"00000000-0000-0000-0000-000000000001\": [\n" +
                                "      {\n" +
                                "        \"uuid\": \"00000000-0000-0000-0000-000000000010\",\n" +
                                "        \"uuid2TypeStr\": \"org.elegantobjects.jpages.App2.common.Model.Domain.BookInfo\"\n" +
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
                                "    \"uuid\": \"00000000-0000-0000-0000-000000000099\",\n" +
                                "    \"uuid2TypeStr\": \"org.elegantobjects.jpages.App2.common.Model.Domain.LibraryInfo\"\n" +
                                "  }\n" +
                                "}";

                // Check JSON loaded properly
                if(true) {
                    Result<DomainLibraryInfo> library2Result = library2.updateDomainInfoFromJson(json);
                    if (library2Result instanceof Result.Failure) {

                        // Since the library2 was not saved in the central database, we will get a "library not found error" which is expected
                        ctx.log.d(this, ((Result.Failure<DomainLibraryInfo>) library2Result).exception().getMessage());

                        // The JSON was still loaded properly
                        ctx.log.d(this, "Results of Library2 json load:" + library2.toJson());

                    } else {
                        // Intentionally Wont see this branch bc the library2 was never saved to the central database/api.
                        ctx.log.d(this, "Results of Library2 json load:");
                        ctx.log.d(this, library2.toJson());
                    }
                }

                // Create a Library Domain Object from the Info
                if(true) {
                    try {
                        DomainLibraryInfo libraryInfo3 =
                                Library.createDomainInfoFromJson(
                                        json,
                                        DomainLibraryInfo.class,
                                        ctx
                                );

                        assert libraryInfo3 != null;
                        Library library3 = new Library(libraryInfo3, ctx);
                        ctx.log.d(this, "Results of Library3 json load:" + library3.toJson());
                    } catch (Exception e) {
                        ctx.log.d(this, "Exception: " + e.getMessage());
                    }
                }
            }

            // Load Book from DTO Json
            if (true) {
                ctx.log.d(this, "----------------------------------");
                ctx.log.d(this,"Load BookInfo from DTO Json: ");

                String json =
                        "{\n" +
                                "  \"id\": {\n" +
                                "    \"uuid\": \"00000000-0000-0000-0000-000000000010\",\n" +
                                "    \"uuid2TypeStr\": \"org.elegantobjects.jpages.App2.common.Model.DTO.BookInfo\"\n" +
                                "  },\n" +
                                "  \"title\": \"The Great Gatsby\",\n" +
                                "  \"author\": \"F. Scott Fitzgerald\",\n" +
                                "  \"description\": \"The Great Gatsby is a 1925 novel written by American author F. Scott Fitzgerald that follows a cast of characters living in the fictional towns of West Egg and East Egg on prosperous Long Island in the summer of 1922. The story primarily concerns the young and mysterious millionaire Jay Gatsby and his quixotic passion and obsession with the beautiful former debutante Daisy Buchanan. Considered to be Fitzgerald's magnum opus, The Great Gatsby explores themes of decadence, idealism, resistance to change, social upheaval, and excess, creating a portrait of the Jazz Age or the Roaring Twenties that has been described as a cautionary tale regarding the American Dream.\",\n" +
                                "  \"extraFieldToShowThisIsADTO\": \"Extra Unneeded Data from JSON payload load\"\n" +
                                "}";

                try {
                    DTOBookInfo bookInfo3 = new DTOBookInfo(json, ctx);
//                    Book book3 = new Book(bookInfo3.toDeepCopyDomainInfo(), ctx);
                    Book book3 = new Book(new DomainBookInfo(bookInfo3), ctx);

                    ctx.log.d(this,"Results of load BookInfo from DTO Json: " + book3.toJson());
                } catch (Exception e) {
                    ctx.log.d(this, "Exception: " + e.getMessage());
                }
            }

            Check_out_Book_via_User:
            if (true) {
                final User user2 = new User(createFakeUserInfoInContextUserRepo(2, ctx), ctx);
                final Result<DomainBookInfo> book12Result = addFakeBookInfoInContextBookRepo(12, ctx);

                if (book12Result instanceof Result.Failure) {
                    ctx.log.d(this,"Book Error: " +
                            ((Result.Failure<DomainBookInfo>) book12Result).exception().getMessage()
                    );
                } else {

                    final UUID2<Book> book12id = ((Result.Success<DomainBookInfo>) book12Result).value().id();
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

            Give_Book_To_User:
            if (true) {
                final User user01 = new User(createFakeUserInfoInContextUserRepo(1, ctx), ctx);
                final User user2 = new User(createFakeUserInfoInContextUserRepo(2, ctx), ctx);
                final Result<DomainBookInfo> book12Result = addFakeBookInfoInContextBookRepo(12, ctx);

                if (book12Result instanceof Result.Failure) {
                    ctx.log.d(this,"Book Error: " +
                        ((Result.Failure<DomainBookInfo>) book12Result).exception().getMessage()
                    );
                } else {

                    final UUID2<Book> book12id = ((Result.Success<DomainBookInfo>) book12Result).value().id();
                    final Book book12 = new Book(book12id, ctx);

                    // If book was checked out, it is still checked out by the first person. todo if its checked out of a library, have the library perform a "checkout" transfer
                    user2.acceptBook(book12); // no library involved.

                    ctx.log.d(this,"User (2):" + user2.id + " Give Book:" + book12id + " to User(1):" + user01.id);

                    final Result<ArrayList<UUID2<Book>>> giveBookToUserResult = user2.giveBookToUser(book12, user01);
                    if (giveBookToUserResult instanceof Result.Failure) {
                        ctx.log.d(this,"Give book FAILURE --> Book:" +
                            ((Result.Failure<ArrayList<UUID2<Book>>>) giveBookToUserResult).exception().getMessage()
                        );
                    } else {
                        ctx.log.d(this,"Give Book SUCCESS --> Book:" +
                            ((Result.Success<ArrayList<UUID2<Book>>>) giveBookToUserResult).value()
                        );
                    }
                }
            }

        }

        ctx.log.d(this,
    "\n\n" +
            "*****************************\n" +
            "Tests Completed Successfully\n" +
            "*****************************\n"
        );
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

    private Result<DomainLibraryInfo> createFakeLibraryInfoInContextLibraryRepo(
            final Integer id,
            Context context
    ) {
        Integer someNumber = id;
        if (someNumber == null) someNumber = 1;

        return context.libraryRepo()
                .upsertLibraryInfo(
                        new DomainLibraryInfo(
                                UUID2.createFakeUUID2(someNumber, DomainLibraryInfo.class.getName()),
                                "Library " + someNumber
                        )
                );
    }

    private DomainUserInfo createFakeUserInfoInContextUserRepo(
            final Integer id,
            Context context
    ) {
        Integer someNumber = id;
        if (someNumber == null) someNumber = 1;

        return context.userRepo()
                .upsertUserInfo(
                        new DomainUserInfo(
                                UUID2.createFakeUUID2(someNumber, DomainUserInfo.class.getName()),
                                "User " + someNumber,
                                "user" + someNumber + "@gmail.com"
                        ));
    }

    private Result<DomainBookInfo> addFakeBookInfoInContextBookRepo(
            final Integer id,
            Context context
    ) {
        final DomainBookInfo bookInfo = createFakeBookInfo(null, id);

        return context.bookRepo()
                .upsertBookInfo(bookInfo);
    }

    private DomainBookInfo createFakeBookInfo(String uuidStr, final Integer id) {
        Integer fakeId = id;
        if (fakeId == null) fakeId = 1;

        UUID2<Book> uuid;
        if (uuidStr == null)
            uuid = UUID2.createFakeUUID2(fakeId, Book.class.getName());
        else
            uuid = UUID2.fromString(uuidStr);

        return new DomainBookInfo(
                uuid,
                "Book " + fakeId,
                "Author " + fakeId,
                "Description " + fakeId
        );
    }
}
