package org.elegantobjects.jpages.App2.domain;


import org.elegantobjects.jpages.App2.data.Entity;
import org.elegantobjects.jpages.App2.data.DTO;
import org.elegantobjects.jpages.App2.core.Result;
import org.elegantobjects.jpages.App2.core.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.BookInfoApi;
import org.elegantobjects.jpages.App2.data.BookInfoDatabase;
import org.elegantobjects.jpages.App2.core.log.ILog;
import org.elegantobjects.jpages.App2.core.log.Log;
import org.elegantobjects.jpages.App2.domain.core.IRepo;

import java.util.Map;

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
        BookInfo() { this(new BookInfoApi(), new BookInfoDatabase(), new Log()); }

        @Override
        public Result<Domain.BookInfo> fetchBookInfo(UUID2<Book> id) {
            log.d(this,"Repo.BookRepo.fetchBookInfo " + id);

            // Make the request to API
            Result<DTO.BookInfo> bookInfoApiResult = api.getBookInfo(id);
            if (bookInfoApiResult instanceof Result.Failure) {

                // If API fails, try to get from cached DB
                Result<Entity.BookInfo> bookInfoResult = database.getBookInfo(id);
                if (bookInfoResult instanceof Result.Failure) {
                    Exception exception = ((Result.Failure<Entity.BookInfo>) bookInfoResult).exception();
                    return new Result.Failure<Domain.BookInfo>(exception);
                }

                Entity.BookInfo bookInfo = ((Result.Success<Entity.BookInfo>) bookInfoResult).value();
                return new Result.Success<>(bookInfo.toDeepCopyDomainInfo());
            }

            // Convert to Domain Model
            Domain.BookInfo bookInfo = ((Result.Success<DTO.BookInfo>) bookInfoApiResult)
                    .value()
                    .toDeepCopyDomainInfo();

            // Cache to Local DB
            Result<Entity.BookInfo> resultDB = database.updateBookInfo(bookInfo.toEntity());
            if (resultDB instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Entity.BookInfo>) resultDB).exception();
                return new Result.Failure<>(exception);
            }

            return new Result.Success<>(bookInfo);
        }

        @Override
        public Result<Domain.BookInfo> updateBookInfo(Domain.BookInfo bookInfo) {
            log.d(this,"Repo.BookRepo - Updating BookInfo: " + bookInfo);

            Result<Domain.BookInfo> bookResult = saveBookToApiAndDB(bookInfo, Repo.BookInfo.UpdateType.UPDATE);
            if (bookResult instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Domain.BookInfo>) bookResult).exception();
                return new Result.Failure<>(exception);
            }

            return bookResult;
        }

        @Override
        public Result<Domain.BookInfo> addBookInfo(Domain.BookInfo bookInfo) {
            log.d(this,"Repo.BookRepo - Adding book info: " + bookInfo);

            Result<Domain.BookInfo> bookResult = saveBookToApiAndDB(bookInfo, Repo.BookInfo.UpdateType.ADD);
            if (bookResult instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Domain.BookInfo>) bookResult).exception();
                return new Result.Failure<>(exception);
            }

            return bookResult;
        }

        @Override
        public Result<Domain.BookInfo> upsertBookInfo(Domain.BookInfo bookInfo) {
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

        private enum UpdateType {
            ADD,
            UPDATE,
            UPSERT,
            DELETE
        }

        private Result<Domain.BookInfo> saveBookToApiAndDB(
                Domain.BookInfo bookInfo,
                Repo.BookInfo.UpdateType updateType
        ) {
            log.d(this,"updateType: " + updateType + ", id: " + bookInfo.id());

            // Make the API request
            Result<DTO.BookInfo> resultApi;
            switch (updateType) {
                case UPDATE:
                    resultApi = api.updateBookInfo(bookInfo.toDTO());
                    break;
                case ADD:
                    resultApi = api.addBookInfo(bookInfo.toDTO());
                    break;
                default:
                    return new Result.Failure<>(new Exception("UpdateType not supported: " + updateType));
            }

            if (resultApi instanceof Result.Failure) {
                Exception exception = ((Result.Failure<DTO.BookInfo>) resultApi).exception();
                return new Result.Failure<>(exception);
            }

            // Save to Local DB
            Result<Entity.BookInfo> resultDB;
            switch (updateType) {
                case UPDATE:
                    resultDB = database.updateBookInfo(bookInfo.toEntity());
                    break;
                case ADD:
                    resultDB = database.addBookInfo(bookInfo.toEntity());
                    break;
                default:
                    return new Result.Failure<>(new Exception("UpdateType not supported: " + updateType));
            }

            if (resultDB instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Entity.BookInfo>) resultDB).exception();
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
                        new Entity.BookInfo(
                                UUID2.createFakeUUID2(i, Entity.BookInfo.class.getName()),
                                "Title " + i,
                                "Author " + i,
                                "Description " + i)
                );
            }
        }

        public void populateApiWithFakeBookInfo() {
            for (int i = 0; i < 10; i++) {
                Result<DTO.BookInfo> result = api.addBookInfo(
                        new DTO.BookInfo(
                                UUID2.createFakeUUID2(i, DTO.BookInfo.class.getName()),
                                "Title " + i,
                                "Author " + i,
                                "Description " + i,
                                "Some extra info from the DTO" + i)
                );

                if (result instanceof Result.Failure) {
                    Exception exception = ((Result.Failure<DTO.BookInfo>) result).exception();
                    log.d(this,exception.getMessage());
                }
            }
        }

        public void printDB() {
            for (Map.Entry<UUID2<Book>, Entity.BookInfo> entry : database.getAllBookInfos().entrySet()) {
                log.d(this,entry.getKey() + " = " + entry.getValue());
            }
        }

        public void printAPI() {
            for (Map.Entry<UUID2<Book>, DTO.BookInfo> entry : api.getAllBookInfos().entrySet()) {
                log.d(this,entry.getKey() + " = " + entry.getValue());
            }
        }
    }

    // Holds User info for all users in the system (simple CRUD operations)
    public static class UserInfo extends Repo implements IRepo.UserInfo {
        // Simulate a database on a server somewhere
        private final UUID2.HashMap<User, Domain.UserInfo> database = new UUID2.HashMap<>();

        public UserInfo(ILog log) {
            super(log);
        }

        @Override
        public Result<Domain.UserInfo> fetchUserInfo(UUID2<User> id) {
            log.d(this,"Repo.UserInfo - Fetching user info: " + id);

            // Simulate network/database
            if (database.containsKey(id)) {
                return new Result.Success<>(database.get(id));
            }

            return new Result.Failure<>(new Exception("Repo.UserInfo, UserInfo not found, id:" + id));
        }

        @Override
        public Result<Domain.UserInfo> updateUserInfo(Domain.UserInfo userInfo) {
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
        public Domain.UserInfo upsertUserInfo(Domain.UserInfo userInfo) {
            log.d(this,"Repo.UserInfo - Upserting user info: " + userInfo);

            database.put(userInfo.id(), userInfo);
            return userInfo;
        }

    }

    // Holds Library info for all the libraries in the system (simple CRUD operations)
    public static class LibraryInfo extends Repo implements IRepo.LibraryInfo {
        // simulate a database on server (UUID2<Library> is the key)
        private final UUID2.HashMap<Library, Domain.LibraryInfo> database = new UUID2.HashMap<>();

        public LibraryInfo(ILog log) {
            super(log);
        }

        @Override
        public Result<Domain.LibraryInfo> fetchLibraryInfo(UUID2<Library> id) {
            log.d(this,"Repo.LibraryInfo - Fetching library info: " + id);

            // Simulate network/database
            if (database.containsKey(id)) {
                return new Result.Success<>(database.get(id));
            }

            return new Result.Failure<>(new Exception("Repo.LibraryInfo, Library not found, id: " + id));
        }

        @Override
        public Result<Domain.LibraryInfo> updateLibraryInfo(Domain.LibraryInfo libraryInfo) {
            log.d(this,"Repo.LibraryInfo - Updating library info: " + libraryInfo.id());

            // Simulate network/database
            if (database.containsKey(libraryInfo.id())) {
                database.put(libraryInfo.id(), libraryInfo);

                return new Result.Success<>(libraryInfo);
            }

            return new Result.Failure<>(new Exception("Repo.LibraryInfo, Library not found, id: " + libraryInfo.id()));
        }

        @Override
        public Result<Domain.LibraryInfo> upsertLibraryInfo(Domain.LibraryInfo libraryInfo) {
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
            Domain.LibraryInfo library = database.get(libraryId);

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
