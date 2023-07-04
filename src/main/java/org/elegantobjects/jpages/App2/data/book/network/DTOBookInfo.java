package org.elegantobjects.jpages.App2.data.book.network;

import org.elegantobjects.jpages.App2.common.Model;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.common.network.DTOInfo;
import org.elegantobjects.jpages.App2.data.common.Info;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.Context;
import org.elegantobjects.jpages.App2.domain.book.BookInfo;
import org.jetbrains.annotations.NotNull;

public class DTOBookInfo extends DTOInfo
        implements
        Model.ToDomainInfo<BookInfo>,
        Model.ToDomainInfo.hasToDeepCopyDomainInfo<BookInfo>,
        Info.ToInfo<DTOBookInfo>,
        Info.hasToDeepCopyInfo<DTOBookInfo>
{
    public final String title;
    public final String author;
    public final String description;
    public final String extraFieldToShowThisIsADTO;

    public
    DTOBookInfo(
        @NotNull UUID2<Book> id,
        String title,
        String author,
        String description,
        String extraFieldToShowThisIsADTO
    ) {
        super(id);
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;

        if (extraFieldToShowThisIsADTO == null) {
            this.extraFieldToShowThisIsADTO = "This is a DTO";
        } else {
            this.extraFieldToShowThisIsADTO = extraFieldToShowThisIsADTO;
        }
    }
    public
    DTOBookInfo(String json, @NotNull Context context) {
        this(context.gson.fromJson(json, DTOBookInfo.class));  // creates a DTOInfo.BookInfo from the JSON
    }

    // Note: Intentionally DON'T accept `Entity.EntityBookInfo` (to keep DB layer separate from API layer)
    public
    DTOBookInfo(@NotNull DTOBookInfo bookInfo) {
        this(new UUID2<Book>(bookInfo.id.uuid(), Book.class),
            bookInfo.title,
            bookInfo.author,
            bookInfo.description,
            bookInfo.extraFieldToShowThisIsADTO);
    }
    public
    DTOBookInfo(@NotNull BookInfo bookInfo) {
        this(new UUID2<Book>(bookInfo.id().uuid(), Book.class),
            bookInfo.title,
            bookInfo.author,
            bookInfo.description,
            "Imported from Domain.BookInfo");
    }
    // Never accept Entity.BookInfo to keep the API layer separate from the DB layer
    // Note: no constructor for BookInfo(Entity.BookInfo bookInfo)

    @Override
    public String toString() {
        return "Book (" + this.id + ") : " + this.title + " by " + this.author + ", " + this.description + ", " + this.extraFieldToShowThisIsADTO;
    }

    ///////////////////////////////////////////
    // DTOs don't have any business logic    //
    ///////////////////////////////////////////

    ///////////////////////////////////
    // ToDomainInfo implementation   //
    ///////////////////////////////////

    @Override
    public BookInfo toDeepCopyDomainInfo() {
        // note: implement deep copy, if class is not flat.
        return new BookInfo(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public UUID2<Book> getDomainInfoId() {
        return (UUID2<Book>) this.id;
    }

    /////////////////////////////
    // ToInfo implementation   //
    /////////////////////////////

    @Override
    public DTOBookInfo toDeepCopyInfo() {
        // note: implement deep copy, if needed.
        return new DTOBookInfo(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public UUID2<Book> getInfoId() {
        return (UUID2<Book>) this.id;
    }
}