package org.elegantobjects.jpages.LibraryApp.domain.user;

import org.elegantobjects.jpages.LibraryApp.common.util.Result;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.domain.common.IRepo;
import org.jetbrains.annotations.NotNull;

/**
 * IUserInfoRepo is an interface for the UserInfoRepo class.<br>
 * <br>
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

public interface IUserInfoRepo extends IRepo {
    Result<UserInfo> fetchUserInfo(@NotNull UUID2<User> id);
    Result<UserInfo> updateUserInfo(@NotNull UserInfo userInfo);
    Result<UserInfo> upsertUserInfo(@NotNull UserInfo userInfo);
}
