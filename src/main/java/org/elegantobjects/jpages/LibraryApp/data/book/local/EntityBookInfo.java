package org.elegantobjects.jpages.LibraryApp.data.book.local;

import org.elegantobjects.jpages.LibraryApp.common.Model;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.data.common.local.EntityInfo;
import org.elegantobjects.jpages.LibraryApp.data.common.Info;
import org.elegantobjects.jpages.LibraryApp.domain.book.Book;
import org.elegantobjects.jpages.LibraryApp.domain.book.BookInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        @NotNull String title,
        @NotNull String author,
        @NotNull String description,
        @Nullable String extraFieldToShowThisIsAnEntity
    ) {
        super(id);
        this.title = title;
        this.author = author;
        this.description = description;

        if(extraFieldToShowThisIsAnEntity == null) {
            this.extraFieldToShowThisIsAnEntity = "This is an Entity";
        } else {
            this.extraFieldToShowThisIsAnEntity = extraFieldToShowThisIsAnEntity;
        }
    }

    //////////////////////////////////////////////////////////
    // DTOInfo <-> DomainInfo conversion                    //
    // Note: Intentionally DON'T accept `DTOInfo.BookInfo`  //
    //   - to keep DB layer separate from API layer)        //
    //////////////////////////////////////////////////////////

    public
    EntityBookInfo(@NotNull EntityBookInfo bookInfo) {  // from EntityInfo.EntityBookInfo -> EntityInfo.EntityBookInfo
        this(
            new UUID2<Book>(bookInfo.id()),  // change `UUID2Type` to UUID2<Book>
            bookInfo.title,
            bookInfo.author,
            bookInfo.description,
            bookInfo.extraFieldToShowThisIsAnEntity
        );
    }
    public
    EntityBookInfo(@NotNull BookInfo bookInfo) {  // from DomainInfo.BookInfo -> EntityInfo.BookInfo
        this(
            bookInfo.id(),
            bookInfo.title,
            bookInfo.author,
            bookInfo.description,
            "Imported from Domain.DomainBookInfo"
        );
    }


    @Override
    public String toString() {
        return "Book (" + this.id() + ") : " +
                this.title + " by " + this.author + ", " +
                this.extraFieldToShowThisIsAnEntity + ", " +
                this.description;
    }

    ////////////////////////////////////////////
    // Entities don't have any business logic //
    // - All "Info" changes are done in the   //
    //   domain layer.                        //
    ////////////////////////////////////////////

    /////////////////////////////////
    // ToDomainInfo implementation //
    /////////////////////////////////

    @Override
    public BookInfo toDeepCopyDomainInfo() {
        // implement deep copy, if structure is not flat.
        return new BookInfo(this);
    }

    /////////////////////////////
    // ToInfo implementation   //
    /////////////////////////////

    @Override
    public EntityBookInfo toDeepCopyInfo() {
        // note: implement deep copy, if structure is not flat.
        return new EntityBookInfo(this);
    }
}
