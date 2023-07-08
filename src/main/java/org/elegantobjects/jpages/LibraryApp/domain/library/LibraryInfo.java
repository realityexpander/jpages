package org.elegantobjects.jpages.LibraryApp.domain.library;

import org.elegantobjects.jpages.LibraryApp.domain.common.DomainInfo;
import org.elegantobjects.jpages.LibraryApp.common.util.Result;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.common.Model;
import org.elegantobjects.jpages.LibraryApp.domain.book.Book;
import org.elegantobjects.jpages.LibraryApp.domain.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * LibraryInfo is a DomainInfo class for the Library domain object.<br>
 * <br>
 * LibraryInfo is a mutable class that contains information about the Library domain object.<br>
 * <br>
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

public class LibraryInfo extends DomainInfo
        implements
        Model.ToDomainInfo<LibraryInfo>
{
    public final String name;
    private final UUID2.HashMap<UUID2<User>, ArrayList<UUID2<Book>>> registeredUserIdToCheckedOutBookIdMap;  // registered users of this library
    private final UUID2.HashMap<UUID2<Book>, Long> bookIdToNumBooksAvailableMap;  // known books & number available in this library (inventory)

    public
    LibraryInfo(
        @NotNull UUID2<Library> id,
        @NotNull String name,
        @NotNull UUID2.HashMap<UUID2<User>, ArrayList<UUID2<Book>>> registeredUserIdToCheckedOutBookIdMap,
        @NotNull UUID2.HashMap<UUID2<Book>, Long> bookIdToNumBooksAvailableMap
    ) {
        super(id);
        this.name = name;
        this.registeredUserIdToCheckedOutBookIdMap = registeredUserIdToCheckedOutBookIdMap;
        this.bookIdToNumBooksAvailableMap = bookIdToNumBooksAvailableMap;
    }
    public
    LibraryInfo(@NotNull UUID2<Library> id, @NotNull String name) {
        this(id, name, new UUID2.HashMap<>(), new UUID2.HashMap<>());
    }
    LibraryInfo(@NotNull LibraryInfo libraryInfo) {
        this(
            libraryInfo.id(),
            libraryInfo.name,
            libraryInfo.registeredUserIdToCheckedOutBookIdMap,
            libraryInfo.bookIdToNumBooksAvailableMap
        );
    }
    public
    LibraryInfo(@NotNull UUID uuid, @NotNull String name) {
        this(new UUID2<>(uuid, Library.class), name);
    }
    public
    LibraryInfo(@NotNull String id, @NotNull String name) {
        this(UUID.fromString(id), name);
    }

    ///////////////////////////////
    // Published Simple Getters  //
    ///////////////////////////////

    // Convenience method to get the Type-safe id from the Class
    @Override @SuppressWarnings("unchecked")
    public UUID2<Library> id() {
        return (UUID2<Library>) super.id();
    }

    @Override
    public String toString() {
        return this.toPrettyJson();
    }

    ////////////////////////
    // Creational Methods //
    ////////////////////////

    public LibraryInfo withName(@NotNull String name) {
        return new LibraryInfo(this.id(), name, this.registeredUserIdToCheckedOutBookIdMap, this.bookIdToNumBooksAvailableMap);
    }

    /////////////////////////////////////////////
    // Published Domain Business Logic Methods //
    /////////////////////////////////////////////

    public Result<Book> checkOutPublicLibraryBookToUser(@NotNull Book book, @NotNull User user) {
    //  if(!book.isBookFromPublicLibrary())   // todo - should only allow public library books to be checked out from public libraries?
    //    return new Result.Failure<>(new IllegalArgumentException("Book is not from a public library, bookId: " + book.id()));

        Result<UUID2<Book>> checkedOutUUID2Book = checkOutPublicLibraryBookIdToUserId(book.id(), user.id());
        if (checkedOutUUID2Book instanceof Result.Failure)
            return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) checkedOutUUID2Book).exception().getMessage()));

        Result<ArrayList<Book>> unacceptBookResult = user.acceptBook(book);
        if (unacceptBookResult instanceof Result.Failure)
            return new Result.Failure<>(new Exception(((Result.Failure<ArrayList<Book>>) unacceptBookResult).exception().getMessage()));

        return new Result.Success<>(book);
    }
    public Result<Book> checkOutPrivateLibraryBookToUser(@NotNull Book book, @NotNull User user) {
        // Private library book check-outs skip Account checks.
        Result<Void> checkOutBookResult = _checkOutBookIdToUserId(book.id(), user.id());
        if (checkOutBookResult instanceof Result.Failure)
            return new Result.Failure<>(new Exception(((Result.Failure<Void>) checkOutBookResult).exception().getMessage()));

        Result<UUID2<Book>> addBookResult = addBookIdToRegisteredUser(book.id(), user.id());
        if (addBookResult instanceof Result.Failure)
            return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) addBookResult).exception().getMessage()));

        Result<ArrayList<Book>> acceptBookResult = user.acceptBook(book);
        if (acceptBookResult instanceof Result.Failure)
            return new Result.Failure<>(new Exception(((Result.Failure<ArrayList<Book>>) acceptBookResult).exception().getMessage()));

        return new Result.Success<>(book);
    }
    public Result<UUID2<Book>> checkOutPublicLibraryBookIdToUserId(@NotNull UUID2<Book> bookId, @NotNull UUID2<User> userId) {
        if (!isKnownBookId(bookId))
            return new Result.Failure<>(new IllegalArgumentException("BookId is not known. bookId: " + bookId));
        if (!isKnownUserId(userId))
            return new Result.Failure<>(new IllegalArgumentException("UserId is not known, userId: " + userId));
        if (!isBookIdAvailableToCheckout(bookId))
            return new Result.Failure<>(new IllegalArgumentException("Book is not in inventory, bookId: " + bookId));
        if (isBookIdCheckedOutByUserId(bookId, userId))
            return new Result.Failure<>(new IllegalArgumentException("Book is already checked out by User, bookId: " + bookId + ", userId: " + userId));

        Result<Void> checkOutBookResult = _checkOutBookIdToUserId(bookId, userId);
        if (checkOutBookResult instanceof Result.Failure)
            return new Result.Failure<>(new Exception(((Result.Failure<Void>) checkOutBookResult).exception().getMessage()));

        Result<UUID2<Book>> addBookResult = addBookIdToRegisteredUser(bookId, userId);
        if (addBookResult instanceof Result.Failure)
            return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) addBookResult).exception().getMessage()));

        return new Result.Success<>(bookId);
    }

    public Result<Book> checkInPublicLibraryBookFromUser(@NotNull Book book, @NotNull User user) {
        //    if(!book.isBookFromPublicLibrary()) // todo - should only allow public library books to be checked in?
        //        return new Result.Failure<>(new IllegalArgumentException("Book is not from a public library, bookId: " + book.id()));

        Result<UUID2<Book>> returnedBookIdResult = checkInPublicLibraryBookIdFromUserId(book.id(), user.id());
        if (returnedBookIdResult instanceof Result.Failure)
            return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) returnedBookIdResult).exception().getMessage()));

        Result<ArrayList<UUID2<Book>>> unacceptBookResult = user.unacceptBook(book);
        if (unacceptBookResult instanceof Result.Failure)
            return new Result.Failure<>(new Exception(((Result.Failure<ArrayList<UUID2<Book>>>) unacceptBookResult).exception().getMessage()));

        Result<UUID2<Book>> removeBookResult = removeBookIdFromRegisteredUserId(book.id(), user.id());
        if (removeBookResult instanceof Result.Failure)
            return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) removeBookResult).exception().getMessage()));

        return new Result.Success<>(book);
    }
    public Result<Book> checkInPrivateLibraryBookFromUser(@NotNull Book book, @NotNull User user) {
        //    if(!book.isBookFromPrivateLibrary()) // todo - should not allow private library books to be checked in from public library?
        //        return new Result.Failure<>(new IllegalArgumentException("Book is not from private library, bookId: " + book.id()));

        // Private Library Book check-ins skip all Public Library User Account checks.
        Result<Void> checkInBookResult = _checkInBookIdFromUserId(book.id(), user.id());
        if (checkInBookResult instanceof Result.Failure)
            return new Result.Failure<>(new Exception(((Result.Failure<Void>) checkInBookResult).exception().getMessage()));

        Result<ArrayList<UUID2<Book>>> unacceptBookResult = user.unacceptBook(book);
        if (unacceptBookResult instanceof Result.Failure)
            return new Result.Failure<>(new Exception(((Result.Failure<ArrayList<UUID2<Book>>>) unacceptBookResult).exception().getMessage()));

        Result<UUID2<Book>> removeBookResult = removeBookIdFromRegisteredUserId(book.id(), user.id());
        if (removeBookResult instanceof Result.Failure)
            return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) removeBookResult).exception().getMessage()));

        return new Result.Success<>(book);
    }
    public Result<UUID2<Book>> checkInPublicLibraryBookIdFromUserId(@NotNull UUID2<Book> bookId, @NotNull UUID2<User> userId) {
        if (!isKnownBookId(bookId))
            return new Result.Failure<>(new IllegalArgumentException("BookId is not known, bookId: " + bookId));  // todo - do we allow unknown books to be checked in, and just add them to the list?
        if (!isKnownUserId(userId))
            return new Result.Failure<>(new IllegalArgumentException("UserId is not known, userId: " + userId));
        if (!isBookIdCheckedOutByUserId(bookId, userId))
            return new Result.Failure<>(new IllegalArgumentException("Book is not checked out by User, bookId: " + bookId + ", userId: " + userId));

        Result<Void> checkInBookResult = _checkInBookIdFromUserId(bookId, userId);
        if (checkInBookResult instanceof Result.Failure)
            return new Result.Failure<>(new Exception(((Result.Failure<Void>) checkInBookResult).exception().getMessage()));

        return new Result.Success<>(bookId);
    }

    public Result<Book> transferCheckedOutBookFromUserToUser(
        @NotNull Book book,
        @NotNull User fromUser,
        @NotNull User toUser
    ) {
        if (toUser.fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(toUser.fetchInfoFailureReason()));
        if (fromUser.fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fromUser.fetchInfoFailureReason()));

        if(book.isBookFromPublicLibrary()) {
            // Check if the fromUser can transfer the Book
            if (!isKnownUserId(fromUser.id()))
                return new Result.Failure<>(new IllegalArgumentException("fromUser is not known, fromUserId: " + fromUser.id()));
            if (!fromUser.accountInfo().isAccountInGoodStanding())
                return new Result.Failure<>(new IllegalArgumentException("fromUser Account is not in good standing, fromUserId: " + fromUser.id()));

            // Check if receiving User can check out Book
            if (!isKnownUserId(toUser.id()))
                return new Result.Failure<>(new IllegalArgumentException("toUser is not known, toUser: " + toUser.id()));
            if (!toUser.accountInfo().isAccountInGoodStanding())
                return new Result.Failure<>(new IllegalArgumentException("toUser Account is not in good standing, toUser: " + toUser.id()));
            if (toUser.hasReachedMaxAmountOfAcceptedPublicLibraryBooks())
                return new Result.Failure<>(new IllegalArgumentException("toUser has reached max number of accepted Public Library Books, toUser: " + toUser.id()));
        }

        Result<Void> returnedBookResult = _checkInBookIdFromUserId(book.id(), fromUser.id());
        if (returnedBookResult instanceof Result.Failure)
            return new Result.Failure<>(new Exception(((Result.Failure<Void>) returnedBookResult).exception().getMessage()));

        Result<Void> checkedOutBookIdResult = _checkOutBookIdToUserId(book.id(), toUser.id());
        if (checkedOutBookIdResult instanceof Result.Failure)
            return new Result.Failure<>(new Exception(((Result.Failure<Void>) checkedOutBookIdResult).exception().getMessage()));

        return new Result.Success<>(book);
    }

    /////////////////////////////////////////
    // Published Domain Reporting Methods  //
    /////////////////////////////////////////

    public Result<ArrayList<UUID2<Book>>> findAllCheckedOutBookIdsByUserId(@NotNull UUID2<User> userId) {
        if (!isKnownUserId(userId)) return new Result.Failure<>(new IllegalArgumentException("userId is not known, id: " + userId));

        return new Result.Success<>(registeredUserIdToCheckedOutBookIdMap.get(userId));
    }

    public Result<HashMap<UUID2<Book>, Long>> calculateAvailableBookIdToCountOfAvailableBooksMap() {
        HashMap<UUID2<Book>, Long> availableBookIdToNumBooksAvailableMap = new HashMap<>();

        Set<UUID2<Book>> bookSet = this.bookIdToNumBooksAvailableMap.keySet();

        for (UUID2<Book> bookId : bookSet) {
            if (isKnownBookId(bookId)) {
                Long numBooksAvail = this.bookIdToNumBooksAvailableMap.get(bookId);
                availableBookIdToNumBooksAvailableMap.put(bookId, numBooksAvail);
            }
        }

        return new Result.Success<>(availableBookIdToNumBooksAvailableMap);
    }

    public Set<UUID2<Book>> findAllKnownBookIds() {
        return this.bookIdToNumBooksAvailableMap.keySet();
    }

    /////////////////////////////////
    // Published Helper Methods    //
    /////////////////////////////////

    public Result<UUID2<User>> registerUser(@NotNull UUID2<User> userId) {
        return upsertUserId(userId);
    }

    public boolean isKnownBook(@NotNull Book book) {
        return isKnownBookId(book.id());
    }
    public boolean isKnownBookId(@NotNull UUID2<Book> bookId) {
        return bookIdToNumBooksAvailableMap.containsKey(bookId);
    }

    public boolean isKnownUser(@NotNull User user) {
        return isKnownUserId(user.id());
    }
    public boolean isKnownUserId(UUID2<User> userId) {
        return registeredUserIdToCheckedOutBookIdMap.containsKey(userId);
    }

    public boolean isBookAvailableToCheckout(@NotNull Book book) {
        return isBookIdAvailableToCheckout(book.id());
    }
    public boolean isBookIdAvailableToCheckout(@NotNull UUID2<Book> bookId) {
        return bookIdToNumBooksAvailableMap.get(bookId) > 0;
    }

    public boolean isBookCheckedOutByUser(@NotNull Book book, @NotNull User user) {
        return isBookIdCheckedOutByUserId(book.id(), user.id());
    }
    public boolean isBookIdCheckedOutByUserId(@NotNull UUID2<Book> bookId, @NotNull UUID2<User> userId) {
        return registeredUserIdToCheckedOutBookIdMap.get(userId).contains(bookId);
    }

    public boolean isBookCheckedOutByAnyUser(@NotNull Book book) {
        return isBookIdCheckedOutByAnyUser(book.id());
    }
    public boolean isBookIdCheckedOutByAnyUser(UUID2<Book> bookId) {
        return registeredUserIdToCheckedOutBookIdMap.values()
                .stream()
                .anyMatch(bookIds -> bookIds.contains(bookId));
    }

    public Result<UUID2<User>> findUserIdOfCheckedOutBookId(@NotNull UUID2<Book> bookId) {
        if (!isBookIdCheckedOutByAnyUser(bookId))
            return new Result.Failure<>(new IllegalArgumentException("Book is not checked out by any User, bookId: " + bookId));

        for (UUID2<User> userId : registeredUserIdToCheckedOutBookIdMap.keySet()) {
            if (isBookIdCheckedOutByUserId(bookId, userId))
                return new Result.Success<>(userId);
        }

        return new Result.Failure<>(new IllegalArgumentException("Book is not checked out by any User, bookId: " + bookId));
    }
    public Result<UUID2<User>> findUserIdOfCheckedOutBook(@NotNull Book book) {
        return findUserIdOfCheckedOutBookId(book.id());
    }

    // Convenience method - Called from PrivateLibrary class ONLY
    public Result<UUID2<Book>> addPrivateBookIdToInventory(@NotNull UUID2<Book> bookId, int quantity) {
        return addBookIdToInventory(bookId, quantity);
    }

    protected Result<UUID2<Book>> removeTransferringBookFromInventory(@NotNull Book transferringBook) {
        return removeBookIdFromInventory(transferringBook.id(), 1);
    }

    protected Result<UUID2<Book>> addTransferringBookToInventory(@NotNull Book transferringBook) {
        return addBookIdToInventory(transferringBook.id(), 1);
    }

    /////////////////////////////////////////
    // Published Testing Helper Methods    //
    /////////////////////////////////////////

    // Intention revealing method
    public Result<UUID2<Book>> addTestBook(@NotNull UUID2<Book> bookId, int quantity) {
        return addBookIdToInventory(bookId, quantity);
    }

    protected Result<UUID2<User>> upsertTestUser(@NotNull UUID2<User> userId) {
        return upsertUserId(userId);
    }

    //////////////////////////////
    // Private Helper Functions //
    //////////////////////////////

    private Result<Void> _checkInBookIdFromUserId(@NotNull UUID2<Book> bookId, @NotNull UUID2<User> userId) {
        try {
            addBookIdToInventory(bookId, 1);
            return new Result.Success<>(null);
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }
    }

    private Result<Void> _checkOutBookIdToUserId(@NotNull UUID2<Book> bookId, @NotNull UUID2<User> userId) {
        if(!isBookIdAvailableToCheckout(bookId))
            return new Result.Failure<>(new IllegalArgumentException("Book is not in inventory, bookId: " + bookId));

        try {
            removeBookIdFromInventory(bookId, 1);
            return new Result.Success<>(null);
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }
    }

    private Result<UUID2<Book>> addBookIdToInventory(@NotNull UUID2<Book> bookId, int quantity) {
        if (quantity <= 0) return new Result.Failure<>(new IllegalArgumentException("quantity must be > 0, quantity: " + quantity));

        try {
            if (bookIdToNumBooksAvailableMap.containsKey(bookId)) {
                bookIdToNumBooksAvailableMap.put(
                    bookId,
                    bookIdToNumBooksAvailableMap.get(bookId) + 1
                );
            } else {
                bookIdToNumBooksAvailableMap.put(bookId, 1L);
            }
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }

        return new Result.Success<>(bookId);
    }
    private Result<Book> addBookToInventory(@NotNull Book book, int quantity) {
        Result<UUID2<Book>> addedUUID2Book = addBookIdToInventory(book.id(), quantity);

        if (addedUUID2Book instanceof Result.Failure) {
            return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) addedUUID2Book).exception().getMessage()));
        }

        return new Result.Success<>(book);
    }

    private Result<UUID2<Book>> removeBookIdFromInventory(@NotNull UUID2<Book> bookId, int quantity) {
        if (quantity <= 0) return new Result.Failure<>(new IllegalArgumentException("quantity must be > 0"));

        // Simulate network/database call
        try {
            if (bookIdToNumBooksAvailableMap.containsKey(bookId)) {
                bookIdToNumBooksAvailableMap.put(bookId, bookIdToNumBooksAvailableMap.get(bookId) - 1);
            } else {
                return new Result.Failure<>(new Exception("Book not in inventory, id: " + bookId));
            }
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }

        return new Result.Success<>(bookId);
    }
    private Result<Book> removeBookFromInventory(@NotNull Book book, int quantity) {
        Result<UUID2<Book>> removedUUID2Book = removeBookIdFromInventory(book.id(), quantity);

        if (removedUUID2Book instanceof Result.Failure) {
            return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) removedUUID2Book).exception().getMessage()));
        }

        return new Result.Success<>(book);
    }

    private Result<UUID2<Book>> addBookIdToRegisteredUser(@NotNull UUID2<Book> bookId, @NotNull UUID2<User> userId) {
        if (!isKnownBookId(bookId))
            return new Result.Failure<>(new IllegalArgumentException("bookId is not known, id: " + bookId));
        if (!isKnownUserId(userId))
            return new Result.Failure<>(new IllegalArgumentException("userId is not known, id: " + userId));
        if (isBookIdCheckedOutByUserId(bookId, userId))
            return new Result.Failure<>(new IllegalArgumentException("book is already checked out by user, bookId: " + bookId + ", userId: " + userId));

        try {
            if (registeredUserIdToCheckedOutBookIdMap.containsKey(userId)) {
                registeredUserIdToCheckedOutBookIdMap.get(userId).add(bookId);
            } else {
                //noinspection ArraysAsListWithZeroOrOneArgument
                registeredUserIdToCheckedOutBookIdMap.put(userId, new ArrayList<>(Arrays.asList(bookId)));
            }
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }

        return new Result.Success<>(bookId);
    }
    private Result<Book> addBookToUser(@NotNull Book book, @NotNull User user) {
        Result<UUID2<Book>> addedUUID2Book = addBookIdToRegisteredUser(book.id(), user.id());

        if (addedUUID2Book instanceof Result.Failure) {
            return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) addedUUID2Book).exception().getMessage()));
        }

        return new Result.Success<>(book);
    }

    private Result<UUID2<Book>> removeBookIdFromRegisteredUserId(UUID2<Book> bookId, UUID2<User> userId) {
        if (!isKnownBookId(bookId))
            return new Result.Failure<>(new IllegalArgumentException("bookId is not known, bookId: " + bookId));
        if (!isKnownUserId(userId))
            return new Result.Failure<>(new IllegalArgumentException("userId is not known, userId: " + userId));
        if (!isBookIdCheckedOutByUserId(bookId, userId))
            return new Result.Failure<>(new IllegalArgumentException("Book is not checked out by User, bookId: " + bookId + ", userId: " + userId));

        try {
            registeredUserIdToCheckedOutBookIdMap
                .get(userId)
                .remove(bookId); //todo reduce count instead of remove? Can someone check out multiple copies of the same book?
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }

        return new Result.Success<>(bookId);
    }
    private Result<Book> removeBookFromUser(@NotNull Book book, @NotNull User user) {
        Result<UUID2<Book>> removedUUID2Book = removeBookIdFromRegisteredUserId(book.id(), user.id());

        if (removedUUID2Book instanceof Result.Failure) {
            return new Result.Failure<>(new Exception(((Result.Failure<UUID2<Book>>) removedUUID2Book).exception().getMessage()));
        }

        return new Result.Success<>(book);
    }

    private Result<UUID2<User>> insertUserId(@NotNull UUID2<User> userId) {
        if (isKnownUserId(userId))
            return new Result.Failure<>(new IllegalArgumentException("userId is already known"));

        try {
            registeredUserIdToCheckedOutBookIdMap.put(userId, new ArrayList<>());
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }

        return new Result.Success<>(userId);
    }
    private @NotNull Result<UUID2<User>> upsertUserId(UUID2<User> userId) {
        if (isKnownUserId(userId)) return new Result.Success<>(userId);

        return insertUserId(userId);
    }

    private Result<UUID2<User>> removeUserId(@NotNull UUID2<User> userId) {
        if (!isKnownUserId(userId))
            return new Result.Failure<>(new IllegalArgumentException("userId is not known, userId: " + userId));

        try {
            registeredUserIdToCheckedOutBookIdMap.remove(userId);
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }

        return new Result.Success<>(userId);
    }

    ///////////////////////////
    // ToInfo implementation //
    ///////////////////////////

    // note: currently no DB or API for UserInfo (so no .ToInfoEntity() or .ToInfoDTO())
    @Override
    public LibraryInfo toDeepCopyDomainInfo() {
        // Note: *MUST* return a deep copy

        LibraryInfo libraryInfoDeepCopy = new LibraryInfo(this.id(), this.name);

        // Deep copy the bookIdToNumBooksAvailableMap
        libraryInfoDeepCopy.bookIdToNumBooksAvailableMap.putAll(this.bookIdToNumBooksAvailableMap);

        // Deep copy the userIdToCheckedOutBookMap
        for (Map.Entry<UUID2<User>, ArrayList<UUID2<Book>>> entry : this.registeredUserIdToCheckedOutBookIdMap.entrySet()) {
            libraryInfoDeepCopy.registeredUserIdToCheckedOutBookIdMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        return libraryInfoDeepCopy;
    }

}
