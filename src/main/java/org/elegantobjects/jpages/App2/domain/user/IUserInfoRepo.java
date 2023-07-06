package org.elegantobjects.jpages.App2.domain.user;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.common.IRepo;
import org.elegantobjects.jpages.App2.domain.user.User;
import org.elegantobjects.jpages.App2.domain.user.UserInfo;
import org.jetbrains.annotations.NotNull;

public interface IUserInfoRepo extends IRepo {
    Result<UserInfo> fetchUserInfo(@NotNull UUID2<User> id);
    Result<UserInfo> updateUserInfo(@NotNull UserInfo userInfo);
    Result<UserInfo> upsertUserInfo(@NotNull UserInfo userInfo);
}
