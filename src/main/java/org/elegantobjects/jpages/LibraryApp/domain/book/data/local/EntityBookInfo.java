package org.elegantobjects.jpages.LibraryApp.domain.book.data.local;

import org.elegantobjects.jpages.LibraryApp.domain.common.data.Model;
import org.elegantobjects.jpages.LibraryApp.common.util.HumanDate;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.domain.common.data.info.local.EntityInfo;
import org.elegantobjects.jpages.LibraryApp.domain.common.data.info.Info;
import org.elegantobjects.jpages.LibraryApp.domain.Context;
import org.elegantobjects.jpages.LibraryApp.domain.book.Book;
import org.elegantobjects.jpages.LibraryApp.domain.book.data.BookInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * EntityBookInfo is a Data Transfer Object (DTO) that is used to transfer data between the
 * Domain Layer and the Data Layer.
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

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
    public final long creationTimeMillis;
    public final long lastModifiedTimeMillis;
    public final boolean isDeleted;

    public
    EntityBookInfo(
            @NotNull UUID2<Book> id,
            @NotNull String title,
            @NotNull String author,
            @NotNull String description,
            @Nullable String extraFieldToShowThisIsAnEntity,
            long creationTimeMillis,
            long lastModifiedTimeMillis,
            boolean isDeleted
    ) {
        super(id);
        this.title = title;
        this.author = author;
        this.description = description;

        this.creationTimeMillis = creationTimeMillis;
        this.lastModifiedTimeMillis = lastModifiedTimeMillis;
        this.isDeleted = isDeleted;

        if(extraFieldToShowThisIsAnEntity == null) {
            this.extraFieldToShowThisIsAnEntity = "This is an EntityBookInfo"; // default value
        } else {
            this.extraFieldToShowThisIsAnEntity = extraFieldToShowThisIsAnEntity;
        }
    }
    public
    EntityBookInfo(@NotNull String json, @NotNull Context context) {
        this(context.gson.fromJson(json, EntityBookInfo.class));
    }

    ////////////////////////////////////////////////////////////
    // DTOInfo <-> DomainInfo conversion                      //
    // Note: Intentionally DON'T accept `DTOInfo.DTOBookInfo` //
    //   - to keep DB layer separate from API layer)          //
    ////////////////////////////////////////////////////////////

    public
    EntityBookInfo(@NotNull EntityBookInfo bookInfo) {  // from EntityInfo.EntityBookInfo -> EntityInfo.EntityBookInfo
        this(
            new UUID2<Book>(bookInfo.id()),  // change `UUID2Type` to UUID2<Book>
            bookInfo.title,
            bookInfo.author,
            bookInfo.description,
            bookInfo.extraFieldToShowThisIsAnEntity,
            bookInfo.creationTimeMillis,
            bookInfo.lastModifiedTimeMillis,
            bookInfo.isDeleted
        );
    }
    public
    EntityBookInfo(@NotNull BookInfo bookInfo) {  // from DomainInfo.BookInfo -> EntityInfo.BookInfo
        this(
            bookInfo.id(),
            bookInfo.title,
            bookInfo.author,
            bookInfo.description,
            "Extra info added during creation of EntityInfo.EntityBookInfo",
            bookInfo.creationTimeMillis,
            bookInfo.lastModifiedTimeMillis,
            false
        );
    }

    @Override
    public String toString() {
        return "Book (" + this.id() + ") : " +
                this.title + " by " + this.author + ", created=" +
                new HumanDate(this.creationTimeMillis).toDateStr() + ", " +
                "modified=" + new HumanDate(this.lastModifiedTimeMillis).toTimeAgoStr() + ", " +
                "isDeleted=" + this.isDeleted + ", " +
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
        // implement deep copy (if structure is not flat.)
        return new BookInfo(this);
    }

    /////////////////////////////
    // ToInfo implementation   //
    /////////////////////////////

    @Override
    public EntityBookInfo toDeepCopyInfo() {
        // note: implement deep copy (if structure is not flat.)
        return new EntityBookInfo(this);
    }
}
