package org.elegantobjects.jpages.App2.domain.repo;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.log.ILog;
import org.elegantobjects.jpages.App2.common.util.log.Log;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.local.BookInfoDatabase;
import org.elegantobjects.jpages.App2.data.local.EntityBookInfo;
import org.elegantobjects.jpages.App2.data.network.BookInfoApi;
import org.elegantobjects.jpages.App2.data.network.DTOBookInfo;
import org.elegantobjects.jpages.App2.domain.Book;
import org.elegantobjects.jpages.App2.domain.common.IRepo;
import org.elegantobjects.jpages.App2.domain.common.Repo;
import org.elegantobjects.jpages.App2.domain.domainInfo.DomainBookInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

// Business logic for Book Repo (simple CRUD operations; converts to/from DTOs/Entities/Domains)
public class BookInfoRepo extends Repo implements IRepo.BookInfo {
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
    public Result<DomainBookInfo> fetchBookInfo(UUID2<Book> id) {
        log.d(this, "Repo.BookRepo.fetchBookInfo " + id);

        // Make the request to API
        Result<DTOBookInfo> bookInfoApiResult = api.getBookInfo(id);
        if (bookInfoApiResult instanceof Result.Failure) {

            // If API fails, try to get from cached DB
            Result<EntityBookInfo> bookInfoResult = database.getBookInfo(id);
            if (bookInfoResult instanceof Result.Failure) {
                Exception exception = ((Result.Failure<EntityBookInfo>) bookInfoResult).exception();
                return new Result.Failure<DomainBookInfo>(exception);
            }

            EntityBookInfo bookInfo = ((Result.Success<EntityBookInfo>) bookInfoResult).value();
            return new Result.Success<>(bookInfo.toDeepCopyDomainInfo());
        }

        // Convert to Domain Model
        DomainBookInfo bookInfo = ((Result.Success<DTOBookInfo>) bookInfoApiResult)
                .value()
                .toDeepCopyDomainInfo();

        // Cache to Local DB
        Result<EntityBookInfo> resultDB = database.updateBookInfo(bookInfo.toEntity());
        if (resultDB instanceof Result.Failure) {
            Exception exception = ((Result.Failure<EntityBookInfo>) resultDB).exception();
            return new Result.Failure<>(exception);
        }

        return new Result.Success<>(bookInfo);
    }

    @Override
    public Result<DomainBookInfo> updateBookInfo(DomainBookInfo bookInfo) {
        log.d(this, "Repo.BookRepo - Updating BookInfo: " + bookInfo);

        Result<DomainBookInfo> bookResult = saveBookToApiAndDB(bookInfo, UpdateKind.UPDATE);
        if (bookResult instanceof Result.Failure) {
            Exception exception = ((Result.Failure<DomainBookInfo>) bookResult).exception();
            return new Result.Failure<>(exception);
        }

        return bookResult;
    }

    @Override
    public Result<DomainBookInfo> addBookInfo(DomainBookInfo bookInfo) {
        log.d(this, "Repo.BookRepo - Adding book info: " + bookInfo);

        Result<DomainBookInfo> bookResult = saveBookToApiAndDB(bookInfo, UpdateKind.ADD);
        if (bookResult instanceof Result.Failure) {
            Exception exception = ((Result.Failure<DomainBookInfo>) bookResult).exception();
            return new Result.Failure<>(exception);
        }

        return bookResult;
    }

    @Override
    public Result<DomainBookInfo> upsertBookInfo(DomainBookInfo bookInfo) {
        log.d(this, "Repo.Book - Upserting book id: " + bookInfo.id());

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

    private Result<DomainBookInfo> saveBookToApiAndDB(
            @NotNull DomainBookInfo bookInfo,
            @NotNull UpdateKind updateKind
    ) {
        log.d(this, "updateType: " + updateKind + ", id: " + bookInfo.id());

        // Make the API request
        Result<DTOBookInfo> resultApi;
        switch (updateKind) {
            case UPDATE:
                resultApi = api.updateBookInfo(bookInfo.toDTO());
                break;
            case ADD:
                resultApi = api.addBookInfo(bookInfo.toDTO());
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
                resultDB = database.updateBookInfo(bookInfo.toEntity());
                break;
            case ADD:
                resultDB = database.addBookInfo(bookInfo.toEntity());
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
            database.addBookInfo(
                    new EntityBookInfo(
                            UUID2.createFakeUUID2(i, EntityBookInfo.class.getName()),
                            "Title " + i,
                            "Author " + i,
                            "Description " + i,
                            "Some extra info from the Entity" + i
                    )
            );
        }
    }

    public void populateApiWithFakeBookInfo() {
        for (int i = 0; i < 10; i++) {
            Result<DTOBookInfo> result = api.addBookInfo(
                    new DTOBookInfo(
                            UUID2.createFakeUUID2(i, DTOBookInfo.class.getName()),
                            "Title " + i,
                            "Author " + i,
                            "Description " + i,
                            "Some extra info from the DTO" + i)
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
