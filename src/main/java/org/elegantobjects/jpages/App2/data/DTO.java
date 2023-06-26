package org.elegantobjects.jpages.App2.data;

import org.elegantobjects.jpages.App2.Model;
import org.elegantobjects.jpages.App2.core.Info;
import org.elegantobjects.jpages.App2.core.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.core.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.Book;
import org.elegantobjects.jpages.App2.domain.Context;
import org.elegantobjects.jpages.App2.domain.Domain;
import org.jetbrains.annotations.NotNull;

// Data Transfer Objects for APIs
// - Simple data holder class for transferring data to/from the Domain from API
// - Objects can be created from JSON
public class DTO extends Model {
    public DTO(UUID2<IUUID2> id, String className) {
        super(id, className);
    }

    public static class BookInfo extends DTO
            implements
            ToDomainInfo<Domain.BookInfo>,
            ToDomainInfo.hasToDeepCopyDomainInfo<Domain.BookInfo>,
            Info.ToInfo<BookInfo>,
            Info.hasToDeepCopyInfo<BookInfo> {
        final UUID2<Book> id; // note this is a UUID2<Book> and not a UUID2<BookInfo>
        public final String title;
        public final String author;
        public final String description;
        final String extraFieldToShowThisIsADTO;

        public BookInfo(@NotNull
                        UUID2<Book> id,
                        String title,
                        String author,
                        String description,
                        String extraFieldToShowThisIsADTO
        ) {
            super(id.toDomainUUID2(), BookInfo.class.getName());
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

        public BookInfo(String json, Context context) {
            this(context.gson.fromJson(json, BookInfo.class));  // creates a DTO.BookInfo from the JSON
        }

        // Note: Intentionally DON'T accept `Entity.BookInfo` (to keep DB layer separate from API layer)
        BookInfo(BookInfo bookInfo) {
            this(new UUID2<Book>(bookInfo.id().uuid()),
                    bookInfo.title,
                    bookInfo.author,
                    bookInfo.description,
                    bookInfo.extraFieldToShowThisIsADTO);
        }

        public BookInfo(Domain.BookInfo bookInfo) {
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
            return "Book (" + this.id + ") : " + this.title + " by " + this.author + ", " + this.description;
        }

        ///////////////////////////////////////////
        // DTOs don't have any business logic    //
        ///////////////////////////////////////////

        ///////////////////////////////////
        // ToDomainInfo implementation   //
        ///////////////////////////////////

        @Override
        public Domain.BookInfo toDeepCopyDomainInfo() {
            // note: implement deep copy, if required.
            return new Domain.BookInfo(
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
        public BookInfo toDeepCopyInfo() {
            // note: implement deep copy, if needed.
            return new BookInfo(this);
        }

        @Override
        public UUID2<Book> getInfoId() {
            return this.id;
        }
    }
}
