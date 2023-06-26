package org.elegantobjects.jpages.App2.domain;


import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.local.EntityBookInfo;
import org.elegantobjects.jpages.App2.data.network.BookInfoApi;
import org.elegantobjects.jpages.App2.data.local.BookInfoDatabase;
import org.elegantobjects.jpages.App2.common.util.log.ILog;
import org.elegantobjects.jpages.App2.common.util.log.Log;
import org.elegantobjects.jpages.App2.data.network.DTOBookInfo;
import org.elegantobjects.jpages.App2.domain.common.IRepo;
import org.elegantobjects.jpages.App2.domain.repoData.DomainBookInfo;
import org.elegantobjects.jpages.App2.domain.repoData.DomainLibraryInfo;
import org.elegantobjects.jpages.App2.domain.repoData.DomainUserInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

// Since the Repo only accepts/returns Domain.{Domain}Info objects, this lives in the domain layer.
// - Internally, it accesses the data layer, and does conversions between the layers.
public class Repo implements IRepo {
    protected final ILog log;

    Repo(ILog log) {
        this.log = log;
    }

    // Business logic for Book Repo (simple CRUD oerations; converts to/from DTOs/Entities/Domains)
    public static class BookInfo extends Repo implements IRepo.BookInfo {
        private final BookInfoApi api;
        private final BookInfoDatabase database;

        public BookInfo(BookInfoApi api,
            BookInfoDatabase database,
            ILog log
        ) {
            super(log);
            this.api = api;
            this.database = database;
        }
        public BookInfo() { this(new BookInfoApi(), new BookInfoDatabase(), new Log()); }

        @Override
        public Result<DomainBookInfo> fetchBookInfo(UUID2<Book> id) {
            log.d(this,"Repo.BookRepo.fetchBookInfo " + id);

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
            log.d(this,"Repo.BookRepo - Updating BookInfo: " + bookInfo);

            Result<DomainBookInfo> bookResult = saveBookToApiAndDB(bookInfo, UpdateKind.UPDATE);
            if (bookResult instanceof Result.Failure) {
                Exception exception = ((Result.Failure<DomainBookInfo>) bookResult).exception();
                return new Result.Failure<>(exception);
            }

            return bookResult;
        }

        @Override
        public Result<DomainBookInfo> addBookInfo(DomainBookInfo bookInfo) {
            log.d(this,"Repo.BookRepo - Adding book info: " + bookInfo);

            Result<DomainBookInfo> bookResult = saveBookToApiAndDB(bookInfo, UpdateKind.ADD);
            if (bookResult instanceof Result.Failure) {
                Exception exception = ((Result.Failure<DomainBookInfo>) bookResult).exception();
                return new Result.Failure<>(exception);
            }

            return bookResult;
        }

        @Override
        public Result<DomainBookInfo> upsertBookInfo(DomainBookInfo bookInfo) {
            log.d(this,"Repo.Book - Upserting book id: " + bookInfo.id());

            if (database.getBookInfo(bookInfo.id()) != null) {
                return updateBookInfo(bookInfo);
            } else {
                return addBookInfo(bookInfo);
            }
        }

        ///////////////////////////////////
        // Private Helper Methods
        ///////////////////////////////////

        private enum UpdateKind {
            ADD,
            UPDATE,
            UPSERT,
            DELETE
        }

