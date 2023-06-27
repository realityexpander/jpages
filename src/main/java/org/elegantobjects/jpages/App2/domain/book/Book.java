package org.elegantobjects.jpages.App2.domain.book;

import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.Context;
import org.elegantobjects.jpages.App2.domain.common.IRole;
import org.jetbrains.annotations.NotNull;

// Book Domain Object - Only interacts with its own repo, Context, and other Domain Objects
public class Book extends IRole<DomainBookInfo> implements IUUID2 {
    public final UUID2<Book> id;
    private final BookInfoRepo repo;

    public Book(
        @NotNull DomainBookInfo info,
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
        Class<DomainBookInfo> clazz,
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
        this(json, DomainBookInfo.class, context);
    }
    public Book(Context context) {
        this(new DomainBookInfo(UUID2.randomUUID2().uuid()), context);
    }
    // LEAVE for reference, for static Context instance implementation
    // Book(UUID2<Book id) {
    //     this(id, null);
    // }

    /////////////////////////////////////
    // IRole/UUID2 Required Overrides  //
    /////////////////////////////////////

    @Override
    public Result<DomainBookInfo> fetchInfoResult() {
        // context.log.d(this,"Book (" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging

        infoResult = this.repo.fetchBookInfo(this.id);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<DomainBookInfo>) infoResult).value();

        return infoResult;
    }

    @Override
    public Result<DomainBookInfo> updateInfo(DomainBookInfo updatedInfo) {
        // Update self optimistically
        super.updateInfo(updatedInfo);

        // Update the repo
        Result<DomainBookInfo> infoResult = this.repo.updateBookInfo(updatedInfo);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        // Update self with repo result
        this.info = ((Result.Success<DomainBookInfo>) infoResult).value();
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

    public Result<DomainBookInfo> updateAuthor(String author) {
        DomainBookInfo updatedInfo = this.info.withAuthor(author);
        return this.updateInfo(updatedInfo);
    }

    public Result<DomainBookInfo> updateTitle(String title) {
        DomainBookInfo updatedInfo = this.info.withTitle(title);
        return this.updateInfo(updatedInfo);
    }

    public Result<DomainBookInfo> updateDescription(String description) {
        DomainBookInfo updatedInfo = this.info.withDescription(description);
        return this.updateInfo(updatedInfo);
    }
}