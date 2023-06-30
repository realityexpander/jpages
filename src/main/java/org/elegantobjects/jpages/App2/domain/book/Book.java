package org.elegantobjects.jpages.App2.domain.book;

import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.Context;
import org.elegantobjects.jpages.App2.domain.common.Role;
import org.elegantobjects.jpages.App2.domain.library.Library;
import org.elegantobjects.jpages.App2.domain.library.LibraryInfo;
import org.elegantobjects.jpages.App2.domain.library.PrivateLibrary;
import org.jetbrains.annotations.NotNull;

// Book Domain Object - Only interacts with its own repo, Context, and other Domain Objects
public class Book extends Role<BookInfo> implements IUUID2 {
    public final UUID2<Book> id;
    private final BookInfoRepo repo;
    private final Library sourceLibrary; // Book's source Library Domain Object - owns this Book.

    public Book(
        @NotNull BookInfo info,
        Library sourceLibrary,
        Context context
    ) {
        super(info, context);
        this.repo = this.context.bookInfoRepo();
        this.id = info.id();
        this.sourceLibrary = pickSourceLibrary(sourceLibrary, id, context);

        context.log.d(this, "Book (" + this.id + ") created from Info");
    }
    public Book(
        String json,
        Class<BookInfo> clazz,
        Library sourceLibrary,
        Context context
    ) {
        super(json, clazz, context);
        this.repo = this.context.bookInfoRepo();
        this.id = this.info.id();
        this.sourceLibrary = pickSourceLibrary(sourceLibrary, id, context);

        context.log.d(this, "Book (" + this.id + ") created from JSON using class:" + clazz.getName());
    }
    public Book(
        @NotNull UUID2<Book> id,
        Library sourceLibrary,
        Context context
    ) {
        super(id, context);
        this.repo = this.context.bookInfoRepo();
        this.id = id;
        this.sourceLibrary = pickSourceLibrary(sourceLibrary, id, context);

        context.log.d(this, "Book (" + this.id + ") created using id with no Info");
    }
    public Book(String json, Library sourceLibrary, Context context) {
        this(json, BookInfo.class, sourceLibrary, context);
    }
    public Book(String json, Context context) {
        this(json, BookInfo.class, null, context);
    }
    public Book(Context context) {
        this(new BookInfo(UUID2.randomUUID2(Book.class)), null, context);
    }

    ////////////////////////////////
    // Published Getters          //
    ////////////////////////////////

    public Library sourceLibrary() {
        return sourceLibrary;
    }

    /////////////////////////////////////
    // IRole/UUID2 Required Overrides  //
    /////////////////////////////////////

    @Override
    public Result<BookInfo> fetchInfoResult() {
        // context.log.d(this,"Book (" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging

        infoResult = this.repo.fetchBookInfo(this.id);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<BookInfo>) infoResult).value();

        return infoResult;
    }

    @Override
    public Result<BookInfo> updateInfo(BookInfo updatedInfo) {
        // Update self optimistically
        super.updateInfo(updatedInfo);

        // Update the repo
        Result<BookInfo> infoResult = this.repo.updateBookInfo(updatedInfo);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        // Update self with repo result
        this.info = ((Result.Success<BookInfo>) infoResult).value();
        return infoResult;
    }

    @Override
    public String getUUID2TypeStr() {
//        return this.getClass().getName();
//        return UUID2.getUUID2TypeStr(Book.class);
        return UUID2.getUUID2TypeStr(this.getClass()); // todo test does this work?
    }

    ////////////////////////////////////////
    // Book Domain Business Logic Methods //
    // - Methods to modify it's BookInfo  //
    ////////////////////////////////////////

    public boolean isBookFromPrivateLibrary() {
        return this.sourceLibrary instanceof PrivateLibrary;
    }
    public boolean isBookFromPublicLibrary() {
        return !this.isBookFromPrivateLibrary();
    }

    public Result<BookInfo> updateAuthor(String author) {
        BookInfo updatedInfo = this.info.withAuthor(author);
        return this.updateInfo(updatedInfo); // delegate to Info Object
    }
    public Result<BookInfo> updateTitle(String title) {
        BookInfo updatedInfo = this.info.withTitle(title);
        return this.updateInfo(updatedInfo); // delegate to Info Object
    }
    public Result<BookInfo> updateDescription(String description) {
        BookInfo updatedInfo = this.info.withDescription(description);
        return this.updateInfo(updatedInfo); // delegate to Info Object
    }

    // Domain Role-specific business logic in a Role Object.
    public Result<Book> updateSourceLibrary(Library sourceLibrary) {
        // NOTE: This method is only used by the Library Domain Object when moving a Book from one Library to another.
        // It is NOT saved with the BookInfo, as it only applies to the Book Role Object.
        // - shows example of Domain Role-specific business logic in a Role Object.
        // - ie: sourceLibrary only exists in the Domain. BookInfo does not have a sourceLibrary field.

        Book updatedBook = new Book(this.info, sourceLibrary, this.context);  // todo test
        return new Result.Success<>(updatedBook);
    }

    ////////////////////////////////////////
    // Private Helper Methods             //
    ////////////////////////////////////////

    private Library pickSourceLibrary(
        Library sourceLibrary,
        UUID2<Book> bookId,
        Context context
    ) {
        // If a sourceLibrary was not provided, create a new ORPHAN PrivateLibrary for this Book.
        if(sourceLibrary == null) {
            // Create a new ORPHAN PrivateLibrary just for this one Book
            Library privateLibrary = new PrivateLibrary(bookId, true, context);
            context.libraryInfoRepo()
                .upsertLibraryInfo(
                    new LibraryInfo(
                        privateLibrary.id,
                        "ORPHAN Private Library only for one Book, BookId: " + bookId.uuid()
                    )
                );

            // Add this Book to the new ORPHAN PrivateLibrary
            Result<UUID2<Book>> ignoreThisResult = privateLibrary.info()
                .addPrivateBookToInventory(bookId, 1);

            return privateLibrary;
        }

        return sourceLibrary;
    }
}