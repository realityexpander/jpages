package org.elegantobjects.jpages.App2.domain.common;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.*;
import org.elegantobjects.jpages.App2.domain.repoData.DomainBookInfo;
import org.elegantobjects.jpages.App2.domain.repoData.DomainLibraryInfo;
import org.elegantobjects.jpages.App2.domain.repoData.DomainUserInfo;

// Repo only accepts/returns Domain Models, and internally converts to/from DTOs/Entities/Domains
// - works with the network API & local database to perform CRUD operations, and also performs validation.
// - can also be used to implement caching.
// The Repo can easily accept fake APIs & Database for testing.
public interface IRepo {
    interface BookInfo extends IRepo {
        Result<DomainBookInfo> fetchBookInfo(UUID2<Book> id);

        Result<DomainBookInfo> addBookInfo(DomainBookInfo bookInfo);

        Result<DomainBookInfo> updateBookInfo(DomainBookInfo bookInfo);

        Result<DomainBookInfo> upsertBookInfo(DomainBookInfo bookInfo);
    }

    interface UserInfo extends IRepo {
        Result<DomainUserInfo> fetchUserInfo(UUID2<User> id);

        Result<DomainUserInfo> updateUserInfo(DomainUserInfo userInfo);

        DomainUserInfo upsertUserInfo(DomainUserInfo userInfo);
    }

    interface LibraryInfo extends IRepo {
        Result<DomainLibraryInfo> fetchLibraryInfo(UUID2<Library> id);

        Result<DomainLibraryInfo> updateLibraryInfo(DomainLibraryInfo libraryInfo);

        Result<DomainLibraryInfo> upsertLibraryInfo(DomainLibraryInfo libraryInfo);
    }
}
