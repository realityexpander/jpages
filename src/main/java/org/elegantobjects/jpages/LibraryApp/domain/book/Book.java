package org.elegantobjects.jpages.LibraryApp.domain.book;

import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.LibraryApp.common.util.Result;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.domain.book.data.BookInfo;
import org.elegantobjects.jpages.LibraryApp.domain.book.data.BookInfoRepo;
import org.elegantobjects.jpages.LibraryApp.domain.book.data.local.EntityBookInfo;
import org.elegantobjects.jpages.LibraryApp.domain.book.data.network.DTOBookInfo;
import org.elegantobjects.jpages.LibraryApp.domain.Context;
import org.elegantobjects.jpages.LibraryApp.domain.common.Role;
import org.elegantobjects.jpages.LibraryApp.domain.library.Library;
import org.elegantobjects.jpages.LibraryApp.domain.library.data.LibraryInfo;
import org.elegantobjects.jpages.LibraryApp.domain.library.PrivateLibrary;
import org.elegantobjects.jpages.LibraryApp.domain.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Book Role Object - Only interacts with its own repository, Context, and other Role Objects<br>
 * <br>
 * Note: Use of <b>@Nullable</b> for <b>sourceLibrary</b> indicates to <i>"use default value"</i><br>
 * <br>
 * Look at this.pickSourceLibrary() for more information.
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

public class Book extends Role<BookInfo> implements IUUID2 {
    private final BookInfoRepo repo;
    private final Library sourceLibrary; // Book's source Library Role Object - owns this Book.

    public
    Book(
        @NotNull BookInfo info,
        @Nullable  Library sourceLibrary,
        @NotNull Context context
    ) {
        super(info, context);
        this.repo = this.context.bookInfoRepo();
        this.sourceLibrary = pickSourceLibrary(sourceLibrary, this.id(), context);

        context.log.d(this, "Book (" + this.id() + ") created from Info");
    }
    public
    Book(
        @NotNull String json,
        @NotNull Class<BookInfo> clazz,
        @Nullable Library sourceLibrary,
        @NotNull Context context
    ) {
        super(json, clazz, context);
        this.repo = this.context.bookInfoRepo();
        this.sourceLibrary = pickSourceLibrary(sourceLibrary, this.id(), context);

        context.log.d(this, "Book (" + this.id() + ") created from JSON using class:" + clazz.getName());
    }
    public
    Book(
        @NotNull UUID2<Book> id,
        @Nullable Library sourceLibrary,
        @NotNull Context context
    ) {
        super(id, context);
        this.repo = this.context.bookInfoRepo();
        this.sourceLibrary = pickSourceLibrary(sourceLibrary, id, context);

        context.log.d(this, "Book (" + this.id() + ") created using id with no Info");
    }
    public
    Book(@NotNull String json, @Nullable Library sourceLibrary, @NotNull Context context) {
        this(json, BookInfo.class, sourceLibrary, context);
    }
    public
    Book(@NotNull String json, @NotNull Context context) {
        this(json, BookInfo.class, null, context);
    }
    public
    Book(@NotNull Context context) {
        this(new BookInfo(UUID2.randomUUID2(Book.class)), null, context);
    }

    /////////////////////////////////////////
    // Entity 🡒 Domain 🡐 DTO        //
    // - Converters to keep DB/API layer   //
    //   separate from Domain layer        //
    /////////////////////////////////////////

    public
    Book(@NotNull DTOBookInfo infoDTO, @Nullable Library sourceLibrary, @NotNull Context context) {
        // todo Validate DTO
        // - like validate that the UUID2 of the DTO is in the system, etc.

        this(new BookInfo(infoDTO), sourceLibrary, context);
    }
    public
    Book(@NotNull EntityBookInfo infoEntity, @NotNull Library sourceLibrary, @NotNull Context context) {
        // todo Validate Entity
        // - like validate that the UUID2 of the Entity is in the system, etc.

        this(new BookInfo(infoEntity), sourceLibrary, context);
    }

    /////////////////////////
    // Static constructors //
    /////////////////////////

