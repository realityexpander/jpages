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

// Defines a Private Library for Books which are not part of any Library (ie: Personal Books)
// Used as an alternate to "NoLibrary".
// A PrivateLibrary is not a real library, so it doesnt checkin/checkout books to users.
// It is used to represent a Book which is not part of any Library.
// A PrivateLibrary usually has only a single Book in it.
public class PrivateLibrary extends Library implements IUUID2 {

    // This is a marker to indicate a Library is not part of any system Library.
    // ie: It is a Personal Library, a library for a single found book,
    // or a Library for a newly created book, etc.
    //
    // This not a system Library, so it doesn't access the Account Role Object for any account checks.
    // Any user can access this Library.
    // Users can have unlimited Private Libraries & Books in them.
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
    //     and it is sole duty is to hold ONLY 1 Book of one specific BookId, and no other BookIds.
    //   - It can only ever hold 1 Book at a time.
    //   - System Design Note: We could have subclassed PrivateLibrary into OrphanPrivateLibrary,
    //     but that would have added a deeper inheritance tree & complexity to the system for a simple edge use case.
    private final Boolean isForOnlyOneBook;

    public PrivateLibrary(
        @NotNull LibraryInfo info,
        Context context
    ) {
        super(info, context);
        this.id._setUUID2TypeStr(UUID2.calcUUID2TypeStr(PrivateLibrary.class));
        this.isForOnlyOneBook = false;
    }
    public PrivateLibrary(
        UUID2<Library> id,
        Context context
    ) {
        super(id, context);
        this.id._setUUID2TypeStr(UUID2.calcUUID2TypeStr(PrivateLibrary.class));
        this.isForOnlyOneBook = false;
    }
    @SuppressWarnings("unchecked")
    public PrivateLibrary(
        UUID2<Book> bookId,
        Boolean isForOnlyOneBook, // always true
        Context context
    ) {
        // Note: This creates an ORPHAN private library.
        super((UUID2<Library>) UUID2.fromUUID2(bookId, Library.class), context); // convert from BookId to LibraryId
        this.id._setUUID2TypeStr(UUID2.calcUUID2TypeStr(PrivateLibrary.class));

        // It is an ORPHAN bc it is NOT associated with any other system Library (private or not).
        // ORPHAN Private Library can contain only 1 Book,
        //   and it's BookId must match the initial BookId that created this Orphan Library.
        this.isForOnlyOneBook = true;
    }
    public PrivateLibrary(
        Context context
    ) {
        // Note: this creates an ORPHAN private library with a random id.
        this(UUID2.randomUUID2(Book.class), true, context);

        context.log.w(this, "PrivateLibrary (" + this.id + ") created with ORPHAN Library with Random Id.");
    }

    //////////////////////////////////////////////////
    // PrivateLibrary Domain Business Logic Methods //
    //////////////////////////////////////////////////

    @Override
    public Result<Book> checkInBookFromUser(@NotNull Book book, @NotNull User user) {
        context.log.d(this, format("Library (%s) - userId: %s, bookId: %s", this.id, book.id, user.id));

        if (!isForOnlyOneBook) {
            return super.info()
                    .checkInBookFromUser(book, user);  // note: we bypass all normal Library User Account checking
        }

        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Orphan Libraries can only check in 1 Book from Users.
        if (this.info.findAllKnownBookIds().size() != 0) return new Result.Failure<>(new Exception("Orphan Private Library can only check-in 1 Book from Users, bookId: " + book.id));

        // Only allow check in if the Book Id matches the initial Book Id that created this Orphan Library.
        Set<UUID2<Book>> bookIds = this.info.findAllKnownBookIds();
        @SuppressWarnings("unchecked")
        UUID2<Book> firstBookId = (UUID2<Book>) bookIds.toArray()[0]; // there should only be 1 bookId
        if (!firstBookId.equals(book.id)) {
            return new Result.Failure<>(new Exception("Orphan Private Library can only check-in 1 Book from Users and must be the same Id as the initial Book placed in the PrivateLibrary, bookId: " + book.id));
        }

        return super.info()
                .checkInBookFromUser(book, user); // note: we bypass all normal Library User Account checking
    }

    @Override
    public Result<Book> checkOutBookToUser(@NotNull Book book, @NotNull User user) {
        context.log.d(this, format("Library (%s) - userId: %s, bookId: %s", this.id, book.id, user.id));

        if (!isForOnlyOneBook) {
            return super.info()
                    .checkInBookFromUser(book, user);  // note: we bypass all normal Library User Account checking
        }

        if (fetchInfoFailureReason() != null)
            return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Orphan Libraries can only check out 1 Book to Users.
        if (this.info.findAllKnownBookIds().size() != 1)
            return new Result.Failure<>(new Exception("Orphan Private Library can only check-out 1 Book to Users, bookId: " + book.id));

        // Only allow check out if the Book Id matches the initial Book Id that created this Orphan Library.
        Set<UUID2<Book>> bookIds = this.info.findAllKnownBookIds();
        @SuppressWarnings("unchecked")
        UUID2<Book> firstBookId = (UUID2<Book>) bookIds.toArray()[0];  // there should only be 1 bookId
        if (!firstBookId.equals(book.id))
            return new Result.Failure<>(new Exception("Orphan Private Library can only check-out 1 Book to Users and must be the same Id as the initial Book placed in the PrivateLibrary, bookId: " + book.id));

        return super.info()
                .checkOutBookToUser(book, user); // note: we bypass all normal Library User Account checking
    }
}
