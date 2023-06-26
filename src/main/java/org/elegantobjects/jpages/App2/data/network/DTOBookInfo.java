package org.elegantobjects.jpages.App2.data.network;

import org.elegantobjects.jpages.App2.common.Model;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.common.Info;
import org.elegantobjects.jpages.App2.domain.Book;
import org.elegantobjects.jpages.App2.domain.Context;
import org.elegantobjects.jpages.App2.domain.repoData.DomainBookInfo;
import org.jetbrains.annotations.NotNull;

public class DTOBookInfo extends DTO
        implements
        Model.ToDomain<DomainBookInfo>,
        Model.ToDomain.hasToDeepCopyDomainInfo<DomainBookInfo>,
        Info.ToInfo<DTOBookInfo>,
        Info.hasToDeepCopyInfo<DTOBookInfo> {
    final UUID2<Book> id; // note this is a UUID2<Book> and not a UUID2<BookInfo>
    public final String title;
    public final String author;
    public final String description;
    final String extraFieldToShowThisIsADTO;

    public DTOBookInfo(
            @NotNull UUID2<Book> id,
            String title,
            String author,
            String description,
            String extraFieldToShowThisIsADTO
    ) {
        super(id.toDomainUUID2(), DTOBookInfo.class.getName());
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

    public DTOBookInfo(String json, Context context) {
        this(context.gson.fromJson(json, DTOBookInfo.class));  // creates a DTO.BookInfo from the JSON
    }

    // Note: Intentionally DON'T accept `Entity.BookInfo` (to keep DB layer separate from API layer)
    DTOBookInfo(DTOBookInfo bookInfo) {
        this(new UUID2<Book>(bookInfo.id().uuid()),
                bookInfo.title,
                bookInfo.author,
                bookInfo.description,
                bookInfo.extraFieldToShowThisIsADTO);
    }

    public DTOBookInfo(DomainBookInfo bookInfo) {
        this(new UUID2<Book>(bookInfo.id().uuid()),
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
    public DomainBookInfo toDeepCopyDomainInfo() {
        // note: implement deep copy, if required.
        return new DomainBookInfo(
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
    public DTOBookInfo toDeepCopyInfo() {
        // note: implement deep copy, if needed.
        return new DTOBookInfo(this);
    }

    @Override
    public UUID2<Book> getInfoId() {
        return this.id;
    }
}
