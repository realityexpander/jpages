package org.elegantobjects.jpages.App2.domain.book;

import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.domain.common.DomainInfo;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.common.Model;
import org.elegantobjects.jpages.App2.data.book.local.BookInfoEntity;
import org.elegantobjects.jpages.App2.data.book.network.BookInfoDTO;
import org.elegantobjects.jpages.App2.domain.library.Library;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BookInfo extends DomainInfo
    implements
        Model.ToInfoEntity<BookInfoEntity>,
        Model.ToInfoDTO<BookInfoDTO>,
        Model.ToInfoDomain<BookInfo>
{
    public final UUID2<Book> id; // note this is a UUID2<Book> not a UUID2<BookInfo>, it is the id of the Book.
    public final String title;
    public final String author;
    public final String description;

    public BookInfo(
        @NotNull UUID2<Book> id,  // Note: This is a UUID2<Book> not a UUID2<BookInfo>
        String title,
        String author,
        String description
    ) {
        super(id);
        this.title = title;
        this.author = author;
        this.description = description;
        this.id = id;
    }
    public BookInfo(UUID uuid, String title, String author, String description) {
        this(new UUID2<Book>(uuid, Book.class), title, author, description);
    }
    public BookInfo(String id, String title, String author, String description) {
        this(UUID.fromString(id), title, author, description);
    }
    public BookInfo(@NotNull BookInfo bookInfo) {
        // todo validation
        this(bookInfo.id(), bookInfo.title, bookInfo.author, bookInfo.description);
    }
    public BookInfo(UUID id) {
        this(id, "", "", "");
    }
    public <TDomainUUID2 extends IUUID2>
        BookInfo(UUID2<TDomainUUID2> uuid2) {
        this(uuid2.uuid(), "", "", "");
    }

    // DomainInfo objects Must:
    // - Accept both `DTO.BookInfo` and `Entity.BookInfo`
    // - Convert to Domain.BookInfo
    public BookInfo(@NotNull BookInfoDTO bookInfo) {
        // Converts from DTO to Domain

        // Domain decides what to include from the DTO
        // todo validation here
        this(
            new UUID2<Book>(bookInfo.id.uuid(), Book.class), // change to domain type
            bookInfo.title,
            bookInfo.author,
            bookInfo.description
        );
    }
    public BookInfo(@NotNull BookInfoEntity bookInfo) {
        // Converts from Entity to Domain

        // Domain decides what to include from the Entities
        // todo validation here
        this(
            new UUID2<Book>(bookInfo.id.uuid(), Book.class), // change to domain type
            bookInfo.title,
            bookInfo.author,
            bookInfo.description
        );
    }

    @Override
    public String toString() {
        return "Book (" + this.id + ") : " + this.title + " by " + this.author + ", " + this.description;
    }

    ////////////////////////////////
    // Published Getters          //
    ////////////////////////////////

    @Override
    public UUID2<Book> id() {
        return this.id;
    }

    /////////////////////////////////////////////////
    // BookInfo Business Logic Methods             //
    // - All Info manipulation logic is done here. //
    /////////////////////////////////////////////////

    public BookInfo withTitle(String title) {
        return new BookInfo(this.id, title, this.author, this.description);
    }
    public BookInfo withAuthor(String authorName) {
        return new BookInfo(this.id, this.title, authorName, this.description);
    }
    public BookInfo withDescription(String description) {
        return new BookInfo(this.id, this.title, this.author, description);
    }

    /////////////////////////////////////
    // ToEntity / ToDTO implementation //
    /////////////////////////////////////

    @Override
    public BookInfoDTO toInfoDTO() {
        return new BookInfoDTO(this);
    }

    @Override
    public BookInfoEntity toInfoEntity() {
        return new BookInfoEntity(this);
    }

    /////////////////////////////
    // ToDomain implementation //
    /////////////////////////////

    @Override
    public BookInfo toDeepCopyDomainInfo() {
        // shallow copy OK here bc its flat
        return new BookInfo(this);
    }

    @Override
    public UUID2<?> getDomainInfoId() {
        return this.id;
    }
}
