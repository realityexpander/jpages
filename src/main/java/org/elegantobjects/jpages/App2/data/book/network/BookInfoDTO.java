package org.elegantobjects.jpages.App2.data.book.network;

import org.elegantobjects.jpages.App2.common.Model;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.common.network.DTO;
import org.elegantobjects.jpages.App2.data.common.Info;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.Context;
import org.elegantobjects.jpages.App2.domain.book.BookInfo;
import org.jetbrains.annotations.NotNull;

public class BookInfoDTO extends DTO
        implements
        Model.ToInfoDomain<BookInfo>,
        Model.ToInfoDomain.hasToDeepCopyDomainInfo<BookInfo>,
        Info.ToInfo<BookInfoDTO>,
        Info.hasToDeepCopyInfo<BookInfoDTO>
{
    final UUID2<Book> id; // note this is a UUID2<Book> and not a UUID2<BookInfo>
    public final String title;
    public final String author;
    public final String description;
    public final String extraFieldToShowThisIsADTO;

    public BookInfoDTO(
        @NotNull UUID2<Book> id,
        String title,
        String author,
        String description,
        String extraFieldToShowThisIsADTO
    ) {
//        super(id.toDomainUUID2(), DTOBookInfo.class.getName());
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
    public BookInfoDTO(String json, @NotNull Context context) {
        this(context.gson.fromJson(json, BookInfoDTO.class));  // creates a DTO.BookInfo from the JSON
    }

    // Note: Intentionally DON'T accept `Entity.EntityBookInfo` (to keep DB layer separate from API layer)
    BookInfoDTO(@NotNull BookInfoDTO bookInfo) {
        this(new UUID2<Book>(bookInfo.id().uuid(), Book.class),
            bookInfo.title,
            bookInfo.author,
            bookInfo.description,
            bookInfo.extraFieldToShowThisIsADTO);
    }
    public BookInfoDTO(@NotNull BookInfo bookInfo) {
        this(new UUID2<Book>(bookInfo.id().uuid(), Book.class),
            bookInfo.title,
            bookInfo.author,
            bookInfo.description,
            "Imported from Domain.BookInfo");
    }
    // todo - Is it better to have a constructor that takes in a DTO.BookInfo and throws an exception? Or to not have it at all?
    // BookInfo(Entity.BookInfo bookInfo) {
    //     // Never accept Entity.BookInfo to keep the API layer separate from the DB layer
    //     super(bookInfo.id.toDomainUUID2());
    //     throw new IllegalArgumentException("DTO.BookInfo should never be created from Entity.BookInfo");
    // }

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
        // note: implement deep copy, if required.
        return new BookInfo(
                this.id,
                this.title,
                this.author,
                this.description
        );
    }

    @Override
    public UUID2<Book> getDomainInfoId() {
        return this.id;
    }

    /////////////////////////////
    // ToInfo implementation   //
    /////////////////////////////

    @Override
    public BookInfoDTO toDeepCopyInfo() {
        // note: implement deep copy, if needed.
        return new BookInfoDTO(this);
    }

    @Override
    public UUID2<Book> getInfoId() {
        return this.id;
    }
}
