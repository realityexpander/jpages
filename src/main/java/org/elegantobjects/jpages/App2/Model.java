package org.elegantobjects.jpages.App2;

import com.google.gson.GsonBuilder;
import org.elegantobjects.jpages.App2.domain.Book;
import org.elegantobjects.jpages.App2.domain.Library;
import org.elegantobjects.jpages.App2.domain.User;
import org.jetbrains.annotations.NotNull;

import java.util.*;

// "{Model}Info" Data Holders held inside each App Domain Object.
// - Similar to an Entity for a database row or a DTO for a REST API endpoint, these are
//   the objects that are passed around the application.
// - They are the "source of truth" for the Domain objects in the application.
// - {Domain}Info hold the Info state that is on the server/api.
// - {DTO}Info hold the API transfer "dumb" objects and Validation layer for the Domain objects.
// - {Entity}Info hold the Database transfer "dumb" objects. Validation can occur here too, but usually not necessary.
public class Model {
    transient protected UUID2<IUUID2> _id; // Can't make final bc need to set it during JSON deserialization. :(

    Model(UUID2<?> id, String uuidTypeStr) {
        this._id = new UUID2<>(id, uuidTypeStr);
    }

    ///////////////////////////////
    // Converters between
    // - Domain.{Domain}Info
    // - Entity.{Domain}Info
    // - DTO.{Domain}Info
    ///////////////////////////////

    public interface ToDomainInfo<TDomainInfo extends Model.Domain> {
        UUID2<?> getDomainInfoId();  // *MUST* override, method should return id of DomainInfo object (used for deserialization)

        @SuppressWarnings("unchecked")
        default TDomainInfo getDomainInfo()
        {  // Return reference to TDomainInfo, used when importing JSON
            return (TDomainInfo) this; // todo test this cast
        }

        default TDomainInfo toDeepCopyDomainInfo() {    // **MUST** override, method should return a DEEP copy (& no original references)
            throw new RuntimeException("DomainInfo:ToDomainInfo:toDeepCopyDomainInfo(): Must override this method");
        }

        // This interface enforces all DomainInfo objects to include a deepCopyDomainInfo() method
        // - Just add "implements ToDomainInfo.deepCopyDomainInfo<ToDomainInfo<Domain>>" to the class
        //   definition, and the deepCopy() method will be added.
        interface hasToDeepCopyDomainInfo<TToInfo extends Model.ToDomainInfo<? extends Model.Domain>> {

            @SuppressWarnings("unchecked")
            default <TDomainInfo extends Model.Domain>
            TDomainInfo deepCopyDomainInfo() // Requires method override, should return a deep copy (no original references)
            {
                // This is a hack to get around the fact that Java doesn't allow you to call a generic method from a generic class
                return (TDomainInfo) ((TToInfo) this).toDeepCopyDomainInfo();
            }
        }
    }
    interface ToEntity<T extends Model.Entity> {
        T toEntity(); // Should return a deep copy (no original references)
    }
    interface ToDTO<T extends Model.DTO> {
        T toDTO();    // Should return a deep copy (no original references)
    }

    public String toPrettyJson() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this); // todo switch over to context version
    }
    public String toPrettyJson(@NotNull Context context) {
        return context.gson.toJson(this);
    }

    // These are methods are for JSON deserialization purposes
    public UUID2<?> id() { return _id; }
    public void _setIdFromImportedJson(UUID2<IUUID2> _id) {
        this._id = _id;
    }

    // Domain objects contain the "{Model}Info" and the associated business logic to manipulate it
