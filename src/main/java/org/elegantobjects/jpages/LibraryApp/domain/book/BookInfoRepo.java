package org.elegantobjects.jpages.LibraryApp.domain.book;

import org.elegantobjects.jpages.LibraryApp.common.util.Result;
import org.elegantobjects.jpages.LibraryApp.common.util.log.ILog;
import org.elegantobjects.jpages.LibraryApp.common.util.log.Log;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.data.book.local.BookInfoDatabase;
import org.elegantobjects.jpages.LibraryApp.data.book.local.EntityBookInfo;
import org.elegantobjects.jpages.LibraryApp.data.book.network.BookInfoApi;
import org.elegantobjects.jpages.LibraryApp.data.book.network.DTOBookInfo;
import org.elegantobjects.jpages.LibraryApp.domain.common.Repo;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * BookInfoRepo is a repository for the BookInfo class.<br>
 * <br>
 * Business logic for Book Repo (simple CRUD operations; converts to/from DTOs/Entities/Domains)
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

public class BookInfoRepo extends Repo implements IBookInfoRepo {
    private final BookInfoApi bookInfoApi;
    private final BookInfoDatabase bookInfoDatabase;

    public
    BookInfoRepo(
        @NotNull BookInfoApi bookInfoApi,
        @NotNull BookInfoDatabase bookInfoDatabase,
        @NotNull ILog log
    ) {
        super(log);
        this.bookInfoApi = bookInfoApi;
        this.bookInfoDatabase = bookInfoDatabase;
    }
    public
    BookInfoRepo() {
        this(new BookInfoApi(), new BookInfoDatabase(), new Log());
    }

