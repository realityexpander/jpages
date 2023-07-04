package org.elegantobjects.jpages.App2.data.book.local;

import org.elegantobjects.jpages.App2.common.Model;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.common.local.EntityInfo;
import org.elegantobjects.jpages.App2.data.common.Info;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.book.BookInfo;
import org.jetbrains.annotations.NotNull;

public class EntityBookInfo extends EntityInfo
    implements
        Model.ToDomainInfo<BookInfo>,
        Model.ToDomainInfo.hasToDeepCopyDomainInfo<BookInfo>,
        Info.ToInfo<EntityBookInfo>,
        Info.hasToDeepCopyInfo<EntityBookInfo>
{
//    public final UUID2<Book> id;  // note this is a UUID2<Book> and not a UUID2<BookInfo>
    public final String title;
    public final String author;
    public final String description;
    public final String extraFieldToShowThisIsAnEntity;

    public EntityBookInfo(
        @NotNull UUID2<Book> id,
        String title,
        String author,
        String description,
        String extraFieldToShowThisIsAnEntity
    ) {
        super(id);
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;

        if(extraFieldToShowThisIsAnEntity == null) {
            this.extraFieldToShowThisIsAnEntity = "This is an Entity";
        } else {
            this.extraFieldToShowThisIsAnEntity = extraFieldToShowThisIsAnEntity;
        }
    }

    // Note: Intentionally DON'T accept `DTO.BookInfo` (to keep DB layer separate from API layer)
    @SuppressWarnings("unchecked")
    public EntityBookInfo(@NotNull EntityBookInfo bookInfo) {
        this((UUID2<Book>) bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description, bookInfo.extraFieldToShowThisIsAnEntity);
    }
    public EntityBookInfo(@NotNull BookInfo bookInfo) {
        this(bookInfo.id(), bookInfo.title, bookInfo.author, bookInfo.description, "Imported from Domain.DomainBookInfo");
    }
    // todo Is it better to have a constructor that takes in a DTO.BookInfo and throws an exception? Or to not have it at all?
    // BookInfo(DTO.BookInfo bookInfo) {
    //     // Never accept DTO.BookInfo to keep the API layer separate from the DB layer
    //     super(bookInfo.id.toDomainUUID2());
    //     throw new IllegalArgumentException("Entity.BookInfo should never be created from DTO.BookInfo");
    // }

    @Override
    public String toString() {
        return "Book (" + this.id + ") : "
                + this.title + " by " + this.author + ", " +
                this.extraFieldToShowThisIsAnEntity + ", " +
                this.description;
    }

    ////////////////////////////////////////////
    // Entities don't have any business logic //
    ////////////////////////////////////////////

    /////////////////////////////////
    // ToDomainInfo implementation //
    /////////////////////////////////

    @Override
    public BookInfo toDeepCopyDomainInfo() {
        // implement deep copy, if structure is not flat.
        return new BookInfo(this);
    }

    @Override
    public UUID2<?> getDomainInfoId() {
        return this.id;
    }

    /////////////////////////////
    // ToInfo implementation   //
    /////////////////////////////

    @Override
    public EntityBookInfo toDeepCopyInfo() {
        // note: implement deep copy, if structure is not flat.
        return new EntityBookInfo(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public UUID2<Book> getInfoId() {
        return (UUID2<Book>) this.id;
    }
}