//    static class Domain extends Model implements Info.hasDeepCopy<ToDomainInfo<Domain>> {
    static public class Domain extends Model {

        // next lines are ugly java boilerplate to allow call to super() with a UUID2
        Domain(UUID2<?> id, String className) {
//            super(id.toDomainUUID2(), className); // todo Dont convert to UUID2<IUUID2> other places
            super(id, className);
        }
        Domain(UUID uuid, String className) {
            super(new UUID2<IUUID2>(uuid), className);
        }
        Domain(String id, String className) {
            super(UUID2.fromString(id), className);
        }

        // This is primarily for JSON deserialization purposes
        @Override
        public UUID2<?> id() {
            return super.id();
        }

        static public class BookInfo extends Model.Domain implements
                Model.ToEntity<Model.Entity.BookInfo>,
                Model.ToDTO<Model.DTO.BookInfo>,
                Model.ToDomainInfo<Model.Domain.BookInfo>
        {
            private final UUID2<Book> id; // note this is a UUID2<Book> not a UUID2<BookInfo>, it is the id of the Book.
            private final String title;
            private final String author;
            private final String description;

            BookInfo(@NotNull
                     UUID2<Book> id,
                     String title,
                     String author,
                     String description
            ) {
                super(id, Model.Domain.BookInfo.class.getName());
                this.title = title;
                this.author = author;
                this.description = description;
                this.id = id;
            }
            BookInfo(UUID uuid, String title, String author, String description) {
                this(new UUID2<Book>(uuid), title, author, description);
            }
            BookInfo(String id, String title, String author, String description) {
                this(UUID.fromString(id), title, author, description);
            }
            BookInfo(Model.Domain.BookInfo bookInfo) {
                // todo validation
                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description);
            }
            public BookInfo(UUID id) {
                this(id, "", "", "");
            }

            // Domain Must accept both `DTO.BookInfo` and `Entity.BookInfo` (and convert to Domain.BookInfo)
            // Domain decides what to include from the DTOs/Entities
            // todo - should the DTO/Entites decide what to include?
            BookInfo(Model.DTO.BookInfo bookInfo) {
                // Converts from DTO to Domain
                // todo validation here
                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description); // Domain decides what to include from the DTOs
            }
            BookInfo( Model.Entity.BookInfo bookInfo) {
                // Converts from Entity to Domain
                // todo validation here
                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description);  // Domain decides what to include from the Entities
            }

            @Override
            public UUID2<Book> id() { return this.id; }

            ///////////////////////////////////////////
            // BookInfo Business Logic Methods       //
            // - All Info manipulation logic is      //
            //   done here.                          //
            ///////////////////////////////////////////

            public Model.Domain.BookInfo withTitle(String title) {
                return new Model.Domain.BookInfo(this.id, title, this.author, this.description);
            }

            public Model.Domain.BookInfo withAuthor(String authorName) {
                return new Model.Domain.BookInfo(this.id, this.title, authorName, this.description);
            }

            public Model.Domain.BookInfo withDescription(String description) {
                return new Model.Domain.BookInfo(this.id, this.title, this.author, description);
            }

            @Override
            public String toString() {
                return "Book (" + this.id + ") : " + this.title + " by " + this.author + ", " + this.description;
            }

            @Override
            public Model.DTO.BookInfo toDTO() {
                return new Model.DTO.BookInfo(this);
            }
            @Override
            public Model.Entity.BookInfo toEntity() {
                return new Model.Entity.BookInfo(this);
            }

            /////////////////////////////////
            // ToInfo implementation //
            /////////////////////////////////

            @Override
            public Model.Domain.BookInfo toDeepCopyDomainInfo() {
                // shallow copy OK here bc its flat
                return new Model.Domain.BookInfo(this);
            }

            @Override
            public UUID2<?> getDomainInfoId() {
                return this.id;
            }
        }

        static public class UserInfo extends Model.Domain implements Model.ToDomainInfo<Model.Domain.UserInfo> {
            private final UUID2<User> id;  // note this is a UUID2<User> not a UUID2<UserInfo>, it is the id of the User.
            private final String name;
            private final String email;
            private final ArrayList<UUID2<Book>> acceptedBooks;
            private final Model.Domain.UserInfo.Account account;

            static class Account {

                final Model.Domain.UserInfo.Account.AccountStatus accountStatus;
                final int currentFineAmountPennies;
                final int maxBooks;             // max books allowed to be checked out
                final int maxDays;              // max number of days a book can be checked out
                final int maxRenewals;          // max number of renewals (per book)
                final int maxRenewalDays;       // max number days for each renewal (per book)
                final int maxFineAmountPennies; // max dollar amount of all fines allowed before account is suspended
                final int maxFineDays;          // max number of days to pay fine before account is suspended

                Account(Model.Domain.UserInfo.Account.AccountStatus accountStatus,
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
                    this.accountStatus = Model.Domain.UserInfo.Account.AccountStatus.ACTIVE;
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
                    Model.Domain.UserInfo.Account.AccountStatus accountStatus;
                    int maxBooks;
                    int maxDays;
                    int maxRenewals;
                    int maxRenewalDays;
                    int maxFines;
                    int maxFineDays;
                    int maxFineAmount;

                    Builder() {
                        this.accountStatus = Model.Domain.UserInfo.Account.AccountStatus.ACTIVE;
                    } // default values
                    Builder(Model.Domain.UserInfo.Account account) {
                        this.accountStatus = account.accountStatus;
                        this.maxBooks = account.maxBooks;
                        this.maxDays = account.maxDays;
                        this.maxRenewals = account.maxRenewals;
                        this.maxRenewalDays = account.maxRenewalDays;
                        this.maxFines = account.maxFineAmountPennies;
                        this.maxFineDays = account.maxFineDays;
                        this.maxFineAmount = account.maxFineAmountPennies;
                    }

                    Model.Domain.UserInfo.Account.Builder accountStatus(Model.Domain.UserInfo.Account.AccountStatus accountStatus) {
                        this.accountStatus = accountStatus;
                        return this;
                    }
                    Model.Domain.UserInfo.Account.Builder maxBooks(int maxBooks) {
                        this.maxBooks = maxBooks;
                        return this;
                    }
                    Model.Domain.UserInfo.Account.Builder maxDays(int maxDays) {
                        this.maxDays = maxDays;
                        return this;
                    }
                    Model.Domain.UserInfo.Account.Builder maxRenewals(int maxRenewals) {
                        this.maxRenewals = maxRenewals;
                        return this;
                    }
                    Model.Domain.UserInfo.Account.Builder maxRenewalDays(int maxRenewalDays) {
                        this.maxRenewalDays = maxRenewalDays;
                        return this;
                    }
                    Model.Domain.UserInfo.Account.Builder maxFines(int maxFines) {
                        this.maxFines = maxFines;
                        return this;
                    }
                    Model.Domain.UserInfo.Account.Builder maxFineDays(int maxFineDays) {
                        this.maxFineDays = maxFineDays;
                        return this;
                    }
                    Model.Domain.UserInfo.Account.Builder maxFineAmount(int maxFineAmount) {
                        this.maxFineAmount = maxFineAmount;
                        return this;
                    }

                    Model.Domain.UserInfo.Account build() {
                        return new Model.Domain.UserInfo.Account(
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

            UserInfo(@NotNull
                     UUID2<User> id,
                     String name,
                     String email,
                     ArrayList<UUID2<Book>> acceptedBooks,
                     Model.Domain.UserInfo.Account account
            ) {
                super(id.toDomainUUID2(), Model.Domain.UserInfo.class.getName());
                this.id = id;
                this.name = name;
                this.email = email;
                this.acceptedBooks = acceptedBooks;
                this.account = account;
            }
            UserInfo(Model.Domain.UserInfo userInfo) {
                this(userInfo.id,
                        userInfo.name,
                        userInfo.email,
                        userInfo.acceptedBooks,
                        userInfo.account);
            }
            UserInfo(UUID uuid, String name, String email, ArrayList<UUID2<Book>> acceptedBooks, Model.Domain.UserInfo.Account account) {
                this(new UUID2<User>(uuid), name, email, acceptedBooks, account);
            }
            UserInfo(String id, String name, String email, ArrayList<UUID2<Book>> acceptedBooks, Model.Domain.UserInfo.Account account) {
                this(UUID.fromString(id), name, email, acceptedBooks, account);
            }
            UserInfo(UUID2<User> id, String name, String email) {
                this(id, name, email, new ArrayList<UUID2<Book>>(), new Model.Domain.UserInfo.Account());
            }
            UserInfo(UUID uuid, String name, String email) {
                this(new UUID2<User>(uuid), name, email);
            }
            UserInfo(String id, String name, String email) {
                this(UUID.fromString(id), name, email);
            }

            @Override
            public UUID2<User> id() {
                return id;
            }

            ///////////////////////////////
            // Published Simple Getters  //
            ///////////////////////////////

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
            public Model.Domain.UserInfo toDeepCopyDomainInfo() {
                // Note: Must return a deep copy (no original references)
                Model.Domain.UserInfo domainInfoCopy = new Model.Domain.UserInfo(this);

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

        static public class LibraryInfo extends Model.Domain implements Model.ToDomainInfo<Model.Domain.LibraryInfo> {
            private final UUID2<Library> id;  // note this is a UUID2<Library> not a UUID2<LibraryInfo>, it is the id of the Library.
            final String name;
            final private UUID2.HashMap<User, ArrayList<UUID2<Book>>> userIdToCheckedOutBookIdMap;  // registered users of this library
            final private UUID2.HashMap<Book, Integer> bookIdToNumBooksAvailableMap;  // books known & available in this library

            LibraryInfo(@NotNull
                        UUID2<Library> id,
                        String name,
                        UUID2.HashMap<User, ArrayList<UUID2<Book>>> checkoutUserBookMap,
                        UUID2.HashMap<Book, Integer> bookIdToNumBooksAvailableMap
            ) {
                super(id, Model.Domain.LibraryInfo.class.getName());
                this.name = name;
                this.userIdToCheckedOutBookIdMap = checkoutUserBookMap;
                this.bookIdToNumBooksAvailableMap = bookIdToNumBooksAvailableMap;
                this.id = id;
            }
            LibraryInfo(UUID2<Library> id, String name) {
                this(id, name, new UUID2.HashMap<>(), new UUID2.HashMap<>());
            }
            LibraryInfo(Model.Domain.LibraryInfo libraryInfo) {
                this(libraryInfo.id,
                        libraryInfo.name,
                        libraryInfo.userIdToCheckedOutBookIdMap,
                        libraryInfo.bookIdToNumBooksAvailableMap);
            }
            LibraryInfo(UUID uuid, String name) {
                this(new UUID2<Library>(uuid), name);
            }
            LibraryInfo(String id, String name) {
                this(UUID.fromString(id), name);
            }

            @Override
            public String toString() {
                return this.toPrettyJson();
//                return "Library: " + this.name + " (" + this.id + ")" + "\n" +
//                        "  Available Books: " + this.bookIdToNumBooksAvailableMap + "\n" +
//                        "  Checkout Map: " + this.userIdToCheckedOutBookMap;
            }

            @Override
            public UUID2<Library> id() {
                return id;
            }

            /////////////////////////////////////////////
            // Published Domain Business Logic Methods //
            /////////////////////////////////////////////

            public Result<UUID2<Book>> checkOutBookToUser(UUID2<Book> bookId, UUID2<User> userId) {
                if(!isBookIdKnown(bookId)) return new Result.Failure<>(new IllegalArgumentException("bookId is not known. bookId: " + bookId));
                if(!isUserIdKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known, userId: " + userId));
                if(!isBookIdAvailable(bookId)) return new Result.Failure<>(new IllegalArgumentException("book is not available, bookId: " + bookId));
                if(isBookCurrentlyCheckedOutByUser(bookId, userId)) return new Result.Failure<>(new IllegalArgumentException("book is already checked out by user, bookId: " + bookId + ", userId: " + userId));

                try {
                    removeBookIdFromInventory(bookId, 1);
                    addBookIdToUser(bookId, userId);
                } catch (Exception e) {
                    return new Result.Failure<>(e);
                }

                return new Result.Success<>(bookId);
            }
            public Result<Book> checkOutBookToUser(Book book, User user) {
                Result<UUID2<Book>> checkedOutUUID2Book = checkOutBookToUser(book.id, user.id);

                if(checkedOutUUID2Book instanceof Result.Failure) {
                    return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) checkedOutUUID2Book).exception().getMessage()));
                }

                return new Result.Success<>(book);
            }

            public Result<UUID2<Book>> checkInBookFromUser(UUID2<Book> bookId, UUID2<User> userId) {
                if(!isBookIdKnown(bookId)) return new Result.Failure<>(new IllegalArgumentException("bookId is not known"));
                if(!isUserIdKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known"));
                if(!isBookCurrentlyCheckedOutByUser(bookId, userId)) return new Result.Failure<>(new IllegalArgumentException("book is not checked out by user"));

                try {
                    addBookIdToInventory(bookId, 1);
                    removeBookIdFromUserId(bookId, userId);
                } catch (Exception e) {
                    return new Result.Failure<>(e);
                }

                return new Result.Success<>(bookId);
            }
            public Result<Book> checkInBookFromUser(Book book, User user) {
                Result<UUID2<Book>> returnedUUID2Book = checkInBookFromUser(book.id, user.id);

                if(returnedUUID2Book instanceof Result.Failure) {
                    return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) returnedUUID2Book).exception().getMessage()));
                }

                return new Result.Success<>(book);
            }

            /////////////////////////////////////////
            // Published Domain Reporting Methods  //
            /////////////////////////////////////////

            public Result<ArrayList<UUID2<Book>>> findBooksCheckedOutByUserId(UUID2<User> userId) {
                if(!isUserIdKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known, id: " + userId));

                return new Result.Success<>(userIdToCheckedOutBookIdMap.get(userId));
            }


            public Result<HashMap<UUID2<Book>, Integer>> calculateAvailableBookIdToCountOfAvailableBooksList() {
                HashMap<UUID2<Book>, Integer> availableBookIdToNumBooksAvailableMap = new HashMap<>();

//                HashSet<Book> bookSet = new HashSet<>();
                Set<UUID2<Book>> bookSet = this.bookIdToNumBooksAvailableMap.keys();

//                for(UUID2<Book> bookId : this.bookIdToNumBooksAvailableMap.keys()) {
                for(UUID2<Book> bookId : bookSet) {
                    if(isBookIdAvailable(bookId)) {
                        int numBooksAvail = this.bookIdToNumBooksAvailableMap.get(bookId);
                        availableBookIdToNumBooksAvailableMap.put(bookId, numBooksAvail);
                    }
                }

                return new Result.Success<>(availableBookIdToNumBooksAvailableMap);
            }

            /////////////////////////////////
            // Published Helper Methods    //
            /////////////////////////////////

            public boolean isBookIdKnown(UUID2<Book> bookId) {
                return bookIdToNumBooksAvailableMap.containsKey(bookId);
            }
            public boolean isBookIdKnown(Book book) {
                return isBookIdKnown(book.id);
            }

            public boolean isUserIdKnown(UUID2<User> userId) {
                return userIdToCheckedOutBookIdMap.containsKey(userId);
            }
            public boolean isUserIdKnown(User user) {
                return isUserIdKnown(user.id);
            }

            public boolean isBookIdAvailable(UUID2<Book> bookId) {
                return bookIdToNumBooksAvailableMap.get(bookId) > 0;
            }
            public boolean isBookIdAvailable(Book book) {
                return isBookIdAvailable(book.id);
            }

            public boolean isBookCurrentlyCheckedOutByUser(UUID2<Book> bookId, UUID2<User> userId) {
                return userIdToCheckedOutBookIdMap.get(userId.uuid()).contains(bookId);
            }
            public boolean isBookCurrentlyCheckedOutByUser(Book book, User user) {
                return isBookCurrentlyCheckedOutByUser(book.id, user.id);
            }

            public Result<UUID2<User>> registerUser(UUID2<User> userId) {
                return insertUserId(userId);
            }

            /////////////////////////////////////////
            // Published Testing Helper Methods    //
            /////////////////////////////////////////

            public Result<UUID2<Book>> addTestBook(UUID2<Book> bookId, int quantity) {
                return addBookIdToInventory(bookId, quantity);
            }

            protected Result<UUID2<User>> upsertTestUser(UUID2<User> userId) {
                return upsertUserId(userId);
            }

            //////////////////////////////
            // Private Helper Functions //
            //////////////////////////////

            private Result<UUID2<Book>> addBookIdToInventory(UUID2<Book> bookId, int quantity) {
                if(quantity <= 0) return new Result.Failure<>(new IllegalArgumentException("quantity must be > 0"));

                try {
                    if (bookIdToNumBooksAvailableMap.containsKey(bookId.uuid())) {
                        bookIdToNumBooksAvailableMap.put(bookId.uuid(), bookIdToNumBooksAvailableMap.get(bookId.uuid()) + 1);
                    } else {
                        bookIdToNumBooksAvailableMap.put(bookId.uuid(), 1);
                    }
                } catch (Exception e) {
                    return new Result.Failure<>(e);
                }

                return new Result.Success<>(bookId);
            }
            private Result<Book> addBookToInventory(Book book, int quantity) {
                Result<UUID2<Book>> addedUUID2Book = addBookIdToInventory(book.id, quantity);

                if(addedUUID2Book instanceof Result.Failure) {
                    return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) addedUUID2Book).exception().getMessage()));
                }

                return new Result.Success<>(book);
            }

            private Result<UUID2<Book>> removeBookIdFromInventory(UUID2<Book> bookId, int quantity) {
                if(quantity <= 0) return new Result.Failure<>(new IllegalArgumentException("quantity must be > 0"));

                try {
                    if (bookIdToNumBooksAvailableMap.containsKey(bookId.uuid())) {
                        bookIdToNumBooksAvailableMap.put(bookId.uuid(), bookIdToNumBooksAvailableMap.get(bookId.uuid()) - 1);
                    } else {
                        return new Result.Failure<>(new Exception("Book not in inventory"));
                    }
                } catch (Exception e) {
                    return new Result.Failure<>(e);
                }

                return new Result.Success<>(bookId);
            }
            private Result<Book> removeBookFromInventory(Book book, int quantity) {
                Result<UUID2<Book>> removedUUID2Book = removeBookIdFromInventory(book.id, quantity);

                if(removedUUID2Book instanceof Result.Failure) {
                    return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) removedUUID2Book).exception().getMessage()));
                }

                return new Result.Success<>(book);
            }

            private Result<UUID2<Book>> addBookIdToUser(UUID2<Book> bookId, UUID2<User> userId) {
                if(!isBookIdKnown(bookId)) return new Result.Failure<>(new IllegalArgumentException("bookId is not known"));
                if(!isUserIdKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known"));
                if(isBookCurrentlyCheckedOutByUser(bookId, userId)) return new Result.Failure<>(new IllegalArgumentException("book is already checked out by user"));

                try {
                    if(userIdToCheckedOutBookIdMap.containsKey(userId.uuid())) {
                        userIdToCheckedOutBookIdMap.get(userId).add(bookId);
                    } else {
                        //noinspection ArraysAsListWithZeroOrOneArgument
                        userIdToCheckedOutBookIdMap.put(userId.uuid(), new ArrayList<>(Arrays.asList(bookId)));
                    }
                } catch (Exception e) {
                    return new Result.Failure<>(e);
                }

                return new Result.Success<>(bookId);
            }
            private Result<Book> addBookToUser(Book book, User user) {
                Result<UUID2<Book>> addedUUID2Book = addBookIdToUser(book.id, user.id);

                if(addedUUID2Book instanceof Result.Failure) {
                    return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) addedUUID2Book).exception().getMessage()));
                }

                return new Result.Success<>(book);
            }

            private Result<UUID2<Book>> removeBookIdFromUserId(UUID2<Book> bookId, UUID2<User> userId) {
                if(!isBookIdKnown(bookId)) return new Result.Failure<>(new IllegalArgumentException("bookId is not known"));
                if(!isUserIdKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known"));
                if(!isBookCurrentlyCheckedOutByUser(bookId, userId)) return new Result.Failure<>(new IllegalArgumentException("book is not checked out by user"));

                try {
                    userIdToCheckedOutBookIdMap.get(userId.uuid()).remove(bookId);
                } catch (Exception e) {
                    return new Result.Failure<>(e);
                }

                return new Result.Success<>(bookId);
            }
            private Result<Book> removeBookFromUser(Book book, User user) {
                Result<UUID2<Book>> removedUUID2Book = removeBookIdFromUserId(book.id, user.id);

                if(removedUUID2Book instanceof Result.Failure) {
                    return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) removedUUID2Book).exception().getMessage()));
                }

                return new Result.Success<>(book);
            }

            private Result<UUID2<User>> insertUserId(UUID2<User> userId) {
                if(isUserIdKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is already known"));

                try {
                    userIdToCheckedOutBookIdMap.put(userId.uuid(), new ArrayList<>());
                } catch (Exception e) {
                    return new Result.Failure<>(e);
                }

                return new Result.Success<>(userId);
            }

            private Result<UUID2<User>> upsertUserId(UUID2<User> userId) {
                if(isUserIdKnown(userId)) return new Result.Success<>(userId);

                return insertUserId(userId);
            }

            private Result<UUID2<User>> removeUserId(UUID2<User> userId) {
                if(!isUserIdKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known"));

                try {
                    userIdToCheckedOutBookIdMap.remove(userId.uuid());
                } catch (Exception e) {
                    return new Result.Failure<>(e);
                }

                return new Result.Success<>(userId);
            }

            /////////////////////////////////
            // ToInfo implementation //
            /////////////////////////////////

            // note: currently no DB or API for UserInfo (so no .ToEntity() or .ToDTO())
            @Override
            public Model.Domain.LibraryInfo toDeepCopyDomainInfo() {
                // Note: *MUST* return a deep copy
                Model.Domain.LibraryInfo libraryInfoDeepCopy = new Model.Domain.LibraryInfo(this.id, this.name);

                // Deep copy the bookIdToNumBooksAvailableMap
                libraryInfoDeepCopy.bookIdToNumBooksAvailableMap.putAll(this.bookIdToNumBooksAvailableMap);

                // Deep copy the userIdToCheckedOutBookMap
                for (Map.Entry<UUID, ArrayList<UUID2<Book>>> entry : this.userIdToCheckedOutBookIdMap.entrySet()) {
                    libraryInfoDeepCopy.userIdToCheckedOutBookIdMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                }

                return libraryInfoDeepCopy;
            }

            @Override
            public UUID2<?> getDomainInfoId() {
                return this.id;
            }
        }
    }

    // Data Transfer Objects for APIs
    // - Simple data holder class for transferring data to/from the Domain from API
    // - Objects can be created from JSON
    static public class DTO extends Model {
        public DTO(UUID2<IUUID2> id, String className) {
            super(id, className);
        }

        public static class BookInfo extends Model.DTO
                implements
                Model.ToDomainInfo<Model.Domain.BookInfo>,
                Model.ToDomainInfo.hasToDeepCopyDomainInfo<Model.Domain.BookInfo>,
                Info.ToInfo<Model.DTO.BookInfo>,
                Info.hasToDeepCopyInfo<Model.DTO.BookInfo>
        {
            final UUID2<Book> id; // note this is a UUID2<Book> and not a UUID2<BookInfo>
            final String title;
            final String author;
            final String description;
            final String extraFieldToShowThisIsADTO;

            BookInfo(@NotNull
                     UUID2<Book> id,
                     String title,
                     String author,
                     String description,
                     String extraFieldToShowThisIsADTO
            ) {
                super(id.toDomainUUID2(), Model.DTO.BookInfo.class.getName());
                this.id = id;
                this.title = title;
                this.author = author;
                this.description = description;

                if(extraFieldToShowThisIsADTO == null) {
                    this.extraFieldToShowThisIsADTO = "This is a DTO";
                } else {
                    this.extraFieldToShowThisIsADTO = extraFieldToShowThisIsADTO;
                }
            }
            BookInfo(String json, Context context) {
                this(context.gson.fromJson(json, Model.DTO.BookInfo.class));  // creates a DTO.BookInfo from the JSON
            }

            // Note: Intentionally DON'T accept `Entity.BookInfo` (to keep DB layer separate from API layer)
            BookInfo(Model.DTO.BookInfo bookInfo) {
                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description, bookInfo.extraFieldToShowThisIsADTO);
            }
            BookInfo(Model.Domain.BookInfo bookInfo) {
                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description, "Imported from Domain.BookInfo");
            }
            // todo - Is it better to have a constructor that takes in a DTO.BookInfo and throws an exception? Or to not have it at all?
            // BookInfo(Entity.BookInfo bookInfo) {
            //     // Never accept Entity.BookInfo to keep the API layer separate from the DB layer
            //     super(bookInfo.id.toDomainUUID2());
            //     throw new IllegalArgumentException("DTO.BookInfo should never be created from Entity.BookInfo");
            // }

            @Override
            public String toString() {
                return "Book (" + this.id + ") : " + this.title + " by " + this.author +", " + this.description;
            }

            ///////////////////////////////////////////
            // DTOs don't have any business logic    //
            ///////////////////////////////////////////

            ///////////////////////////////////
            // ToDomainInfo implementation   //
            ///////////////////////////////////

            @Override
            public Model.Domain.BookInfo toDeepCopyDomainInfo() {
                // note: implement deep copy, if required.
                return new Model.Domain.BookInfo(
                        this.id,
                        this.title,
                        this.author,
                        this.description
                );
            }

            @Override
            public UUID2<Book> getDomainInfoId() {
                return this.id;
            }

            /////////////////////////////
            // ToInfo implementation   //
            /////////////////////////////

            @Override
            public Model.DTO.BookInfo toDeepCopyInfo() {
                // note: implement deep copy, if needed.
                return new Model.DTO.BookInfo(this);
            }

            @Override
            public UUID2<Book> getInfoId() {
                return this.id;
            }
        }
    }

    // Entities for Databases
    // Simple data holder class for transferring data to/from the Domain from Database
