package org.elegantobjects.jpages.LibraryApp.domain.book.data.network;

import org.elegantobjects.jpages.LibraryApp.domain.common.data.Model;
import org.elegantobjects.jpages.LibraryApp.common.util.HumanDate;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.domain.common.data.info.network.DTOInfo;
import org.elegantobjects.jpages.LibraryApp.domain.common.data.info.Info;
import org.elegantobjects.jpages.LibraryApp.domain.book.Book;
import org.elegantobjects.jpages.LibraryApp.domain.Context;
import org.elegantobjects.jpages.LibraryApp.domain.book.data.BookInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * DTOBookInfo<br>
 * <br>
 * "Dumb" Data Transfer Object for BookInfo<br>
 * <br>
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

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
    public long creationTimeMillis;
    public long lastModifiedTimeMillis;
    public boolean isDeleted;

    public
    DTOBookInfo(
        @NotNull UUID2<Book> id,
        @NotNull String title,
        @NotNull String author,
        @NotNull String description,
        @Nullable String extraFieldToShowThisIsADTO,
        long creationTimeMillis,
        long lastModifiedTimeMillis,
        boolean isDeleted
    ) {
        super(id);
        this.title = title;
        this.author = author;
        this.description = description;

        if (extraFieldToShowThisIsADTO == null) {
            this.extraFieldToShowThisIsADTO = "This is a DTO";
        } else {
            this.extraFieldToShowThisIsADTO = extraFieldToShowThisIsADTO;
        }

        this.creationTimeMillis = creationTimeMillis;
        this.lastModifiedTimeMillis = lastModifiedTimeMillis;
        this.isDeleted = isDeleted;
    }
    public
    DTOBookInfo(@NotNull String json, @NotNull Context context) {
        this(context.gson.fromJson(json, DTOBookInfo.class));
    }

    /////////////////////////////////////////////////////////////////////
    // EntityInfo <-> DomainInfo conversion                            //
    // Note: Intentionally DON'T accept `EntityInfo.EntityBookInfo`    //
    //   - to keep DB layer separate from API layer                    //
    /////////////////////////////////////////////////////////////////////

    public
    DTOBookInfo(@NotNull DTOBookInfo bookInfo) {  // from DTOInfo.DTOBookInfo -> DTOInfo.DTOBookInfo
        this(
            new UUID2<Book>(bookInfo.id()),  // change `UUID2Type` to UUID2<Book>
            bookInfo.title,
            bookInfo.author,
            bookInfo.description,
            bookInfo.extraFieldToShowThisIsADTO,
            bookInfo.creationTimeMillis,
            bookInfo.lastModifiedTimeMillis,
            bookInfo.isDeleted
        );
    }
    public
    DTOBookInfo(@NotNull BookInfo bookInfo) { // from Domain.BookInfo -> DTOInfo.BookInfo
        this(
            bookInfo.id(),
            bookInfo.title,
            bookInfo.author,
            bookInfo.description,
            "Extra info added during creation of DTOInfo.DTOBookInfo",
            bookInfo.creationTimeMillis,
            bookInfo.lastModifiedTimeMillis,
            bookInfo.isDeleted
        );
    }

    @Override
    public String toString() {
        return "Book (" + this.id() + ") : " +
                this.title + " by " + this.author + ", created=" +
                new HumanDate(this.creationTimeMillis).toDateStr() + ", " +
                "modified=" + new HumanDate(this.lastModifiedTimeMillis).toTimeAgoStr() + ", " +
                "isDeleted=" + this.isDeleted + ", " +
                this.extraFieldToShowThisIsADTO + ", " +
                this.description;
    }

    ///////////////////////////////////////////
    // DTOs don't have any business logic    //
    // - All "Info" changes are done in the  //
    //   domain layer.                       //
    ///////////////////////////////////////////

    /////////////////////////////////
    // ToDomainInfo implementation //
    /////////////////////////////////

    @Override
    public BookInfo toDeepCopyDomainInfo() {
        // note: implement deep copy (if class is not flat.)
        return new BookInfo(this);
    }

    /////////////////////////////
    // ToInfo implementation   //
    /////////////////////////////

    @Override
    public DTOBookInfo toDeepCopyInfo() {
        // note: implement deep copy (if class is not flat.)
        return new DTOBookInfo(this);
    }
}


