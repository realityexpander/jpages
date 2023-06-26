package org.elegantobjects.jpages.App2.data;

import org.elegantobjects.jpages.App2.core.Model;
import org.elegantobjects.jpages.App2.core.Info;
import org.elegantobjects.jpages.App2.core.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.core.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.Book;
import org.elegantobjects.jpages.App2.domain.Domain;
import org.jetbrains.annotations.NotNull;

// Entities for Databases
// Simple data holder class for transferring data to/from the Domain from Database
public class Entity extends Model {
    Entity(UUID2<IUUID2> id, String className) {
        super(id, className);
    }

    public static class BookInfo extends Entity
        implements
            ToDomainInfo<Domain.BookInfo>,
            ToDomainInfo.hasToDeepCopyDomainInfo<Domain.BookInfo>,
            Info.ToInfo<BookInfo>,
            Info.hasToDeepCopyInfo<BookInfo>
    {
        final UUID2<Book> id;  // note this is a UUID2<Book> and not a UUID2<BookInfo>
        public final String title;
        public final String author;
        public final String description;
        final String extraFieldToShowThisIsAnEntity = "This is an Entity";

        public BookInfo(
                @NotNull UUID2<Book> id,
                String title,
                String author,
                String description
        ) {
            super(id.toDomainUUID2(), BookInfo.class.getName());
            this.id = id;
            this.title = title;
            this.author = author;
            this.description = description;
        }

        // Note: Intentionally DON'T accept `DTO.BookInfo` (to keep DB layer separate from API layer)
        BookInfo(BookInfo bookInfo) {
            this(bookInfo.id, bookInfo.title, bookInfo.author, bookInfo.description);
        }

        public BookInfo(Domain.BookInfo bookInfo) {
            this(bookInfo.id(), bookInfo.title, bookInfo.author, bookInfo.description);
        }
        // todo Is it better to have a constructor that takes in a DTO.BookInfo and throws an exception? Or to not have it at all?
        // BookInfo(DTO.BookInfo bookInfo) {
        //     // Never accept DTO.BookInfo to keep the API layer separate from the DB layer
        //     super(bookInfo.id.toDomainUUID2());
        //     throw new IllegalArgumentException("Entity.BookInfo should never be created from DTO.BookInfo");
        // }

        @Override
        public String toString() {
            return "Book (" + this.id + ") : " + this.title + " by " + this.author + ", " + this.description;
        }

        ////////////////////////////////////////////
        // Entities don't have any business logic //
        ////////////////////////////////////////////

        /////////////////////////////////
        // ToDomainInfo implementation //
        /////////////////////////////////

        @Override
        public Domain.BookInfo toDeepCopyDomainInfo() {
            // implement deep copy, if needed.
            return new Domain.BookInfo(this);
        }

        @Override
        public UUID2<?> getDomainInfoId() {
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
