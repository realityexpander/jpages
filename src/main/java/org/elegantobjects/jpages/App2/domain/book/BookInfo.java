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
//        this.id = id;
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
    BookInfo(@NotNull DTOBookInfo bookInfo) {
        // Converts from DTO to Domain

        // Domain decides what to include from the DTO
        // todo add validation here
        this(
            new UUID2<Book>(bookInfo.id()), // change to domain type
            bookInfo.title,
            bookInfo.author,
            bookInfo.description
        );
    }
    public
    BookInfo(@NotNull EntityBookInfo bookInfo) {
        // Converts from Entity to Domain

        // Domain decides what to include from the Entities
        // todo add validation here
        this(
            new UUID2<Book>(bookInfo.id()), // change to domain type
            bookInfo.title,
            bookInfo.author,
            bookInfo.description
        );
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
