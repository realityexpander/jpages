package org.elegantobjects.jpages.App2;


import org.elegantobjects.jpages.App2.domain.Book;
import org.elegantobjects.jpages.App2.domain.User;

import java.util.Map;

// Repo only accepts/returns Domain Models, and internally converts to/from DTOs/Entities/Domains
// - works with the network API & local database to perform CRUD operations, and also performs validation.
// - can also be used to implement caching.
// The Repo can easily accept fake APIs & Database for testing.
interface IRepo {
    interface BookInfo extends IRepo {
        Result<Model.Domain.BookInfo> fetchBookInfo(UUID2<Book> id);
        Result<Model.Domain.BookInfo> addBookInfo(Model.Domain.BookInfo bookInfo);
        Result<Model.Domain.BookInfo> updateBookInfo(Model.Domain.BookInfo bookInfo);
        Result<Model.Domain.BookInfo> upsertBookInfo(Model.Domain.BookInfo bookInfo);
    }

    interface UserInfo extends IRepo {
        Result<Model.Domain.UserInfo> fetchUserInfo(UUID2<User> id);
        Result<Model.Domain.UserInfo> updateUserInfo(Model.Domain.UserInfo userInfo);
        Model.Domain.UserInfo upsertUserInfo(Model.Domain.UserInfo userInfo);
    }

    interface LibraryInfo extends IRepo {
        Result<Model.Domain.LibraryInfo> fetchLibraryInfo(UUID2<Library> id);
        Result<Model.Domain.LibraryInfo> updateLibraryInfo(Model.Domain.LibraryInfo libraryInfo);
        Result<Model.Domain.LibraryInfo> upsertLibraryInfo(Model.Domain.LibraryInfo libraryInfo);
    }
}
public class Repo implements IRepo {
    protected final ILog log;

    Repo(ILog log) {
        this.log = log;
    }

    // Business logic for Book Repo (simple CRUD oerations; converts to/from DTOs/Entities/Domains)
    public static class BookInfo extends Repo implements IRepo.BookInfo {
        private final BookInfoApi api;
        private final BookInfoDatabase database;

        BookInfo(BookInfoApi api,
                 BookInfoDatabase database,
                 ILog log
        ) {
            super(log);
            this.api = api;
            this.database = database;
        }
        BookInfo() { this(new BookInfoApi(), new BookInfoDatabase(), new Log()); }

        @Override
        public Result<Model.Domain.BookInfo> fetchBookInfo(UUID2<Book> id) {
            log.d(this,"Repo.BookRepo.fetchBookInfo " + id);

            // Make the request to API
            Result<Model.DTO.BookInfo> bookInfoApiResult = api.getBookInfo(id);
            if (bookInfoApiResult instanceof Result.Failure) {

                // If API fails, try to get from cached DB
                Result<Model.Entity.BookInfo> bookInfoResult = database.getBookInfo(id);
                if (bookInfoResult instanceof Result.Failure) {
                    Exception exception = ((Result.Failure<Model.Entity.BookInfo>) bookInfoResult).exception();
                    return new Result.Failure<Model.Domain.BookInfo>(exception);
                }

                Model.Entity.BookInfo bookInfo = ((Result.Success<Model.Entity.BookInfo>) bookInfoResult).value();
                return new Result.Success<>(bookInfo.toDeepCopyDomainInfo());
            }

            // Convert to Domain Model
            Model.Domain.BookInfo bookInfo = ((Result.Success<Model.DTO.BookInfo>) bookInfoApiResult)
                    .value()
                    .toDeepCopyDomainInfo();

            // Cache to Local DB
            Result<Model.Entity.BookInfo> resultDB = database.updateBookInfo(bookInfo.toEntity());
            if (resultDB instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Model.Entity.BookInfo>) resultDB).exception();
                return new Result.Failure<>(exception);
            }

