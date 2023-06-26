package org.elegantobjects.jpages.App2.domain.core;

import org.elegantobjects.jpages.App2.Model;
import org.elegantobjects.jpages.App2.core.Result;
import org.elegantobjects.jpages.App2.core.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.Book;
import org.elegantobjects.jpages.App2.domain.Library;
import org.elegantobjects.jpages.App2.domain.User;

// Repo only accepts/returns Domain Models, and internally converts to/from DTOs/Entities/Domains
// - works with the network API & local database to perform CRUD operations, and also performs validation.
// - can also be used to implement caching.
// The Repo can easily accept fake APIs & Database for testing.
public interface IRepo {
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
