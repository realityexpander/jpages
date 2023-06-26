package org.elegantobjects.jpages.App2.domain;

import org.elegantobjects.jpages.App2.core.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.core.Result;
import org.elegantobjects.jpages.App2.core.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.core.IRole;
import org.jetbrains.annotations.NotNull;

// Book Domain Object - Only interacts with its own repo, Context, and other Domain Objects
public class Book extends IRole<Domain.BookInfo> implements IUUID2 {
    public final UUID2<Book> id;
    private final Repo.BookInfo repo;

    public Book(
        @NotNull Domain.BookInfo info,
        Context context
    ) {
        super(info, context);
        this.repo = this.context.bookRepo();
        this.id = info.id();
        this.id._setUUID2TypeStr(this.getUUID2TypeStr());

        context.log.d(this, "Book (" + this.id + ") created from Info");
    }
    public Book(
        String json,
        Class<Domain.BookInfo> clazz,
        Context context
    ) {
        super(json, clazz, context);
        this.repo = this.context.bookRepo();
        this.id = this.info.id();
        this.id._setUUID2TypeStr(this.getUUID2TypeStr());

        context.log.d(this, "Book (" + this.id + ") created from JSON using class:" + clazz.getName());
    }
    public Book(
        @NotNull UUID2<Book> id,
        Context context
    ) {
        super(id.toDomainUUID2(), context);
        this.repo = this.context.bookRepo();
        this.id = id;
        this.id._setUUID2TypeStr(this.getUUID2TypeStr());

        context.log.d(this, "Book (" + this.id + ") created using id with no Info");
    }
    public Book(String json, Context context) {
        this(json, Domain.BookInfo.class, context);
    }
    public Book(Context context) {
        this(new Domain.BookInfo(UUID2.randomUUID2().uuid()), context);
    }
    // LEAVE for reference, for static Context instance implementation
    // Book(UUID2<Book id) {
    //     this(id, null);
    // }

    /////////////////////////////////////
    // IRole/UUID2 Required Overrides  //
    /////////////////////////////////////

    @Override
    public Result<Domain.BookInfo> fetchInfoResult() {
        // context.log.d(this,"Book (" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging

        infoResult = this.repo.fetchBookInfo(this.id);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<Domain.BookInfo>) infoResult).value();

        return infoResult;
    }

    @Override
    public Result<Domain.BookInfo> updateInfo(Domain.BookInfo updatedInfo) {
        // Update self optimistically
        super.updateInfo(updatedInfo);

        // Update the repo
        Result<Domain.BookInfo> infoResult = this.repo.updateBookInfo(updatedInfo);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        // Update self with repo result
        this.info = ((Result.Success<Domain.BookInfo>) infoResult).value();
        return infoResult;
    }

    @Override
    public String getUUID2TypeStr() {
        return this.getClass().getName();
    }

    ////////////////////////////////////////
    // Book Domain Business Logic Methods //
    // - Methods to modify it's BookInfo  //
    ////////////////////////////////////////

    public Result<Domain.BookInfo> updateAuthor(String authorName) {
        Domain.BookInfo updatedInfo = this.info.withAuthor(authorName);
        return this.updateInfo(updatedInfo);
    }

    public Result<Domain.BookInfo> updateTitle(String title) {
        Domain.BookInfo updatedInfo = this.info.withTitle(title);
        return this.updateInfo(updatedInfo);
    }

    public Result<Domain.BookInfo> updateDescription(String description) {
        Domain.BookInfo updatedInfo = this.info.withDescription(description);
        return this.updateInfo(updatedInfo);
    }
}