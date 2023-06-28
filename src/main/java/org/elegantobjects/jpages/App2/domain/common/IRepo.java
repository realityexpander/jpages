package org.elegantobjects.jpages.App2.domain.common;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.account.Account;
import org.elegantobjects.jpages.App2.domain.account.AccountInfo;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.book.BookInfo;
import org.elegantobjects.jpages.App2.domain.library.LibraryInfo;
import org.elegantobjects.jpages.App2.domain.user.UserInfo;
import org.elegantobjects.jpages.App2.domain.library.Library;
import org.elegantobjects.jpages.App2.domain.user.User;

// Repo only accepts/returns Domain Models, and internally converts to/from DTOs/Entities/Domains
// - works with the network API & local database to perform CRUD operations, and also performs validation.
// - can also be used to implement caching.
// The Repo can easily accept fake APIs & Database for testing.
public interface IRepo {
    interface BookInfoRepo extends IRepo {
        Result<BookInfo> fetchBookInfo(UUID2<Book> id);

        Result<BookInfo> addBookInfo(BookInfo bookInfo);

        Result<BookInfo> updateBookInfo(BookInfo bookInfo);

        Result<BookInfo> upsertBookInfo(BookInfo bookInfo);
    }

    interface UserInfoRepo extends IRepo {
        Result<UserInfo> fetchUserInfo(UUID2<User> id);

        Result<UserInfo> updateUserInfo(UserInfo userInfo);

        Result<UserInfo> upsertUserInfo(UserInfo userInfo);
    }

    interface LibraryInfoRepo extends IRepo {
        Result<LibraryInfo> fetchLibraryInfo(UUID2<Library> id);

        Result<LibraryInfo> updateLibraryInfo(LibraryInfo libraryInfo);

        Result<LibraryInfo> upsertLibraryInfo(LibraryInfo libraryInfo);
    }

    interface AccountInfoRepo extends IRepo {
        Result<AccountInfo> fetchAccountInfo(UUID2<Account> id);

        Result<AccountInfo> updateAccountInfo(AccountInfo accountInfo);

        Result<AccountInfo> upsertAccountInfo(AccountInfo accountInfo);
    }
}
