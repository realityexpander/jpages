package org.elegantobjects.jpages.App2.domain.library;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.Context;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static java.lang.String.format;

/**

Private Library

<ul>
 <li>A {@code PrivateLibrary} is a {@code Library} for {@code Book}s which are not part of
     any system {@code Library}.</li>
 <li>Used as an alternate to using {@code null} or "NoLibrary".</li>
 <li>A PrivateLibrary is identical to a regular library, except it doesn't verify any Account info and
     any {@code User} can {@code checkIn} and {@code checkOut} {@code Books}.</li>
 <br>
 <li><i>Note: A special case for {@code PrivateLibrary} is an Orphan {@code PrivateLibrary} which only allows
     a single Book of a specific id to be checked into/out of it.</i></li>
</ul>
**/
public class PrivateLibrary extends Library implements IUUID2 {

    // This a Private Library is not part of any system Library.
    // ie: It is a "Personal" Library, a library for a single-found book, or a Library for a newly created book, etc.
    //
    //  This is not a system Library, so it doesn't access the Account Role Object for any account checks.
    // - Any user can `checkIn` and `checkOut` Books from this Library.
    // - Users can have unlimited Private Libraries & unlimited number of Books in them.
    //
    // This is a system design alternative to:
    //   - Using `null` to represent a Book which is not part of any Library.
    //   - or naming it "NoLibrary" or "PersonalLibrary" or "UnassignedLibrary"
    //   - Instead of using "null" we create an object that conveys the intention behind what "null" means in this context.
    //     ie: what is the concept of a "null" Library? Maybe it is a Library which is not part of any system Library?
    //     How about a "Library" which is not part of any system Library is called a "PrivateLibrary"?

    // ORPHAN Private Library:
    //   - ORPHAN definition: An orphan is a child that has no parent.
    //     - For a Book, it would have no "source" Public Library.
    //   - If a Private Library is created from a BookId, it is called an ORPHAN Private Library
    //     and its sole duty is to hold ONLY 1 Book of one specific BookId, and never any other BookIds.
    //   - It can only ever hold 1 Book at a time.
    //   - ORPHAN PrivateLibraries have the `isForOnlyOneBook` flag set to true.
    //   - App Design Note: We could have subclassed PrivateLibrary into OrphanPrivateLibrary,
    //     but that would have added a deeper inheritance tree & complexity to the system for a simple edge use case.
    private final Boolean isForOnlyOneBook;  // true = ORPHAN Private Library, false = normal Private Library
                                             // Note: the naming here conveys the intent of the variable,
                                             //       even if the reader doesn't know about "Orphan" libraries.

    public
    PrivateLibrary(
        @NotNull LibraryInfo info,
        @NotNull Context context
    ) {
        super(info, context);
        this.id()._setUUID2TypeStr(UUID2.calcUUID2TypeStr(PrivateLibrary.class));
        this.isForOnlyOneBook = false;
    }
    public
    PrivateLibrary(
        @NotNull UUID2<Library> id,
        @NotNull Context context
    ) {
        super(id, context);
        this.id()._setUUID2TypeStr(UUID2.calcUUID2TypeStr(PrivateLibrary.class));
        this.isForOnlyOneBook = false;
    }
    public
    PrivateLibrary(
            @NotNull UUID2<Book> bookId,
            @SuppressWarnings("unused")
            boolean isForOnlyOneBook, // note: always true for this constructor
            @NotNull Context context
    ) {
        // Note: This creates an ORPHAN private library.
        // It is an ORPHAN bc it is NOT associated with any other system Library (private or not).

        super(new UUID2<Library>(bookId), context); // make the LibraryId match the BookId
        this.id()._setUUID2TypeStr(UUID2.calcUUID2TypeStr(PrivateLibrary.class));

        // It is an ORPHAN bc it is NOT associated with any other system Library (private or not).
        // ORPHAN Private Library can:
        //  - Contain only 1 Book,
        //  - And the 1 BookId must always match initial BookId that created this Orphan Library.
        this.isForOnlyOneBook = true;
    }
    public PrivateLibrary(
        @NotNull Context context
    ) {
        // Note: this creates an ORPHAN private library with a random id.
        this(UUID2.randomUUID2(Book.class), true, context);

        context.log.w(this, "PrivateLibrary (" + this.id() + ") created with ORPHAN Library with Random Id.");
    }

