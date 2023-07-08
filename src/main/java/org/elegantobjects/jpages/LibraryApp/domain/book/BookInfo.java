package org.elegantobjects.jpages.LibraryApp.domain.book;

import org.elegantobjects.jpages.LibraryApp.common.util.HumanDate;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.LibraryApp.domain.common.DomainInfo;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.common.Model;
import org.elegantobjects.jpages.LibraryApp.data.book.local.EntityBookInfo;
import org.elegantobjects.jpages.LibraryApp.data.book.network.DTOBookInfo;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BookInfo extends DomainInfo
    implements
        Model.ToEntityInfo<EntityBookInfo>,
        Model.ToDTOInfo<DTOBookInfo>,
        Model.ToDomainInfo<BookInfo>
{
    public final String title;
    public final String author;
    public final String description;
    public long creationTimeMillis;
    public long lastModifiedTimeMillis;
    public boolean isDeleted;

    public
    BookInfo(
        @NotNull UUID2<Book> id,  // Note: This is a UUID2<Book> not a UUID2<BookInfo>
        @NotNull String title,
        @NotNull String author,
        @NotNull String description,
        long creationTimeMillis,
        long lastModifiedTimeMillis,
        boolean isDeleted
    ) {
        super(id);
        this.title = title;
        this.author = author;
        this.description = description;
        this.creationTimeMillis = creationTimeMillis;
        this.lastModifiedTimeMillis = lastModifiedTimeMillis;
        this.isDeleted = isDeleted;
    }
    public
    BookInfo(@NotNull UUID uuid, @NotNull String title, @NotNull String author, @NotNull String description, long creationTimeMillis, long lastModifiedTimeMillis, boolean isDeleted) {
        this(
            new UUID2<Book>(uuid, Book.class),
            title,
            author,
            description,
            creationTimeMillis,
            lastModifiedTimeMillis,
            isDeleted
        );
    }
    public
    BookInfo(@NotNull String id, @NotNull String title, @NotNull String author, @NotNull String description) {
        this(UUID.fromString(id), title, author, description, 0, 0, false);
    }
    public
    BookInfo(@NotNull BookInfo bookInfo) {
        // todo add validation

        this(bookInfo.id(), bookInfo.title, bookInfo.author, bookInfo.description, bookInfo.creationTimeMillis, bookInfo.lastModifiedTimeMillis, bookInfo.isDeleted);
    }
    public
    BookInfo(@NotNull UUID id) {
        this(id, "", "", "", 0, 0, false);
    }
    public <TDomainUUID2 extends IUUID2>
    BookInfo(@NotNull UUID2<TDomainUUID2> uuid2) {
        this(uuid2.uuid(), "", "", "", 0, 0, false);
    }

    // DomainInfo objects Must:
    // - Accept both `DTO.BookInfo` and `Entity.BookInfo`
    // - Convert to Domain.BookInfo
    public
    BookInfo(@NotNull DTOBookInfo dtoBookInfo) {
        // Converts from DTOInfo to DomainInfo
        this(
            new UUID2<Book>(dtoBookInfo.id().uuid(), Book.class), // change id to domain UUID2<Book> type
            dtoBookInfo.title,
            dtoBookInfo.author,
            dtoBookInfo.description,
            dtoBookInfo.creationTimeMillis,
            dtoBookInfo.lastModifiedTimeMillis,
            dtoBookInfo.isDeleted
        );

        // Basic Validation = Domain decides what to include from the DTO
        // - must be done after construction
        validateBookInfo();
    }

    public
    BookInfo(@NotNull EntityBookInfo entityBookInfo) {
        // Converts from EntityInfo to DomainInfo
        this(
            new UUID2<Book>(entityBookInfo.id()), // change to the Domain UUID2 type
            entityBookInfo.title,
            entityBookInfo.author,
            entityBookInfo.description,
            entityBookInfo.creationTimeMillis,
            entityBookInfo.lastModifiedTimeMillis,
            entityBookInfo.isDeleted
        );

        // Basic Validation - Domain decides what to include from the Entities
        // - must be done after contruction
        validateBookInfo();
    }

    private void validateBookInfo() {
        if(title.length() > 100)
            throw new IllegalArgumentException("BookInfo.title cannot be longer than 100 characters");
        if(author.length() > 100)
            throw new IllegalArgumentException("BookInfo.author cannot be longer than 100 characters");
        if(description.length() > 1000)
            throw new IllegalArgumentException("BookInfo.description cannot be longer than 1000 characters");

        // todo add enhanced validation here, or in the application layer
    }

    ////////////////////////////////
    // Published Getters          //
    ////////////////////////////////

    // Convenience method to get the Type-safe id from the Class
    @Override @SuppressWarnings("unchecked")
    public UUID2<Book> id() {
        return (UUID2<Book>) super.id();
    }

    @Override
    public String toString() {
        return "Book (" + this.id() + ") : " +
                this.title + " by " + this.author + ", created=" +
                new HumanDate(this.creationTimeMillis).toDateStr() + ", " +
                "modified=" + new HumanDate(this.lastModifiedTimeMillis).toTimeAgoStr() + ", " +
                "isDeleted=" + this.isDeleted + ", " +
                this.description;
    }

    /////////////////////////////////////////////////
    // BookInfo Business Logic Methods             //
    // - All Info manipulation logic is done here. //
    /////////////////////////////////////////////////

    public BookInfo withTitle(String title) {
        return new BookInfo(this.id(), title, this.author, this.description, this.creationTimeMillis, System.currentTimeMillis(), this.isDeleted);
    }
    public BookInfo withAuthor(String authorName) {
        return new BookInfo(this.id(), this.title, authorName, this.description, this.creationTimeMillis, System.currentTimeMillis(), this.isDeleted);
    }
    public BookInfo withDescription(String description) {
        return new BookInfo(this.id(), this.title, this.author, description, this.creationTimeMillis, System.currentTimeMillis(), this.isDeleted);
    }

    /////////////////////////////////////
    // ToEntity / ToDTO implementation //
    /////////////////////////////////////

    @Override
    public DTOBookInfo toInfoDTO() {
        return new DTOBookInfo(this);
    }

    @Override
    public EntityBookInfo toInfoEntity() {
        return new EntityBookInfo(this);
    }

    /////////////////////////////
    // ToDomain implementation //
    /////////////////////////////

    @Override
    public BookInfo toDeepCopyDomainInfo() {
        // shallow copy OK here bc its flat
        return new BookInfo(this);
    }
}
