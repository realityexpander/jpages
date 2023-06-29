package org.elegantobjects.jpages.App2.domain.library;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.Context;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.user.User;
import org.jetbrains.annotations.NotNull;

// Defines a Private Library for Books which are not part of any Library (ie: Personal Books)
// Used as an alternate to "NoLibrary".
// A PrivateLibrary is not a real library, so it doesnt checkin/checkout books to users.
// It is used to represent a Book which is not part of any Library.
// A PrivateLibrary usually has only a single Book in it.
public class PrivateLibrary extends Library implements IUUID2 {

    // Note - This is a marker to indicate that the Book is not part of any Library.

    public PrivateLibrary(
        @NotNull LibraryInfo info,
        Context context
    ) {
        super(info, context);
        this.id._setUUID2TypeStr(UUID2.getUUID2TypeStr(PrivateLibrary.class));
    }
    public PrivateLibrary(
        UUID2<Library> id,
        Context context
    ) {
        super(id, context);  // todo could use Book id here (on calling side)
        this.id._setUUID2TypeStr(UUID2.getUUID2TypeStr(PrivateLibrary.class));
    }
    public PrivateLibrary(
        Context context
    ) {
        this(UUID2.randomUUID2(Library.class), context);
        this.id._setUUID2TypeStr(UUID2.getUUID2TypeStr(PrivateLibrary.class));
    }

    //////////////////////////////////////////////////
    // PrivateLibrary Domain Business Logic Methods //
    //////////////////////////////////////////////////

    // Private Library is not a real Library, so it cannot check out Books to Users.
    @Override
    public Result<Book> checkOutBookToUser(Book book, User user) {
        return new Result.Failure<>(new Exception("Private Library cannot check out Books to Users"));
    }

    // Private Library is not a real Library, so it cannot check in Books from Users.
    @Override
    public Result<Book> checkInBookFromUser(Book book, User user) {
        return new Result.Failure<>(new Exception("Private Library cannot check in Books from Users"));
    }

}