    //////////////////////////////////////////////////
    // PrivateLibrary Domain Business Logic Methods //
    //////////////////////////////////////////////////

    @Override
    public Result<Book> checkOutBookToUser(@NotNull Book book, @NotNull User user) {
        context.log.d(this, format("Library (%s) - userId: %s, bookId: %s", this.id(), book.id(), user.id()));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Automatically upsert the User into the Library's User Register
        // - Private libraries are open to all users, so we don't need to check if the user is registered.
        Result<UUID2<User>> addRegisteredUserResult = this.info.registerUser(user.id());
        if (addRegisteredUserResult instanceof Result.Failure)
            return new Result.Failure<>(new Exception("Failed to register User in Library, userId: " + user.id()));

        if (!isForOnlyOneBook) {
            // note: PrivateLibraries bypass all normal Library User Account checks
            return super.info().checkOutPrivateLibraryBookToUser(book, user);
        }

        if (fetchInfoFailureReason() != null)
            return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Orphan Libraries can only check out 1 Book to 1 User.
        if (this.info.findAllKnownBookIds().size() != 1)
            return new Result.Failure<>(new Exception("Orphan Private Library can only check-out 1 Book to Users, bookId: " + book.id()));

        // Only allow check out if the Book Id matches the initial Book Id that created this Orphan Library.
        Set<UUID2<Book>> bookIds = this.info.findAllKnownBookIds();
        @SuppressWarnings("unchecked")
        UUID2<Book> firstBookId = (UUID2<Book>) bookIds.toArray()[0];  // there should only be 1 bookId
        if (!firstBookId.equals(book.id()))
            return new Result.Failure<>(new Exception("Orphan Private Library can only check-out 1 Book to Users and must be the same Id as the initial Book placed in the PrivateLibrary, bookId: " + book.id()));

        Result<Book> checkOutResult = super.info().checkOutPrivateLibraryBookToUser(book, user); // note: we bypass all normal Library User Account checking
        if (checkOutResult instanceof Result.Failure)
            return new Result.Failure<>(new Exception("Failed to check-out Book from Private Library, bookId: " + book.id()));

        // Update the Info
        Result<LibraryInfo> updateInfoResult = this.updateInfo(this.info);
        if (updateInfoResult instanceof Result.Failure) return new Result.Failure<>(((Result.Failure<LibraryInfo>) updateInfoResult).exception());

        return checkOutResult;
    }

    @Override
    public Result<Book> checkInBookFromUser(@NotNull Book book, @NotNull User user) {
        context.log.d(this, format("Library (%s) - userId: %s, bookId: %s", this.id(), book.id(), user.id()));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        if (!isForOnlyOneBook) {
            // note: we bypass all normal Library User Account checking
            return super.info().checkInPrivateLibraryBookFromUser(book, user);
        }

        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Orphan Libraries can only check in 1 Book from Users.
        if (this.info.findAllKnownBookIds().size() != 0) return new Result.Failure<>(new Exception("Orphan Private Library can only check-in 1 Book from Users, bookId: " + book.id()));

        // Only allow checkIn if the BookId matches the initial BookId that created this Orphan PrivateLibrary.
        Set<UUID2<Book>> bookIds = this.info.findAllKnownBookIds();
        @SuppressWarnings("unchecked")
        UUID2<Book> firstBookId = (UUID2<Book>) bookIds.toArray()[0]; // there should only be 1 BookId
        if (!firstBookId.equals(book.id())) {
            return new Result.Failure<>(new Exception("Orphan Private Library can only check-in 1 Book from Users and must be the same Id as the initial Book placed in the PrivateLibrary, bookId: " + book.id()));
        }

        // note: we bypass all normal Library User Account checking
        Result<Book> checkInResult = super.info.checkInPrivateLibraryBookFromUser(book, user);
        if (checkInResult instanceof Result.Failure) return new Result.Failure<>(((Result.Failure<Book>) checkInResult).exception());

        // Update the Info
        Result<LibraryInfo> updateInfoResult = this.updateInfo(this.info);
        if (updateInfoResult instanceof Result.Failure) return new Result.Failure<>(((Result.Failure<LibraryInfo>) updateInfoResult).exception());

        return checkInResult;
    }
}