        private Result<DomainBookInfo> saveBookToApiAndDB(
            @NotNull DomainBookInfo bookInfo,
            @NotNull Repo.BookInfo.UpdateKind updateKind
        ) {
            log.d(this,"updateType: " + updateKind + ", id: " + bookInfo.id());

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
                                "Description " + i)
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
                    log.d(this,exception.getMessage());
                }
            }
        }

        public void printDB() {
            for (Map.Entry<UUID2<Book>, EntityBookInfo> entry : database.getAllBookInfos().entrySet()) {
                log.d(this,entry.getKey() + " = " + entry.getValue());
            }
        }

        public void printAPI() {
            for (Map.Entry<UUID2<Book>, DTOBookInfo> entry : api.getAllBookInfos().entrySet()) {
                log.d(this,entry.getKey() + " = " + entry.getValue());
            }
        }
    }

    // Holds User info for all users in the system (simple CRUD operations)
    public static class UserInfo extends Repo implements IRepo.UserInfo {
        // Simulate a database on a server somewhere
        private final UUID2.HashMap<User, DomainUserInfo> database = new UUID2.HashMap<>();

        public UserInfo(ILog log) {
            super(log);
        }

        @Override
        public Result<DomainUserInfo> fetchUserInfo(UUID2<User> id) {
            log.d(this,"Repo.UserInfo - Fetching user info: " + id);

            // Simulate network/database
            if (database.containsKey(id)) {
                return new Result.Success<>(database.get(id));
            }

            return new Result.Failure<>(new Exception("Repo.UserInfo, UserInfo not found, id:" + id));
        }

        @Override
        public Result<DomainUserInfo> updateUserInfo(DomainUserInfo userInfo) {
            String method = Thread.currentThread().getStackTrace()[2].getMethodName();
            log.d(this, "Repo.User - " + method + " - Updating user info: " + userInfo);

            // Simulate network/database
            if (database.containsKey(userInfo.id())) {
                database.put(userInfo.id(), userInfo);
                return new Result.Success<>(userInfo);
            }

            return new Result.Failure<>(new Exception("Repo.UserInfo, UserInfo not found, id:" + userInfo.id()));
        }

        @Override
        public DomainUserInfo upsertUserInfo(DomainUserInfo userInfo) {
            log.d(this,"Repo.UserInfo - Upserting user info: " + userInfo);

            database.put(userInfo.id(), userInfo);
            return userInfo;
        }

    }

    // Holds Library info for all the libraries in the system (simple CRUD operations)
    public static class LibraryInfo extends Repo implements IRepo.LibraryInfo {
        // simulate a database on server (UUID2<Library> is the key)
        private final UUID2.HashMap<Library, DomainLibraryInfo> database = new UUID2.HashMap<>();

        public LibraryInfo(ILog log) {
            super(log);
        }

        @Override
        public Result<DomainLibraryInfo> fetchLibraryInfo(UUID2<Library> id) {
            log.d(this,"Repo.LibraryInfo - Fetching library info: " + id);

            // Simulate network/database
            if (database.containsKey(id)) {
                return new Result.Success<>(database.get(id));
            }

            return new Result.Failure<>(new Exception("Repo.LibraryInfo, Library not found, id: " + id));
        }

        @Override
        public Result<DomainLibraryInfo> updateLibraryInfo(DomainLibraryInfo libraryInfo) {
            log.d(this,"Repo.LibraryInfo - Updating library info: " + libraryInfo.id());

            // Simulate network/database
            if (database.containsKey(libraryInfo.id())) {
                database.put(libraryInfo.id(), libraryInfo);

                return new Result.Success<>(libraryInfo);
            }

            return new Result.Failure<>(new Exception("Repo.LibraryInfo, Library not found, id: " + libraryInfo.id()));
        }

        @Override
        public Result<DomainLibraryInfo> upsertLibraryInfo(DomainLibraryInfo libraryInfo) {
            log.d(this,"Repo.LibraryInfo - Upsert library info: " + libraryInfo.id());

            // Simulate network/database
            database.put(libraryInfo.id(), libraryInfo);

            return new Result.Success<>(libraryInfo);
        }

        ///////////////////////////////////
        /// Published Helper methods    ///
        ///////////////////////////////////

        public void populateWithFakeBooks(UUID2<Library> libraryId, int numberOfBooksToCreate) {
            log.d(this,"libraryId: " + libraryId + ", numberOfBooksToCreate: " + numberOfBooksToCreate);
            DomainLibraryInfo library = database.get(libraryId);

            for (int i = 0; i < numberOfBooksToCreate; i++) {
                Result<UUID2<Book>> result = library.addTestBook(UUID2.createFakeUUID2(i, Book.class.getName()), 1);

                if (result instanceof Result.Failure) {
                    Exception exception = ((Result.Failure<UUID2<Book>>) result).exception();
                    log.d(this,exception.getMessage());
                }
            }
        }

    }
}
