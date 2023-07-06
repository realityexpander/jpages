package org.elegantobjects.jpages.App2.domain.book;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.log.ILog;
import org.elegantobjects.jpages.App2.common.util.log.Log;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.book.local.BookInfoDatabase;
import org.elegantobjects.jpages.App2.data.book.local.EntityBookInfo;
import org.elegantobjects.jpages.App2.data.book.network.BookInfoApi;
import org.elegantobjects.jpages.App2.data.book.network.DTOBookInfo;
import org.elegantobjects.jpages.App2.domain.common.Repo;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

// Business logic for Book Repo (simple CRUD operations; converts to/from DTOs/Entities/Domains)
public class BookInfoRepo extends Repo implements IBookInfoRepo {
    private final BookInfoApi api;
    private final BookInfoDatabase database;

    public
    BookInfoRepo(
        @NotNull BookInfoApi api,
        @NotNull BookInfoDatabase database,
        ILog log
    ) {
        super(log);
        this.api = api;
        this.database = database;
    }

    public
    BookInfoRepo() {
        this(new BookInfoApi(), new BookInfoDatabase(), new Log());
    }

    @Override
    public Result<BookInfo> fetchBookInfo(@NotNull UUID2<Book> id) {
        log.d(this, "bookId " + id);

        // Make the request to API
        Result<DTOBookInfo> bookInfoApiResult = api.getBookInfo(id);
        if (bookInfoApiResult instanceof Result.Failure) {

            // If API fails, try to get from cached DB
            Result<EntityBookInfo> bookInfoResult = database.getBookInfo(id);
            if (bookInfoResult instanceof Result.Failure) {
                Exception exception = ((Result.Failure<EntityBookInfo>) bookInfoResult).exception();
                return new Result.Failure<BookInfo>(exception);
            }

            EntityBookInfo bookInfo = ((Result.Success<EntityBookInfo>) bookInfoResult).value();
            return new Result.Success<>(bookInfo.toDeepCopyDomainInfo());
        }

        // Convert to Domain Model
        BookInfo bookInfo = ((Result.Success<DTOBookInfo>) bookInfoApiResult)
                .value()
                .toDeepCopyDomainInfo();

        // Cache to Local DB
        Result<EntityBookInfo> resultDB = database.updateBookInfo(bookInfo.toInfoEntity());
        if (resultDB instanceof Result.Failure) {
            Exception exception = ((Result.Failure<EntityBookInfo>) resultDB).exception();
            return new Result.Failure<>(exception);
        }

        return new Result.Success<>(bookInfo);
    }

    @Override
    public Result<BookInfo> updateBookInfo(@NotNull BookInfo bookInfo) {
        log.d(this, "bookInfo: " + bookInfo);

        Result<BookInfo> bookResult = saveBookToApiAndDB(bookInfo, UpdateKind.UPDATE);
        if (bookResult instanceof Result.Failure) {
            Exception exception = ((Result.Failure<BookInfo>) bookResult).exception();
            return new Result.Failure<>(exception);
        }

        return bookResult;
    }

    @Override
    public Result<BookInfo> addBookInfo(@NotNull BookInfo bookInfo) {
        log.d(this, "bookInfo: " + bookInfo);

        Result<BookInfo> bookResult = saveBookToApiAndDB(bookInfo, UpdateKind.ADD);
        if (bookResult instanceof Result.Failure) {
            Exception exception = ((Result.Failure<BookInfo>) bookResult).exception();
            return new Result.Failure<>(exception);
        }

        return bookResult;
    }

    @Override
    public Result<BookInfo> upsertBookInfo(@NotNull BookInfo bookInfo) {
        log.d(this, "bookId: " + bookInfo.id());

        if (database.getBookInfo(bookInfo.id()) != null) {
            return updateBookInfo(bookInfo);
        } else {
            return addBookInfo(bookInfo);
        }
    }

    /////////////////////////////
    // Private Helper Methods  //
    /////////////////////////////


    private enum UpdateKind {
        ADD,
        UPDATE,
        UPSERT,
        DELETE
    }

    private Result<BookInfo> saveBookToApiAndDB(
        @NotNull BookInfo bookInfo,
        @NotNull UpdateKind updateKind
    ) {
        log.d(this, "updateType: " + updateKind + ", id: " + bookInfo.id());

        // Make the API request
        Result<DTOBookInfo> resultApi;
        switch (updateKind) {
            case UPDATE:
                resultApi = api.updateBookInfo(bookInfo.toInfoDTO());
                break;
            case ADD:
                resultApi = api.addBookInfo(bookInfo.toInfoDTO());
                break;
            default:
                return new Result.Failure<>(new Exception("UpdateType not supported: " + updateKind));
        }

        if (resultApi instanceof Result.Failure) {
            Exception exception = ((Result.Failure<DTOBookInfo>) resultApi).exception();
            return new Result.Failure<>(exception);
        }

        // Save to Local DB
        Result<EntityBookInfo> resultDB;
        switch (updateKind) {
            case UPDATE:
                resultDB = database.updateBookInfo(bookInfo.toInfoEntity());
                break;
            case ADD:
                resultDB = database.addBookInfo(bookInfo.toInfoEntity());
                break;
            default:
                return new Result.Failure<>(new Exception("UpdateType not supported: " + updateKind));
        }

        if (resultDB instanceof Result.Failure) {
            Exception exception = ((Result.Failure<EntityBookInfo>) resultDB).exception();
            return new Result.Failure<>(exception);
        }

        return new Result.Success<>(bookInfo);
    }


    /////////////////////////////////////////////////////
    // Debugging / Testing Methods                     //
    //  - not part of interface or used in production) //
    /////////////////////////////////////////////////////

    public void populateDatabaseWithFakeBookInfo() {
        for (int i = 0; i < 10; i++) {
            final int id = 1000+i*100;

            database.addBookInfo(
                new EntityBookInfo(
                    UUID2.createFakeUUID2(id, EntityBookInfo.class),
                    "Title " + id,
                    "Author " + id,
                    "Description " + id,
                    "Some extra info from the Entity" + id
                )
            );
        }
    }

    public void populateApiWithFakeBookInfo() {
        for (int i = 0; i < 10; i++) {
            final int id = 1000+i*100;

            Result<DTOBookInfo> result = api.addBookInfo(
                new DTOBookInfo(
                    UUID2.createFakeUUID2(id, DTOBookInfo.class),
                    "Title " + id,
                    "Author " + id,
                    "Description " + id,
                    "Some extra info from the DTO" + id
                )
            );

            if (result instanceof Result.Failure) {
                Exception exception = ((Result.Failure<DTOBookInfo>) result).exception();
                log.d(this, exception.getMessage());
            }
        }
    }

    public void printDB() {
        for (Map.Entry<UUID2<Book>, EntityBookInfo> entry : database.getAllBookInfos().entrySet()) {
            log.d(this, entry.getKey() + " = " + entry.getValue());
        }
    }

    public void printAPI() {
        for (Map.Entry<UUID2<Book>, DTOBookInfo> entry : api.getAllBookInfos().entrySet()) {
            log.d(this, entry.getKey() + " = " + entry.getValue());
        }
    }
}