    public static Result<Book> fetchBook(
        @NotNull UUID2<Book> uuid2,
        @Nullable Library sourceLibrary,   // `null` means use default (ie: Orphan PrivateLibrary)
        @NotNull Context context
    ) {
        BookInfoRepo repo = context.bookInfoRepo();

        Result<BookInfo> infoResult = repo.fetchBookInfo(uuid2);
        if (infoResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<BookInfo>) infoResult).exception());
        }

        BookInfo info = ((Result.Success<BookInfo>) infoResult).value();
        return new Result.Success<>(new Book(info, sourceLibrary, context));
    }
    public static Result<Book> fetchBook(
        @NotNull UUID2<Book> uuid2,
        @NotNull Context context
    ) {
        return fetchBook(uuid2, null, context);
    }

    ////////////////////////
    // Published Getters  //
    ////////////////////////

    // Convenience method to get the Type-safe id from the Class
    @Override @SuppressWarnings("unchecked")
    public UUID2<Book> id() {
        return (UUID2<Book>) super.id();
    }

    public Library sourceLibrary() {
        return sourceLibrary;
    }

    /////////////////////////////////////
    // IRole/UUID2 Required Overrides  //
    /////////////////////////////////////

    @Override
    public Result<BookInfo> fetchInfoResult() {
        // context.log.d(this,"Book (" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging

        infoResult = this.repo.fetchBookInfo(this.id());
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<BookInfo>) infoResult).value();

        return infoResult;
    }

    @Override
    public Result<BookInfo> updateInfo(@NotNull BookInfo updatedInfo) {
        // Update self optimistically
        super.updateInfo(updatedInfo);

        // Update the repo
        Result<BookInfo> infoResult = this.repo.updateBookInfo(updatedInfo);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        // Update self with the Repo Result
        this.info = ((Result.Success<BookInfo>) infoResult).value();
        return infoResult;
    }

    @Override
    public String uuid2TypeStr() {
        return UUID2.calcUUID2TypeStr(this.getClass());
    }

    ////////////////////////////////////////
    // Book Role Business Logic Methods //
    // - Methods to modify it's BookInfo  //
    ////////////////////////////////////////

    public boolean isBookFromPrivateLibrary() {
        return this.sourceLibrary instanceof PrivateLibrary;
    }
    public boolean isBookFromPublicLibrary() {
        return !this.isBookFromPrivateLibrary();
    }

    public Result<Book> transferToLibrary(@NotNull Library library) {
        if(this.sourceLibrary == library) {
            return new Result.Success<>(this);
        }

        if(this.isBookFromPrivateLibrary()) {
            return library.transferBookSourceLibraryToThisLibrary(this);
        }

        if(sourceLibrary.isBookCheckedOutByAnyUser(this)) {
            Result<User> userResult = sourceLibrary.getUserOfCheckedOutBook(this);
            if(userResult instanceof Result.Failure)
                return new Result.Failure<>(((Result.Failure<User>) userResult).exception());

            User user = ((Result.Success<User>) userResult).value();
            return library.transferCheckedOutBookSourceLibraryToThisLibrary(this, user);
        }

        return library.transferBookSourceLibraryToThisLibrary(this);
    }

    public Result<BookInfo> updateAuthor(@NotNull String author) {
        BookInfo updatedInfo = this.info().withAuthor(author);
        return this.updateInfo(updatedInfo); // delegate to Info Object
    }
    public Result<BookInfo> updateTitle(@NotNull String title) {
        BookInfo updatedInfo = this.info().withTitle(title);
        return this.updateInfo(updatedInfo); // delegate to Info Object
    }
    public Result<BookInfo> updateDescription(@NotNull String description) {
        BookInfo updatedInfo = this.info().withDescription(description);
        return this.updateInfo(updatedInfo); // delegate to Info Object
    }

    // Role Role-specific business logic in a Role Object.
    public Result<Book> updateSourceLibrary(@NotNull Library sourceLibrary) {
        // NOTE: This method is primarily used by the Library Role Object when moving a Book from one Library
        //   to another Library.
        // This info is *NOT* saved with the Book's BookInfo, as it only applies to the Book Role Object.
        // - Shows example of Role-specific business logic in a Role Object that is not saved in the Info Object.
        // - ie: sourceLibrary only exists in this Book Role Object as `BookInfo` does NOT have a `sourceLibrary` field.

        Book updatedBook = new Book(this.info(), sourceLibrary, this.context);
        return new Result.Success<>(updatedBook);
    }

    ////////////////////////////////////////
    // Private Helper Methods             //
    ////////////////////////////////////////

    private Library pickSourceLibrary(
        @Nullable Library sourceLibrary,
        @NotNull UUID2<Book> bookId,
        @NotNull Context context
    ) {
        // If a sourceLibrary was not provided, create a new ORPHAN PrivateLibrary for this Book.
        if(sourceLibrary == null) {
            // Create a new ORPHAN PrivateLibrary just for this one Book
            Library privateLibrary = new PrivateLibrary(bookId, true, context);
            context.libraryInfoRepo()
                .upsertLibraryInfo(
                    new LibraryInfo(
                        privateLibrary.id(),
                        "ORPHAN Private Library only for one Book, BookId: " + bookId.uuid()
                    )
                );

            // Add this Book to the new ORPHAN PrivateLibrary
            Result<UUID2<Book>> ignoreThisResult = privateLibrary.info()
                .addPrivateBookIdToInventory(bookId, 1);

            return privateLibrary;
        }

        return sourceLibrary;
    }
}