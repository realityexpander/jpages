package org.elegantobjects.jpages.LibraryApp.domain.user.data;

import org.elegantobjects.jpages.LibraryApp.common.util.Result;
import org.elegantobjects.jpages.LibraryApp.common.util.log.ILog;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.domain.common.data.repo.Repo;
import org.elegantobjects.jpages.LibraryApp.domain.user.User;
import org.jetbrains.annotations.NotNull;

/**
 * UserInfoRepo is a repository for UserInfo objects.<br>
 * <br>
 * Holds User info for all the users in the system (simple CRUD operations).<br>
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

public class UserInfoRepo extends Repo implements IUserInfoRepo {
    // Simulate a database on a server somewhere
    private final UUID2.HashMap<UUID2<User>, UserInfo> database = new UUID2.HashMap<>();

    public
    UserInfoRepo(@NotNull ILog log) {
        super(log);
    }

    @Override
    public Result<UserInfo> fetchUserInfo(@NotNull UUID2<User> id) {
        log.d(this, "userId: " + id);

        // Simulate network/database
        if (database.containsKey(id)) {
            return new Result.Success<>(database.get(id));
        }

        return new Result.Failure<>(new Exception("Repo.UserInfo, UserInfo not found, id:" + id));
    }

    @Override
    public Result<UserInfo> updateUserInfo(@NotNull UserInfo userInfo) {
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.d(this, methodName + ", userId:" + userInfo.id());

        // Simulate network/database
        if (database.containsKey(userInfo.id())) {
            database.put(userInfo.id(), userInfo);
            return new Result.Success<>(userInfo);
        }

        return new Result.Failure<>(new Exception("Repo.UserInfo, UserInfo not found, id:" + userInfo.id()));
    }

    @Override
    public Result<UserInfo> upsertUserInfo(@NotNull UserInfo userInfo) {
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.d(this, methodName + ", userId:" + userInfo.id());

        // Simulate network/database
        database.put(userInfo.id(), userInfo);

        return new Result.Success<>(userInfo);
    }

}
