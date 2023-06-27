package org.elegantobjects.jpages.App2.domain.user;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.log.ILog;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.user.User;
import org.elegantobjects.jpages.App2.domain.common.IRepo;
import org.elegantobjects.jpages.App2.domain.common.Repo;
import org.elegantobjects.jpages.App2.domain.user.DomainUserInfo;

// Holds User info for all users in the system (simple CRUD operations)
public class UserInfoRepo extends Repo implements IRepo.UserInfo {
    // Simulate a database on a server somewhere
    private final UUID2.HashMap<User, DomainUserInfo> database = new UUID2.HashMap<>();

    public UserInfoRepo(ILog log) {
        super(log);
    }

    @Override
    public Result<DomainUserInfo> fetchUserInfo(UUID2<User> id) {
        log.d(this, "Repo.UserInfo - Fetching user info: " + id);

        // Simulate network/database
        if (database.containsKey(id)) {
            return new Result.Success<>(database.get(id));
        }

        return new Result.Failure<>(new Exception("Repo.UserInfo, UserInfo not found, id:" + id));
    }

    @Override
    public Result<DomainUserInfo> updateUserInfo(DomainUserInfo userInfo) {
        String method = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.d(this, "Repo.User - " + method + " - Updating user info: " + userInfo);

        // Simulate network/database
        if (database.containsKey(userInfo.id())) {
            database.put(userInfo.id(), userInfo);
            return new Result.Success<>(userInfo);
        }

        return new Result.Failure<>(new Exception("Repo.UserInfo, UserInfo not found, id:" + userInfo.id()));
    }

    @Override
    public DomainUserInfo upsertUserInfo(DomainUserInfo userInfo) {
        log.d(this, "Repo.UserInfo - Upserting user info: " + userInfo);

        database.put(userInfo.id(), userInfo);
        return userInfo;
    }

}
