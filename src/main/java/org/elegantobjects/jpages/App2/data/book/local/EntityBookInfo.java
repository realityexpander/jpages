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
    public final String title;
    public final String author;
    public final String description;
    public final String extraFieldToShowThisIsAnEntity;

    public
    EntityBookInfo(
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


    public @SuppressWarnings("unchecked")
    EntityBookInfo(@NotNull EntityBookInfo bookInfo) {
        this((UUID2<Book>) bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description, bookInfo.extraFieldToShowThisIsAnEntity);
    }
    public
    EntityBookInfo(@NotNull BookInfo bookInfo) {
        this(bookInfo.id(), bookInfo.title, bookInfo.author, bookInfo.description, "Imported from Domain.DomainBookInfo");
    }
    // Note: Intentionally DON'T accept `DTO.BookInfo` (to keep DB layer separate from API layer)

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
    public UUID2<?> domainInfoId() {
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
