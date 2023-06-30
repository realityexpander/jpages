package org.elegantobjects.jpages.App2.domain.book;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.log.ILog;
import org.elegantobjects.jpages.App2.common.util.log.Log;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.book.local.BookInfoDatabase;
import org.elegantobjects.jpages.App2.data.book.local.BookInfoEntity;
import org.elegantobjects.jpages.App2.data.book.network.BookInfoApi;
import org.elegantobjects.jpages.App2.data.book.network.BookInfoDTO;
import org.elegantobjects.jpages.App2.domain.common.Repo;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

// Business logic for Book Repo (simple CRUD operations; converts to/from DTOs/Entities/Domains)
public class BookInfoRepo extends Repo implements IBookInfoRepo {
    private final BookInfoApi api;
    private final BookInfoDatabase database;

    public BookInfoRepo(BookInfoApi api,
                        BookInfoDatabase database,
                        ILog log
    ) {
        super(log);
        this.api = api;
        this.database = database;
    }

    public BookInfoRepo() {
        this(new BookInfoApi(), new BookInfoDatabase(), new Log());
    }

    @Override
    public Result<BookInfo> fetchBookInfo(UUID2<Book> id) {
        log.d(this, "bookId " + id);

        // Make the request to API
        Result<BookInfoDTO> bookInfoApiResult = api.getBookInfo(id);
        if (bookInfoApiResult instanceof Result.Failure) {

            // If API fails, try to get from cached DB
            Result<BookInfoEntity> bookInfoResult = database.getBookInfo(id);
            if (bookInfoResult instanceof Result.Failure) {
                Exception exception = ((Result.Failure<BookInfoEntity>) bookInfoResult).exception();
                return new Result.Failure<BookInfo>(exception);
            }

            BookInfoEntity bookInfo = ((Result.Success<BookInfoEntity>) bookInfoResult).value();
            return new Result.Success<>(bookInfo.toDeepCopyDomainInfo());
        }

        // Convert to Domain Model
        BookInfo bookInfo = ((Result.Success<BookInfoDTO>) bookInfoApiResult)
                .value()
                .toDeepCopyDomainInfo();

        // Cache to Local DB
        Result<BookInfoEntity> resultDB = database.updateBookInfo(bookInfo.toInfoEntity());
        if (resultDB instanceof Result.Failure) {
            Exception exception = ((Result.Failure<BookInfoEntity>) resultDB).exception();
            return new Result.Failure<>(exception);
        }

        return new Result.Success<>(bookInfo);
    }

    @Override
    public Result<BookInfo> updateBookInfo(BookInfo bookInfo) {
        log.d(this, "bookInfo: " + bookInfo);

        Result<BookInfo> bookResult = saveBookToApiAndDB(bookInfo, UpdateKind.UPDATE);
        if (bookResult instanceof Result.Failure) {
            Exception exception = ((Result.Failure<BookInfo>) bookResult).exception();
            return new Result.Failure<>(exception);
        }

        return bookResult;
    }

    @Override
    public Result<BookInfo> addBookInfo(BookInfo bookInfo) {
        log.d(this, "bookInfo: " + bookInfo);

        Result<BookInfo> bookResult = saveBookToApiAndDB(bookInfo, UpdateKind.ADD);
        if (bookResult instanceof Result.Failure) {
            Exception exception = ((Result.Failure<BookInfo>) bookResult).exception();
            return new Result.Failure<>(exception);
        }

        return bookResult;
    }

    @Override
    public Result<BookInfo> upsertBookInfo(BookInfo bookInfo) {
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
        Result<BookInfoDTO> resultApi;
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
            Exception exception = ((Result.Failure<BookInfoDTO>) resultApi).exception();
            return new Result.Failure<>(exception);
        }

        // Save to Local DB
        Result<BookInfoEntity> resultDB;
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
            Exception exception = ((Result.Failure<BookInfoEntity>) resultDB).exception();
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
            database.addBookInfo(
                    new BookInfoEntity(
                            UUID2.createFakeUUID2(i*100, BookInfoEntity.class),
                            "Title " + i*100,
                            "Author " + i*100,
                            "Description " + i*100,
                            "Some extra info from the Entity" + i*100
                    )
            );
        }
    }

    public void populateApiWithFakeBookInfo() {
        for (int i = 0; i < 10; i++) {
            Result<BookInfoDTO> result = api.addBookInfo(
                    new BookInfoDTO(
                            UUID2.createFakeUUID2(i*100, BookInfoDTO.class),
                            "Title " + i*100,
                            "Author " + i*100,
                            "Description " + i*100,
                            "Some extra info from the DTO" + i*100)
            );

            if (result instanceof Result.Failure) {
                Exception exception = ((Result.Failure<BookInfoDTO>) result).exception();
                log.d(this, exception.getMessage());
            }
        }
    }

    public void printDB() {
        for (Map.Entry<UUID2<Book>, BookInfoEntity> entry : database.getAllBookInfos().entrySet()) {
            log.d(this, entry.getKey() + " = " + entry.getValue());
        }
    }

    public void printAPI() {
        for (Map.Entry<UUID2<Book>, BookInfoDTO> entry : api.getAllBookInfos().entrySet()) {
            log.d(this, entry.getKey() + " = " + entry.getValue());
        }
    }
}
