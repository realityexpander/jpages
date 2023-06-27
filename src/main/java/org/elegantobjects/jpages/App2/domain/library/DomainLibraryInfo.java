package org.elegantobjects.jpages.App2.domain.library;

import org.elegantobjects.jpages.App2.domain.common.DomainInfo;
import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.common.ModelInfo;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DomainLibraryInfo extends DomainInfo
        implements
        ModelInfo.ToDomain<DomainLibraryInfo>
{
    private final UUID2<Library> id;  // note this is a UUID2<Library> not a UUID2<LibraryInfo>, it is the id of the Library.
    final private String name;
    final private UUID2.HashMap<User, ArrayList<UUID2<Book>>> userIdToCheckedOutBookIdMap;  // registered users of this library
    final private UUID2.HashMap<Book, Integer> bookIdToNumBooksAvailableMap;  // books known & available in this library

    public DomainLibraryInfo(
        @NotNull UUID2<Library> id,
        String name,
        UUID2.HashMap<User, ArrayList<UUID2<Book>>> checkoutUserBookMap,
        UUID2.HashMap<Book, Integer> bookIdToNumBooksAvailableMap
    ) {
        super(id, DomainLibraryInfo.class.getName());
        this.name = name;
        this.userIdToCheckedOutBookIdMap = checkoutUserBookMap;
        this.bookIdToNumBooksAvailableMap = bookIdToNumBooksAvailableMap;
        this.id = id;
    }
    public DomainLibraryInfo(UUID2<Library> id, String name) {
        this(id, name, new UUID2.HashMap<>(), new UUID2.HashMap<>());
    }
    public DomainLibraryInfo(@NotNull DomainLibraryInfo libraryInfo) {
        this(
            libraryInfo.id,
            libraryInfo.name,
            libraryInfo.userIdToCheckedOutBookIdMap,
            libraryInfo.bookIdToNumBooksAvailableMap
        );
    }
    public DomainLibraryInfo(UUID uuid, String name) {
        this(new UUID2<Library>(uuid), name);
    }
    public DomainLibraryInfo(String id, String name) {
        this(UUID.fromString(id), name);
    }

    @Override
    public String toString() {
        return this.toPrettyJson();
    }

    ///////////////////////////////
    // Published Simple Getters  //
    ///////////////////////////////

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

        Set<UUID2<Book>> bookSet = this.bookIdToNumBooksAvailableMap.keys();

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

    ///////////////////////////
    // ToInfo implementation //
    ///////////////////////////

    // note: currently no DB or API for UserInfo (so no .ToEntity() or .ToDTO())
    @Override
    public DomainLibraryInfo toDeepCopyDomainInfo() {
        // Note: *MUST* return a deep copy
        DomainLibraryInfo libraryInfoDeepCopy = new DomainLibraryInfo(this.id, this.name);

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