//    static class Entity extends Model implements Info.hasToDeepCopyInfo<DomainInfo<Domain>> {
    static public class Entity extends Model {
        Entity(UUID2<IUUID2> id, String className) {
            super(id, className);
        }

        public static class BookInfo extends Model.Entity
                implements
                Model.ToDomainInfo<Model.Domain.BookInfo>,
                Model.ToDomainInfo.hasToDeepCopyDomainInfo<Model.Domain.BookInfo>,
                Info.ToInfo<Model.Entity.BookInfo>,
                Info.hasToDeepCopyInfo<Model.Entity.BookInfo>
        {
            final UUID2<Book> id;  // note this is a UUID2<Book> and not a UUID2<BookInfo>
            final String title;
            final String author;
            final String description;
            final String extraFieldToShowThisIsAnEntity = "This is an Entity";

            BookInfo(
                    @NotNull UUID2<Book> id,
                    String title,
                    String author,
                    String description
            ) {
                super(id.toDomainUUID2(), Model.Entity.BookInfo.class.getName());
                this.id = id;
                this.title = title;
                this.author = author;
                this.description = description;
            }

            // Note: Intentionally DON'T accept `DTO.BookInfo` (to keep DB layer separate from API layer)
            BookInfo(Model.Entity.BookInfo bookInfo) {
                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description);
            }
            BookInfo(Model.Domain.BookInfo bookInfo) {
                this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description);
            }
            // todo Is it better to have a constructor that takes in a DTO.BookInfo and throws an exception? Or to not have it at all?
            // BookInfo(DTO.BookInfo bookInfo) {
            //     // Never accept DTO.BookInfo to keep the API layer separate from the DB layer
            //     super(bookInfo.id.toDomainUUID2());
            //     throw new IllegalArgumentException("Entity.BookInfo should never be created from DTO.BookInfo");
            // }

            @Override
            public String toString() {
                return "Book (" + this.id + ") : " + this.title + " by " + this.author +", " + this.description;
            }

            ////////////////////////////////////////////
            // Entities don't have any business logic //
            ////////////////////////////////////////////

            /////////////////////////////////
            // ToDomainInfo implementation //
            /////////////////////////////////

            @Override
            public Model.Domain.BookInfo toDeepCopyDomainInfo() {
                // implement deep copy, if needed.
                return new Model.Domain.BookInfo(this);
            }

            @Override
            public UUID2<?> getDomainInfoId() {
                return this.id;
            }

            /////////////////////////////
            // ToInfo implementation   //
            /////////////////////////////

            @Override
            public Model.Entity.BookInfo toDeepCopyInfo() {
                // note: implement deep copy, if needed.
                return new Model.Entity.BookInfo(this);
            }

            @Override
            public UUID2<Book> getInfoId() {
                return this.id;
            }
        }
    }
}