            return new Result.Success<>(bookInfo);
        }

        @Override
        public Result<Model.Domain.BookInfo> updateBookInfo(Model.Domain.BookInfo bookInfo) {
            log.d(this,"Repo.BookRepo - Updating BookInfo: " + bookInfo);

            Result<Model.Domain.BookInfo> bookResult = saveBookToApiAndDB(bookInfo, Repo.BookInfo.UpdateType.UPDATE);
            if (bookResult instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Model.Domain.BookInfo>) bookResult).exception();
                return new Result.Failure<>(exception);
            }

            return bookResult;
        }

        @Override
        public Result<Model.Domain.BookInfo> addBookInfo(Model.Domain.BookInfo bookInfo) {
            log.d(this,"Repo.BookRepo - Adding book info: " + bookInfo);

            Result<Model.Domain.BookInfo> bookResult = saveBookToApiAndDB(bookInfo, Repo.BookInfo.UpdateType.ADD);
            if (bookResult instanceof Result.Failure) {
                Exception exception = ((Result.Failure<Model.Domain.BookInfo>) bookResult).exception();
                return new Result.Failure<>(exception);
            }

            return bookResult;
        }

        @Override
        public Result<Model.Domain.BookInfo> upsertBookInfo(Model.Domain.BookInfo bookInfo) {
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

        private Result<Model.Domain.BookInfo> saveBookToApiAndDB(
                Model.Domain.BookInfo bookInfo,
                Repo.BookInfo.UpdateType updateType
        ) {
            log.d(this,"updateType: " + updateType + ", id: " + bookInfo.id());

            // Make the API request
            Result<Model.DTO.BookInfo> resultApi;
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
                Exception exception = ((Result.Failure<Model.DTO.BookInfo>) resultApi).exception();
                return new Result.Failure<>(exception);
            }

            // Save to Local DB
            Result<Model.Entity.BookInfo> resultDB;
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
                Exception exception = ((Result.Failure<Model.Entity.BookInfo>) resultDB).exception();
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
                        new Model.Entity.BookInfo(
                                UUID2.createFakeUUID2(i, Model.Entity.BookInfo.class.getName()),
                                "Title " + i,
                                "Author " + i,
                                "Description " + i)
                );
            }
        }

        public void populateApiWithFakeBookInfo() {
            for (int i = 0; i < 10; i++) {
                Result<Model.DTO.BookInfo> result = api.addBookInfo(
                        new Model.DTO.BookInfo(
                                UUID2.createFakeUUID2(i, Model.DTO.BookInfo.class.getName()),
                                "Title " + i,
                                "Author " + i,
                                "Description " + i,
                                "Some extra info from the DTO" + i)
                );

                if (result instanceof Result.Failure) {
                    Exception exception = ((Result.Failure<Model.DTO.BookInfo>) result).exception();
                    log.d(this,exception.getMessage());
                }
            }
        }

        public void printDB() {
            for (Map.Entry<UUID2<Book>, Model.Entity.BookInfo> entry : database.getAllBookInfos().entrySet()) {
                log.d(this,entry.getKey() + " = " + entry.getValue());
            }
        }

        public void printAPI() {
            for (Map.Entry<UUID2<Book>, Model.DTO.BookInfo> entry : api.getAllBookInfos().entrySet()) {
                log.d(this,entry.getKey() + " = " + entry.getValue());
            }
        }
    }

    // Holds User info for all users in the system (simple CRUD operations)
    public static class UserInfo extends Repo implements IRepo.UserInfo {
        // Simulate a database on a server somewhere
        private final UUID2.HashMap<User, Model.Domain.UserInfo> database = new UUID2.HashMap<>();

        UserInfo(ILog log) {
            super(log);
        }

        @Override
        public Result<Model.Domain.UserInfo> fetchUserInfo(UUID2<User> id) {
            log.d(this,"Repo.User - Fetching user info: " + id);

            if (database.containsKey(id)) {
                return new Result.Success<>(database.get(id));
            }

            return new Result.Failure<>(new Exception("User not found"));
        }

        @Override
        public Result<Model.Domain.UserInfo> updateUserInfo(Model.Domain.UserInfo userInfo) {
            String method = Thread.currentThread().getStackTrace()[2].getMethodName();
            log.d(this, "Repo.User - " + method + " - Updating user info: " + userInfo);

            if (database.containsKey(userInfo.id())) {
                database.put(userInfo.id(), userInfo);
                return new Result.Success<>(userInfo);
            }

            return new Result.Failure<>(new Exception("User not found, id:" + userInfo.id()));
        }

        @Override
        public Model.Domain.UserInfo upsertUserInfo(Model.Domain.UserInfo userInfo) {
            log.d(this,"Repo.User - Upserting user info: " + userInfo);

            database.put(userInfo.id(), userInfo);
            return userInfo;
        }

    }

    // Holds Library info for all the libraries in the system (simple CRUD operations)
    public static class LibraryInfo extends Repo implements IRepo.LibraryInfo {
        // simulate a database on server (UUID2<Library> is the key)
        private final UUID2.HashMap<Library, Model.Domain.LibraryInfo> database = new UUID2.HashMap<>();

        LibraryInfo(ILog log) {
            super(log);
        }

        @Override
        public Result<Model.Domain.LibraryInfo> fetchLibraryInfo(UUID2<Library> id) {
            log.d(this,"id: " + id);

            // Simulate a network request
            if (database.containsKey(id)) {
                return new Result.Success<>(database.get(id));
            }

            return new Result.Failure<>(new Exception("Library not found, id: " + id));
        }

        @Override
        public Result<Model.Domain.LibraryInfo> updateLibraryInfo(Model.Domain.LibraryInfo libraryInfo) {
            log.d(this," libraryInfo: " + libraryInfo);

            // Simulate a network request
            if (database.containsKey(libraryInfo.id)) {
                database.put(libraryInfo.id, libraryInfo);

                return new Result.Success<>(libraryInfo);
            }

            return new Result.Failure<>(new Exception("Library not found, id: " + libraryInfo.id));
        }

        @Override
        public Result<Model.Domain.LibraryInfo> upsertLibraryInfo(Model.Domain.LibraryInfo libraryInfo) {
            log.d(this,"libraryInfo: " + libraryInfo);

            database.put(libraryInfo.id, libraryInfo);

            return new Result.Success<>(libraryInfo);
        }

        ///////////////////////////////////
        /// Published Helper methods    ///
        ///////////////////////////////////

        public void populateWithFakeBooks(UUID2<Library> libraryId, int numberOfBooksToCreate) {
            log.d(this,"libraryId: " + libraryId + ", numberOfBooksToCreate: " + numberOfBooksToCreate);
            Model.Domain.LibraryInfo library = database.get(libraryId);

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
