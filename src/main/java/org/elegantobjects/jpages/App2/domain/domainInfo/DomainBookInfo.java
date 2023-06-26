package org.elegantobjects.jpages.App2.domain.domainInfo;

import org.elegantobjects.jpages.App2.common.DomainInfo;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.common.ModelInfo;
import org.elegantobjects.jpages.App2.data.local.EntityBookInfo;
import org.elegantobjects.jpages.App2.data.network.DTOBookInfo;
import org.elegantobjects.jpages.App2.domain.Book;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DomainBookInfo extends DomainInfo
    implements
        ModelInfo.ToEntity<EntityBookInfo>,
        ModelInfo.ToDTO<DTOBookInfo>,
        ModelInfo.ToDomain<DomainBookInfo>
{
    private final UUID2<Book> id; // note this is a UUID2<Book> not a UUID2<BookInfo>, it is the id of the Book.
    public final String title;
    public final String author;
    public final String description;

    public DomainBookInfo(
        @NotNull UUID2<Book> id,
        String title,
        String author,
        String description
    ) {
        super(id, DomainBookInfo.class.getName());
        this.title = title;
        this.author = author;
        this.description = description;
        this.id = id;
    }
    public DomainBookInfo(UUID uuid, String title, String author, String description) {
        this(new UUID2<Book>(uuid), title, author, description);
    }
    public DomainBookInfo(String id, String title, String author, String description) {
        this(UUID.fromString(id), title, author, description);
    }
    public DomainBookInfo(@NotNull DomainBookInfo bookInfo) {
        // todo validation
        this(bookInfo.id(), bookInfo.title, bookInfo.author, bookInfo.description);
    }
    public DomainBookInfo(UUID id) {
        this(id, "", "", "");
    }

    // Domain Must accept both `DTO.BookInfo` and `Entity.BookInfo` (and convert to Domain.BookInfo)
    public DomainBookInfo(@NotNull DTOBookInfo bookInfo) {
        // Converts from DTO to Domain
        // todo validation here

        // Domain decides what to include from the DTO
        this(bookInfo.getInfoId(),
                bookInfo.title,
                bookInfo.author,
                bookInfo.description);
    }
    public DomainBookInfo(@NotNull EntityBookInfo bookInfo) {
        // Converts from Entity to Domain
        // todo validation here

        // Domain decides what to include from the Entities
        this(bookInfo.getInfoId(),
                bookInfo.title,
                bookInfo.author,
                bookInfo.description);
    }

    /////////////////////////////////////////////////
    // BookInfo Business Logic Methods             //
    // - All Info manipulation logic is done here. //
    /////////////////////////////////////////////////

    @Override
    public UUID2<Book> id() {
        return this.id;
    }

    public DomainBookInfo withTitle(String title) {
        return new DomainBookInfo(this.id, title, this.author, this.description);
    }

    public DomainBookInfo withAuthor(String authorName) {
        return new DomainBookInfo(this.id, this.title, authorName, this.description);
    }

    public DomainBookInfo withDescription(String description) {
        return new DomainBookInfo(this.id, this.title, this.author, description);
    }

    @Override
    public String toString() {
        return "Book (" + this.id + ") : " + this.title + " by " + this.author + ", " + this.description;
    }

    /////////////////////////////////////
    // ToEntity / ToDTO implementation //
    /////////////////////////////////////

    @Override
    public DTOBookInfo toDTO() {
        return new DTOBookInfo(this);
    }

    @Override
    public EntityBookInfo toEntity() {
        return new EntityBookInfo(this);
    }

    /////////////////////////////
    // ToDomain implementation //
    /////////////////////////////

    @Override
    public DomainBookInfo toDeepCopyDomainInfo() {
        // shallow copy OK here bc its flat
        return new DomainBookInfo(this);
    }

    @Override
    public UUID2<?> getDomainInfoId() {
        return this.id;
    }
}
