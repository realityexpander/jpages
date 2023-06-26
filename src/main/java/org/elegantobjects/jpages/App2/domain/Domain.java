package org.elegantobjects.jpages.App2.domain;

import org.elegantobjects.jpages.App2.data.Entity;
import org.elegantobjects.jpages.App2.data.DTO;
import org.elegantobjects.jpages.App2.Model;
import org.elegantobjects.jpages.App2.core.Result;
import org.elegantobjects.jpages.App2.core.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.core.uuid2.UUID2;
import org.jetbrains.annotations.NotNull;

import java.util.*;

// Domain objects contain the "{Model}Info" and the associated business logic to manipulate it
//    static class Domain extends Model implements Info.hasDeepCopy<ToDomainInfo<Domain>> {
public class Domain extends Model {

    // next lines are ugly java boilerplate to allow call to super() with a UUID2
    Domain(UUID2<?> id, String className) {
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

    static public class BookInfo extends Domain
        implements
            Model.ToEntity<Entity.BookInfo>,
            Model.ToDTO<DTO.BookInfo>,
            ToDomainInfo<BookInfo>
    {
        private final UUID2<Book> id; // note this is a UUID2<Book> not a UUID2<BookInfo>, it is the id of the Book.
        public final String title;
        public final String author;
        public final String description;

        public BookInfo(@NotNull
                        UUID2<Book> id,
                        String title,
                        String author,
                        String description
        ) {
            super(id, BookInfo.class.getName());
            this.title = title;
            this.author = author;
            this.description = description;
            this.id = id;
        }
        public BookInfo(UUID uuid, String title, String author, String description) {
            this(new UUID2<Book>(uuid), title, author, description);
        }
        public BookInfo(String id, String title, String author, String description) {
            this(UUID.fromString(id), title, author, description);
        }
        public BookInfo(@NotNull BookInfo bookInfo) {
            // todo validation
            this(bookInfo.id(), bookInfo.title, bookInfo.author, bookInfo.description);
        }
        public BookInfo(UUID id) {
            this(id, "", "", "");
        }

        // Domain Must accept both `DTO.BookInfo` and `Entity.BookInfo` (and convert to Domain.BookInfo)
        // Domain decides what to include from the DTOs/Entities
        // todo - should the DTO/Entites decide what to include?
        public BookInfo(DTO.BookInfo bookInfo) {
            // Converts from DTO to Domain
            // todo validation here

            // Domain decides what to include from the DTO
            this(bookInfo.getInfoId(),
                    bookInfo.title,
                    bookInfo.author,
                    bookInfo.description);
        }
        public BookInfo(Entity.BookInfo bookInfo) {
            // Converts from Entity to Domain
            // todo validation here

            // Domain decides what to include from the Entities
            this(bookInfo.getInfoId(),
                    bookInfo.title,
                    bookInfo.author,
                    bookInfo.description);
        }

        /////////////////////////////////////////////////
        // BookInfo Business Logic Methods             //
        // - All Info manipulation logic is done here. //
        /////////////////////////////////////////////////

        @Override
        public UUID2<Book> id() {
            return this.id;
        }

        public BookInfo withTitle(String title) {
            return new BookInfo(this.id, title, this.author, this.description);
        }

        public BookInfo withAuthor(String authorName) {
            return new BookInfo(this.id, this.title, authorName, this.description);
        }

        public BookInfo withDescription(String description) {
            return new BookInfo(this.id, this.title, this.author, description);
        }

        @Override
        public String toString() {
            return "Book (" + this.id + ") : " + this.title + " by " + this.author + ", " + this.description;
        }

        @Override
        public DTO.BookInfo toDTO() {
            return new DTO.BookInfo(this);
        }

        @Override
        public Entity.BookInfo toEntity() {
            return new Entity.BookInfo(this);
        }

        ///////////////////////////
        // ToInfo implementation //
        ///////////////////////////

        @Override
        public BookInfo toDeepCopyDomainInfo() {
            // shallow copy OK here bc its flat
            return new BookInfo(this);
        }

        @Override
        public UUID2<?> getDomainInfoId() {
            return this.id;
        }
    }

    static public class UserInfo extends Domain
        implements
            ToDomainInfo<UserInfo>
    {
        private final UUID2<User> id;  // note this is a UUID2<User> not a UUID2<UserInfo>, it is the id of the User.
        private final String name;
        private final String email;
        private final ArrayList<UUID2<Book>> acceptedBooks;
        private final UserInfo.Account account;

        static class Account {

            final UserInfo.Account.AccountStatus accountStatus;
            final int currentFineAmountPennies;
            final int maxBooks;             // max books allowed to be checked out
            final int maxDays;              // max number of days a book can be checked out
            final int maxRenewals;          // max number of renewals (per book)
            final int maxRenewalDays;       // max number days for each renewal (per book)
            final int maxFineAmountPennies; // max dollar amount of all fines allowed before account is suspended
            final int maxFineDays;          // max number of days to pay fine before account is suspended

            Account(UserInfo.Account.AccountStatus accountStatus,
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
                this.accountStatus = UserInfo.Account.AccountStatus.ACTIVE;
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
                UserInfo.Account.AccountStatus accountStatus;
                int maxBooks;
                int maxDays;
                int maxRenewals;
                int maxRenewalDays;
                int maxFines;
                int maxFineDays;
                int maxFineAmount;

                Builder() {
                    this.accountStatus = UserInfo.Account.AccountStatus.ACTIVE;
                } // default values

                Builder(UserInfo.Account account) {
                    this.accountStatus = account.accountStatus;
                    this.maxBooks = account.maxBooks;
                    this.maxDays = account.maxDays;
                    this.maxRenewals = account.maxRenewals;
                    this.maxRenewalDays = account.maxRenewalDays;
                    this.maxFines = account.maxFineAmountPennies;
                    this.maxFineDays = account.maxFineDays;
                    this.maxFineAmount = account.maxFineAmountPennies;
                }

                UserInfo.Account.Builder accountStatus(UserInfo.Account.AccountStatus accountStatus) {
                    this.accountStatus = accountStatus;
                    return this;
                }

                UserInfo.Account.Builder maxBooks(int maxBooks) {
                    this.maxBooks = maxBooks;
                    return this;
                }

                UserInfo.Account.Builder maxDays(int maxDays) {
                    this.maxDays = maxDays;
                    return this;
                }

                UserInfo.Account.Builder maxRenewals(int maxRenewals) {
                    this.maxRenewals = maxRenewals;
                    return this;
                }

                UserInfo.Account.Builder maxRenewalDays(int maxRenewalDays) {
                    this.maxRenewalDays = maxRenewalDays;
                    return this;
                }

                UserInfo.Account.Builder maxFines(int maxFines) {
                    this.maxFines = maxFines;
                    return this;
                }

                UserInfo.Account.Builder maxFineDays(int maxFineDays) {
                    this.maxFineDays = maxFineDays;
                    return this;
                }

                UserInfo.Account.Builder maxFineAmount(int maxFineAmount) {
                    this.maxFineAmount = maxFineAmount;
                    return this;
                }

                UserInfo.Account build() {
                    return new UserInfo.Account(
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
                 UserInfo.Account account
        ) {
            super(id.toDomainUUID2(), UserInfo.class.getName());
            this.id = id;
            this.name = name;
            this.email = email;
            this.acceptedBooks = acceptedBooks;
            this.account = account;
        }

        UserInfo(UserInfo userInfo) {
            this(userInfo.id,
                    userInfo.name,
                    userInfo.email,
                    userInfo.acceptedBooks,
                    userInfo.account);
        }

        UserInfo(UUID uuid, String name, String email, ArrayList<UUID2<Book>> acceptedBooks, UserInfo.Account account) {
            this(new UUID2<User>(uuid), name, email, acceptedBooks, account);
        }

        UserInfo(String id, String name, String email, ArrayList<UUID2<Book>> acceptedBooks, UserInfo.Account account) {
            this(UUID.fromString(id), name, email, acceptedBooks, account);
        }

        public UserInfo(UUID2<User> id, String name, String email) {
            this(id, name, email, new ArrayList<UUID2<Book>>(), new UserInfo.Account());
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
        public UserInfo toDeepCopyDomainInfo() {
            // Note: Must return a deep copy (no original references)
            UserInfo domainInfoCopy = new UserInfo(this);

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

    static public class LibraryInfo extends Domain
        implements
            ToDomainInfo<LibraryInfo>
    {
        private final UUID2<Library> id;  // note this is a UUID2<Library> not a UUID2<LibraryInfo>, it is the id of the Library.
        final private String name;
        final private UUID2.HashMap<User, ArrayList<UUID2<Book>>> userIdToCheckedOutBookIdMap;  // registered users of this library
        final private UUID2.HashMap<Book, Integer> bookIdToNumBooksAvailableMap;  // books known & available in this library

        LibraryInfo(@NotNull
                    UUID2<Library> id,
                    String name,
                    UUID2.HashMap<User, ArrayList<UUID2<Book>>> checkoutUserBookMap,
                    UUID2.HashMap<Book, Integer> bookIdToNumBooksAvailableMap
        ) {
            super(id, LibraryInfo.class.getName());
            this.name = name;
            this.userIdToCheckedOutBookIdMap = checkoutUserBookMap;
            this.bookIdToNumBooksAvailableMap = bookIdToNumBooksAvailableMap;
            this.id = id;
        }

        public LibraryInfo(UUID2<Library> id, String name) {
            this(id, name, new UUID2.HashMap<>(), new UUID2.HashMap<>());
        }

        LibraryInfo(LibraryInfo libraryInfo) {
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

        public String name() {
            return this.name;
        }

        /////////////////////////////////////////////
        // Published Domain Business Logic Methods //
        /////////////////////////////////////////////

        public Result<UUID2<Book>> checkOutBookToUser(UUID2<Book> bookId, UUID2<User> userId) {
            if (!isBookIdKnown(bookId))
                return new Result.Failure<>(new IllegalArgumentException("bookId is not known. bookId: " + bookId));
            if (!isUserIdKnown(userId))
                return new Result.Failure<>(new IllegalArgumentException("userId is not known, userId: " + userId));
            if (!isBookIdAvailable(bookId))
                return new Result.Failure<>(new IllegalArgumentException("book is not available, bookId: " + bookId));
            if (isBookCurrentlyCheckedOutByUser(bookId, userId))
                return new Result.Failure<>(new IllegalArgumentException("book is already checked out by user, bookId: " + bookId + ", userId: " + userId));

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

            if (checkedOutUUID2Book instanceof Result.Failure) {
                return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) checkedOutUUID2Book).exception().getMessage()));
            }

            return new Result.Success<>(book);
        }

        public Result<UUID2<Book>> checkInBookFromUser(UUID2<Book> bookId, UUID2<User> userId) {
            if (!isBookIdKnown(bookId))
                return new Result.Failure<>(new IllegalArgumentException("bookId is not known"));
            if (!isUserIdKnown(userId))
                return new Result.Failure<>(new IllegalArgumentException("userId is not known"));
            if (!isBookCurrentlyCheckedOutByUser(bookId, userId))
                return new Result.Failure<>(new IllegalArgumentException("book is not checked out by user"));

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

            if (returnedUUID2Book instanceof Result.Failure) {
                return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) returnedUUID2Book).exception().getMessage()));
            }

            return new Result.Success<>(book);
        }

        /////////////////////////////////////////
        // Published Domain Reporting Methods  //
        /////////////////////////////////////////

        public Result<ArrayList<UUID2<Book>>> findBooksCheckedOutByUserId(UUID2<User> userId) {
            if (!isUserIdKnown(userId))
                return new Result.Failure<>(new IllegalArgumentException("userId is not known, id: " + userId));

            return new Result.Success<>(userIdToCheckedOutBookIdMap.get(userId));
        }


        public Result<HashMap<UUID2<Book>, Integer>> calculateAvailableBookIdToCountOfAvailableBooksList() {
            HashMap<UUID2<Book>, Integer> availableBookIdToNumBooksAvailableMap = new HashMap<>();

//                HashSet<Book> bookSet = new HashSet<>();
            Set<UUID2<Book>> bookSet = this.bookIdToNumBooksAvailableMap.keys();

//                for(UUID2<Book> bookId : this.bookIdToNumBooksAvailableMap.keys()) {
            for (UUID2<Book> bookId : bookSet) {
                if (isBookIdAvailable(bookId)) {
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
            if (quantity <= 0) return new Result.Failure<>(new IllegalArgumentException("quantity must be > 0"));

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

            if (addedUUID2Book instanceof Result.Failure) {
                return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) addedUUID2Book).exception().getMessage()));
            }

