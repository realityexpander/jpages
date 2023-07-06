package org.elegantobjects.jpages.App2.domain.book;

import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.domain.common.DomainInfo;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.common.Model;
import org.elegantobjects.jpages.App2.data.book.local.EntityBookInfo;
import org.elegantobjects.jpages.App2.data.book.network.DTOBookInfo;
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

    public
    BookInfo(
        @NotNull UUID2<Book> id,  // Note: This is a UUID2<Book> not a UUID2<BookInfo>
        @NotNull String title,
        @NotNull String author,
        @NotNull String description
    ) {
        super(id);
        this.title = title;
        this.author = author;
        this.description = description;
    }
    public
    BookInfo(@NotNull UUID uuid, @NotNull String title, @NotNull String author, @NotNull String description) {
        this(new UUID2<Book>(uuid, Book.class), title, author, description);
    }
    public
    BookInfo(@NotNull String id, @NotNull String title, @NotNull String author, @NotNull String description) {
        this(UUID.fromString(id), title, author, description);
    }
    public
    BookInfo(@NotNull BookInfo bookInfo) {
        // todo add validation

        this(bookInfo.id(), bookInfo.title, bookInfo.author, bookInfo.description);
    }
    public
    BookInfo(@NotNull UUID id) {
        this(id, "", "", "");
    }
    public <TDomainUUID2 extends IUUID2>
    BookInfo(@NotNull UUID2<TDomainUUID2> uuid2) {
        this(uuid2.uuid(), "", "", "");
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
            dtoBookInfo.description
        );

        // Basic Validation = Domain decides what to include from the DTO
        // - must be done after conversion
        validateBookInfo();
    }

    public
    BookInfo(@NotNull EntityBookInfo entityBookInfo) {
        // Converts from EntityInfo to DomainInfo
        this(
            new UUID2<Book>(entityBookInfo.id()), // change to domain UUID2 type
            entityBookInfo.title,
            entityBookInfo.author,
            entityBookInfo.description
        );

        // Basic Validation - Domain decides what to include from the Entities
        // - must be done after conversion
        validateBookInfo();
    }

    private void validateBookInfo() {
        if(title.length() > 100)
            throw new IllegalArgumentException("DTOBookInfo.title cannot be longer than 100 characters");
        if(author.length() > 100)
            throw new IllegalArgumentException("DTOBookInfo.author cannot be longer than 100 characters");
        if(description.length() > 1000)
            throw new IllegalArgumentException("DTOBookInfo.description cannot be longer than 1000 characters");

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
        return "Book (" + this.id() + ") : " + this.title + " by " + this.author + ", " + this.description;
    }

    /////////////////////////////////////////////////
    // BookInfo Business Logic Methods             //
    // - All Info manipulation logic is done here. //
    /////////////////////////////////////////////////

    public BookInfo withTitle(String title) {
        return new BookInfo(this.id(), title, this.author, this.description);
    }
    public BookInfo withAuthor(String authorName) {
        return new BookInfo(this.id(), this.title, authorName, this.description);
    }
    public BookInfo withDescription(String description) {
        return new BookInfo(this.id(), this.title, this.author, description);
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
