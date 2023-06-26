package org.elegantobjects.jpages.App2.domain.core;

import org.elegantobjects.jpages.App2.core.Result;
import org.elegantobjects.jpages.App2.core.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.Book;
import org.elegantobjects.jpages.App2.domain.Domain;
import org.elegantobjects.jpages.App2.domain.Library;
import org.elegantobjects.jpages.App2.domain.User;

// Repo only accepts/returns Domain Models, and internally converts to/from DTOs/Entities/Domains
// - works with the network API & local database to perform CRUD operations, and also performs validation.
// - can also be used to implement caching.
// The Repo can easily accept fake APIs & Database for testing.
public interface IRepo {
    interface BookInfo extends IRepo {
        Result<Domain.BookInfo> fetchBookInfo(UUID2<Book> id);

        Result<Domain.BookInfo> addBookInfo(Domain.BookInfo bookInfo);

        Result<Domain.BookInfo> updateBookInfo(Domain.BookInfo bookInfo);

        Result<Domain.BookInfo> upsertBookInfo(Domain.BookInfo bookInfo);
    }

    interface UserInfo extends IRepo {
        Result<Domain.UserInfo> fetchUserInfo(UUID2<User> id);

        Result<Domain.UserInfo> updateUserInfo(Domain.UserInfo userInfo);

        Domain.UserInfo upsertUserInfo(Domain.UserInfo userInfo);
    }

    interface LibraryInfo extends IRepo {
        Result<Domain.LibraryInfo> fetchLibraryInfo(UUID2<Library> id);

        Result<Domain.LibraryInfo> updateLibraryInfo(Domain.LibraryInfo libraryInfo);

        Result<Domain.LibraryInfo> upsertLibraryInfo(Domain.LibraryInfo libraryInfo);
    }
}
