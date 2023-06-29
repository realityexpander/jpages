package org.elegantobjects.jpages.App2.domain.library;

import org.elegantobjects.jpages.App2.domain.common.DomainInfo;
import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.common.Model;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LibraryInfo extends DomainInfo
        implements
        Model.ToInfoDomain<LibraryInfo>
{
    public final UUID2<Library> id;  // note this is a UUID2<Library> not a UUID2<LibraryInfo>, it is the id of the Library.
    final private String name;
    final private UUID2.HashMap<User, ArrayList<UUID2<Book>>> userIdToCheckedOutBookIdMap;  // registered users of this library
    final private UUID2.HashMap<Book, Integer> bookIdToNumBooksAvailableMap;  // known books & number available in this library

    public LibraryInfo(
        @NotNull UUID2<Library> id,
        String name,
        UUID2.HashMap<User, ArrayList<UUID2<Book>>> userIdToCheckedOutBookIdMap,
        UUID2.HashMap<Book, Integer> bookIdToNumBooksAvailableMap
    ) {
        super(id);
        this.name = name;
        this.userIdToCheckedOutBookIdMap = userIdToCheckedOutBookIdMap;
        this.bookIdToNumBooksAvailableMap = bookIdToNumBooksAvailableMap;
        this.id = id;
    }
    public LibraryInfo(UUID2<Library> id, String name) {
        this(id, name, new UUID2.HashMap<>(), new UUID2.HashMap<>());
    }
    public LibraryInfo(@NotNull LibraryInfo libraryInfo) {
        this(
            libraryInfo.id,
            libraryInfo.name,
            libraryInfo.userIdToCheckedOutBookIdMap,
            libraryInfo.bookIdToNumBooksAvailableMap
        );
    }
    public LibraryInfo(UUID uuid, String name) {
        this(new UUID2<Library>(uuid, Library.class), name);
    }
    public LibraryInfo(String id, String name) {
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
        if (!isBookKnown(bookId))
            return new Result.Failure<>(new IllegalArgumentException("bookId is not known. bookId: " + bookId));
        if (!isUserKnown(userId))
            return new Result.Failure<>(new IllegalArgumentException("userId is not known, userId: " + userId));
        if (!isBookAvailable(bookId))
            return new Result.Failure<>(new IllegalArgumentException("book is not available, bookId: " + bookId));
        if (isBookCheckedOutByUser(bookId, userId))
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
        if (!isBookKnown(bookId))
            return new Result.Failure<>(new IllegalArgumentException("bookId is not known, bookId: " + bookId));
        if (!isUserKnown(userId))
            return new Result.Failure<>(new IllegalArgumentException("userId is not known, userId: " + userId));
        if (!isBookCheckedOutByUser(bookId, userId))
            return new Result.Failure<>(new IllegalArgumentException("Book is not checked out by User, bookId: " + bookId + ", userId: " + userId));

        try {
            addBookIdToInventory(bookId, 1);
            removeBookIdFromUserId(bookId, userId);
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }

        return new Result.Success<>(bookId);
    }
    public Result<Book> checkInBookFromUser(Book book, User user) {
        Result<UUID2<Book>> returnedBookIdResult = checkInBookFromUser(book.id, user.id);

        if (returnedBookIdResult instanceof Result.Failure) {
            return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) returnedBookIdResult).exception().getMessage()));
        }

        return new Result.Success<>(book);
    }

    public Result<Book> swapBookCheckoutFromUserToUser(
        @NotNull Book book,
        @NotNull User fromUser,
        @NotNull User toUser
    ) {
        if (toUser.fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(toUser.fetchInfoFailureReason()));
        if (fromUser.fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fromUser.fetchInfoFailureReason()));

        // Check if the fromUser can transfer the Book
        if (!isUserKnown(fromUser.id))
            return new Result.Failure<>(new IllegalArgumentException("fromUser is not known, fromUserId: " + fromUser.id));
        if (!isBookKnown(book.id))
            return new Result.Failure<>(new IllegalArgumentException("bookId is not known, bookId: " + book.id));
        if (!isBookCheckedOutByUser(book.id, fromUser.id))
            return new Result.Failure<>(new IllegalArgumentException("Book is not checked out by User, bookId: " + book.id + ", fromUserId: " + fromUser.id));
        if (!fromUser.accountInfo().isAccountInGoodStanding())
            return new Result.Failure<>(new IllegalArgumentException("fromUser Account is not in good standing, fromUserId: " + fromUser.id));

        // Check if receiving User can check out Book
        if (!isUserKnown(toUser.id))
            return new Result.Failure<>(new IllegalArgumentException("toUser is not known, toUser: " + toUser.id));
        if (!isBookKnown(book.id))
            return new Result.Failure<>(new IllegalArgumentException("bookId is not known, bookId: " + book.id));
        if (!toUser.accountInfo().isAccountInGoodStanding())
            return new Result.Failure<>(new IllegalArgumentException("toUser Account is not in good standing, toUser: " + toUser.id));
        if (toUser.hasReachedMaxNumAcceptedBooks())
            return new Result.Failure<>(new IllegalArgumentException("toUser has reached max number of accepted Books, toUser: " + toUser.id));

        Result<UUID2<Book>> returnedBookIdResult = checkInBookFromUser(book.id, fromUser.id);
        if (returnedBookIdResult instanceof Result.Failure) {
            return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) returnedBookIdResult).exception().getMessage()));
        }

        Result<UUID2<Book>> checkedOutBookIdResult = checkOutBookToUser(book.id, toUser.id);
        if (checkedOutBookIdResult instanceof Result.Failure) {
            return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) checkedOutBookIdResult).exception().getMessage()));
        }

        return new Result.Success<>(book);
    }

    /////////////////////////////////////////
    // Published Domain Reporting Methods  //
    /////////////////////////////////////////

    public Result<ArrayList<UUID2<Book>>> findBooksCheckedOutByUserId(UUID2<User> userId) {
        if (!isUserKnown(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known, id: " + userId));

        return new Result.Success<>(userIdToCheckedOutBookIdMap.get(userId));
    }

    public Result<HashMap<UUID2<Book>, Integer>> calculateAvailableBookIdToCountOfAvailableBooksList() {
        HashMap<UUID2<Book>, Integer> availableBookIdToNumBooksAvailableMap = new HashMap<>();

        Set<UUID2<Book>> bookSet = this.bookIdToNumBooksAvailableMap.keys();

        for (UUID2<Book> bookId : bookSet) {
            if (isBookAvailable(bookId)) {
                int numBooksAvail = this.bookIdToNumBooksAvailableMap.get(bookId);
                availableBookIdToNumBooksAvailableMap.put(bookId, numBooksAvail);
            }
        }

        return new Result.Success<>(availableBookIdToNumBooksAvailableMap);
    }

    /////////////////////////////////
    // Published Helper Methods    //
    /////////////////////////////////

    public boolean isBookKnown(UUID2<Book> bookId) {
        return bookIdToNumBooksAvailableMap.containsKey(bookId);
    }
    public boolean isBookKnown(@NotNull Book book) {
        return isBookKnown(book.id);
    }

    public boolean isUserKnown(UUID2<User> userId) {
        return userIdToCheckedOutBookIdMap.containsKey(userId);
    }
    public boolean isUserKnown(@NotNull User user) {
        return isUserKnown(user.id);
    }

    public boolean isBookAvailable(UUID2<Book> bookId) {
        return bookIdToNumBooksAvailableMap.get(bookId) > 0;
    }
    public boolean isBookAvailable(@NotNull Book book) {
        return isBookAvailable(book.id);
    }

    public boolean isBookCheckedOutByUser(UUID2<Book> bookId, @NotNull UUID2<User> userId) {
        return userIdToCheckedOutBookIdMap.get(userId.uuid()).contains(bookId);
    }
    public boolean isBookCheckedOutByUser(@NotNull Book book, @NotNull User user) {
        return isBookCheckedOutByUser(book.id, user.id);
    }

    public Result<UUID2<User>> registerUser(UUID2<User> userId) {
        return insertUserId(userId);
    }

    /////////////////////////////////////////
    // Published Testing Helper Methods    //
    /////////////////////////////////////////

    // Intention revealing method
    public Result<UUID2<Book>> addTestBook(UUID2<Book> bookId, int quantity) {
        return addBookIdToInventory(bookId, quantity);
    }

    // Convenience method - Called from PrivateLibrary class only
    // Intention revealing method
    public Result<UUID2<Book>> addPrivateBook(UUID2<Book> bookId, int quantity) {
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
        if (!isBookKnown(bookId))
            return new Result.Failure<>(new IllegalArgumentException("bookId is not known"));
        if (!isUserKnown(userId))
            return new Result.Failure<>(new IllegalArgumentException("userId is not known"));
        if (isBookCheckedOutByUser(bookId, userId))
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
        if (!isBookKnown(bookId))
            return new Result.Failure<>(new IllegalArgumentException("bookId is not known"));
        if (!isUserKnown(userId))
            return new Result.Failure<>(new IllegalArgumentException("userId is not known"));
        if (!isBookCheckedOutByUser(bookId, userId))
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
        if (isUserKnown(userId))
            return new Result.Failure<>(new IllegalArgumentException("userId is already known"));

        try {
            userIdToCheckedOutBookIdMap.put(userId.uuid(), new ArrayList<>());
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }

        return new Result.Success<>(userId);
    }
    private Result<UUID2<User>> upsertUserId(UUID2<User> userId) {
        if (isUserKnown(userId)) return new Result.Success<>(userId);

        return insertUserId(userId);
    }

    private Result<UUID2<User>> removeUserId(UUID2<User> userId) {
        if (!isUserKnown(userId))
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
