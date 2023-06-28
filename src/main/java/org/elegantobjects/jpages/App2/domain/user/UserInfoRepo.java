package org.elegantobjects.jpages.App2.domain.user;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.log.ILog;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.common.IRepo;
import org.elegantobjects.jpages.App2.domain.common.Repo;

// Holds User info for all users in the system (simple CRUD operations)
public class UserInfoRepo extends Repo implements IRepo.UserInfoRepo {
    // Simulate a database on a server somewhere
//    private final UUID2.HashMap<User, UserInfo> database = new UUID2.HashMap<>(User.class);
    private final UUID2.HashMap<User, UserInfo> database = new UUID2.HashMap<>();

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
    public Result<UserInfo> updateUserInfo(UserInfo userInfo) {
        String method = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.d(this, method + ", userId:" + userInfo.id);

        // Simulate network/database
        if (database.containsKey(userInfo.id())) {
            database.put(userInfo.id(), userInfo);
            return new Result.Success<>(userInfo);
        }

        return new Result.Failure<>(new Exception("Repo.UserInfo, UserInfo not found, id:" + userInfo.id()));
    }

    @Override
    public Result<UserInfo> upsertUserInfo(UserInfo userInfo) {
        String method = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.d(this, method + ", userId:" + userInfo.id);

        // Simulate network/database
        database.put(userInfo.id(), userInfo);

        return new Result.Success<>(userInfo);
    }

}