    @Override
    public Result<BookInfo> fetchBookInfo(@NotNull UUID2<Book> id) {
        log.d(this, "bookId " + id);

        // Make the request to API
        Result<DTOBookInfo> bookInfoApiResult = bookInfoApi.getBookInfo(id);
        if (bookInfoApiResult instanceof Result.Failure) {

            // If API fails, try to get from cached DB
            Result<EntityBookInfo> bookInfoResult = bookInfoDatabase.getBookInfo(id);
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
        Result<EntityBookInfo> resultDB = bookInfoDatabase.updateBookInfo(bookInfo.toInfoEntity());
        if (resultDB instanceof Result.Failure) {
            Exception exception = ((Result.Failure<EntityBookInfo>) resultDB).exception();
            return new Result.Failure<>(exception);
        }

        return new Result.Success<>(bookInfo);
    }

    @Override
    public Result<BookInfo> updateBookInfo(@NotNull BookInfo bookInfo) {
        log.d(this, "bookInfo: " + bookInfo);

        Result<BookInfo> saveResult = saveBookInfoToApiAndDB(bookInfo, UpdateKind.UPDATE);
        if (saveResult instanceof Result.Failure) {
            Exception exception = ((Result.Failure<BookInfo>) saveResult).exception();
            return new Result.Failure<>(exception);
        }

        return saveResult;
    }

    @Override
    public Result<BookInfo> addBookInfo(@NotNull BookInfo bookInfo) {
        log.d(this, "bookInfo: " + bookInfo);

        Result<BookInfo> saveResult = saveBookInfoToApiAndDB(bookInfo, UpdateKind.ADD);
        if (saveResult instanceof Result.Failure) {
            Exception exception = ((Result.Failure<BookInfo>) saveResult).exception();
            return new Result.Failure<>(exception);
        }

        return saveResult;
    }

    @Override
    public Result<BookInfo> upsertBookInfo(@NotNull BookInfo bookInfo) {
        log.d(this, "bookId: " + bookInfo.id());

        Result<BookInfo> saveResult = saveBookInfoToApiAndDB(bookInfo, UpdateKind.UPSERT);
        if (saveResult instanceof Result.Failure) {
            Exception exception = ((Result.Failure<BookInfo>) saveResult).exception();
            return new Result.Failure<>(exception);
        }

        return saveResult;
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

    private Result<BookInfo> saveBookInfoToApiAndDB(
        @NotNull BookInfo bookInfo,
        @NotNull UpdateKind updateKind
    ) {
        log.d(this, "updateType: " + updateKind + ", id: " + bookInfo.id());

        // Make the API request
        Result<DTOBookInfo> apiResult;
        switch (updateKind) {
            case UPDATE:
                Result<DTOBookInfo> bookExistsResult = bookInfoApi.getBookInfo(bookInfo.id());
                if(bookExistsResult instanceof Result.Failure)
                    return new Result.Failure<>(((Result.Failure<DTOBookInfo>) bookExistsResult).exception());
                apiResult = bookInfoApi.updateBookInfo(bookInfo.toInfoDTO());
                break;
            case UPSERT:
                apiResult = bookInfoApi.updateBookInfo(bookInfo.toInfoDTO());
                break;
            case ADD:
                apiResult = bookInfoApi.addBookInfo(bookInfo.toInfoDTO());
                break;
            default:
                return new Result.Failure<>(new Exception("UpdateType not supported: " + updateKind));
        }

        if (apiResult instanceof Result.Failure) {
            Exception exception = ((Result.Failure<DTOBookInfo>) apiResult).exception();
            return new Result.Failure<>(exception);
        }

        // Save to Local DB
        Result<EntityBookInfo> dbResult;
        switch (updateKind) {
            case UPDATE:
                Result<EntityBookInfo> bookExistsResult = bookInfoDatabase.getBookInfo(bookInfo.id());
                if(bookExistsResult instanceof Result.Failure)
                    return new Result.Failure<>(((Result.Failure<EntityBookInfo>) bookExistsResult).exception());
                dbResult = bookInfoDatabase.updateBookInfo(bookInfo.toInfoEntity());
                break;
            case UPSERT:
                dbResult = bookInfoDatabase.updateBookInfo(bookInfo.toInfoEntity());
                break;
            case ADD:
                dbResult = bookInfoDatabase.addBookInfo(bookInfo.toInfoEntity());
                break;
            default:
                return new Result.Failure<>(new Exception("UpdateType not supported: " + updateKind));
        }

        if (dbResult instanceof Result.Failure) {
            Exception exception = ((Result.Failure<EntityBookInfo>) dbResult).exception();
            return new Result.Failure<>(exception);
        }

        return new Result.Success<>(bookInfo);
    }

    public Result<BookInfo> upsertTestEntityBookInfoToDB(EntityBookInfo entityBookInfo) {
        Result<EntityBookInfo> result = bookInfoDatabase.upsertBookInfo(entityBookInfo);
        if (result instanceof Result.Failure) {
            Exception exception = ((Result.Failure<EntityBookInfo>) result).exception();
            return new Result.Failure<>(exception);
        }

        return new Result.Success<>(entityBookInfo.toDeepCopyDomainInfo());
    }

    public Result<BookInfo> upsertTestDTOBookInfoToApi(DTOBookInfo dtoBookInfo) {
        Result<DTOBookInfo> result = bookInfoApi.upsertBookInfo(dtoBookInfo);
        if (result instanceof Result.Failure) {
            Exception exception = ((Result.Failure<DTOBookInfo>) result).exception();
            return new Result.Failure<>(exception);
        }

        return new Result.Success<>(dtoBookInfo.toDeepCopyDomainInfo());
    }

    /////////////////////////////////////////////////////
    // Debugging Methods                               //
    //  - not part of interface or used in production) //
    /////////////////////////////////////////////////////

    public void printDB() {
        for (Map.Entry<UUID2<Book>, EntityBookInfo> entry : bookInfoDatabase.getAllBookInfos().entrySet()) {
            log.d(this, entry.getKey() + " = " + entry.getValue());
        }
    }

    public void printAPI() {
        for (Map.Entry<UUID2<Book>, DTOBookInfo> entry : bookInfoApi.getAllBookInfos().entrySet()) {
            log.d(this, entry.getKey() + " = " + entry.getValue());
        }
    }
}
