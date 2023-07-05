package org.elegantobjects.jpages.App2.domain.user;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.log.ILog;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.common.Repo;
import org.jetbrains.annotations.NotNull;

// Holds User info for all users in the system (simple CRUD operations)
public class UserInfoRepo extends Repo implements IUserInfoRepo {
    // Simulate a database on a server somewhere
    private final UUID2.HashMap<UUID2<User>, UserInfo> database = new UUID2.HashMap<>();

    public UserInfoRepo(ILog log) {
        super(log);
    }

    @Override
    public Result<UserInfo> fetchUserInfo(UUID2<User> id) {
        log.d(this, "userId: " + id);

        // Simulate network/database
        if (database.containsKey(id)) {
            return new Result.Success<>(database.get(id));
        }

        return new Result.Failure<>(new Exception("Repo.UserInfo, UserInfo not found, id:" + id));
    }

    @Override
    public Result<UserInfo> updateUserInfo(@NotNull UserInfo userInfo) {
        String method = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.d(this, method + ", userId:" + userInfo.id());

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