            return new Result.Success<>(book);
        }

        private Result<UUID2<Book>> removeBookIdFromInventory(UUID2<Book> bookId, int quantity) {
            if (quantity <= 0) return new Result.Failure<>(new IllegalArgumentException("quantity must be > 0"));

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

            if (removedUUID2Book instanceof Result.Failure) {
                return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) removedUUID2Book).exception().getMessage()));
            }

            return new Result.Success<>(book);
        }

        private Result<UUID2<Book>> addBookIdToUser(UUID2<Book> bookId, UUID2<User> userId) {
            if (!isBookIdKnown(bookId))
                return new Result.Failure<>(new IllegalArgumentException("bookId is not known"));
            if (!isUserIdKnown(userId))
                return new Result.Failure<>(new IllegalArgumentException("userId is not known"));
            if (isBookCurrentlyCheckedOutByUser(bookId, userId))
                return new Result.Failure<>(new IllegalArgumentException("book is already checked out by user"));

            try {
                if (userIdToCheckedOutBookIdMap.containsKey(userId.uuid())) {
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

            if (addedUUID2Book instanceof Result.Failure) {
                return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) addedUUID2Book).exception().getMessage()));
            }

            return new Result.Success<>(book);
        }

        private Result<UUID2<Book>> removeBookIdFromUserId(UUID2<Book> bookId, UUID2<User> userId) {
            if (!isBookIdKnown(bookId))
                return new Result.Failure<>(new IllegalArgumentException("bookId is not known"));
            if (!isUserIdKnown(userId))
                return new Result.Failure<>(new IllegalArgumentException("userId is not known"));
            if (!isBookCurrentlyCheckedOutByUser(bookId, userId))
                return new Result.Failure<>(new IllegalArgumentException("book is not checked out by user"));

            try {
                userIdToCheckedOutBookIdMap.get(userId.uuid()).remove(bookId);
            } catch (Exception e) {
                return new Result.Failure<>(e);
            }

            return new Result.Success<>(bookId);
        }

        private Result<Book> removeBookFromUser(Book book, User user) {
            Result<UUID2<Book>> removedUUID2Book = removeBookIdFromUserId(book.id, user.id);

            if (removedUUID2Book instanceof Result.Failure) {
                return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) removedUUID2Book).exception().getMessage()));
            }

            return new Result.Success<>(book);
        }

        private Result<UUID2<User>> insertUserId(UUID2<User> userId) {
            if (isUserIdKnown(userId))
                return new Result.Failure<>(new IllegalArgumentException("userId is already known"));

            try {
                userIdToCheckedOutBookIdMap.put(userId.uuid(), new ArrayList<>());
            } catch (Exception e) {
                return new Result.Failure<>(e);
            }

            return new Result.Success<>(userId);
        }

        private Result<UUID2<User>> upsertUserId(UUID2<User> userId) {
            if (isUserIdKnown(userId)) return new Result.Success<>(userId);

            return insertUserId(userId);
        }

        private Result<UUID2<User>> removeUserId(UUID2<User> userId) {
            if (!isUserIdKnown(userId))
                return new Result.Failure<>(new IllegalArgumentException("userId is not known"));

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
        public LibraryInfo toDeepCopyDomainInfo() {
            // Note: *MUST* return a deep copy
            LibraryInfo libraryInfoDeepCopy = new LibraryInfo(this.id, this.name);

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
