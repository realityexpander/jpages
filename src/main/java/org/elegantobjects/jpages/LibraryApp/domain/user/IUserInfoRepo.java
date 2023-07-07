package org.elegantobjects.jpages.LibraryApp.domain.user;

import org.elegantobjects.jpages.LibraryApp.common.util.Result;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.domain.common.IRepo;
import org.jetbrains.annotations.NotNull;

public interface IUserInfoRepo extends IRepo {
    Result<UserInfo> fetchUserInfo(@NotNull UUID2<User> id);
    Result<UserInfo> updateUserInfo(@NotNull UserInfo userInfo);
    Result<UserInfo> upsertUserInfo(@NotNull UserInfo userInfo);
}
