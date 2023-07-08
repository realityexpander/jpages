package org.elegantobjects.jpages.LibraryApp.domain.account;

import org.elegantobjects.jpages.LibraryApp.common.util.Result;
import org.elegantobjects.jpages.LibraryApp.common.util.log.ILog;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.domain.common.Repo;
import org.jetbrains.annotations.NotNull;

// Holds Account info for all the users in the system (simple CRUD operations)
public class AccountInfoRepo extends Repo implements IAccountInfoRepo {

    // simulate a local database on server (UUID2<Account> is the key)
    private final UUID2.HashMap<UUID2<Account>, AccountInfo> database = new UUID2.HashMap<>();

    public
    AccountInfoRepo(@NotNull ILog log) {
        super(log);
    }

    public Result<AccountInfo> fetchAccountInfo(UUID2<Account> id) {
        log.d(this, "id: " + id);

        // Simulate network/database
        if (database.containsKey(id)) {
            return new Result.Success<>(database.get(id));
        }

        return new Result.Failure<>(new Exception("Repo.AccountInfo, account not found, id: " + id));
    }

    @Override
    public Result<AccountInfo> updateAccountInfo(@NotNull AccountInfo accountInfo) {
        log.d(this, "accountInfo.id: " + accountInfo.id());

        // Simulate network/database
        if (database.containsKey(accountInfo.id())) {
            database.put(accountInfo.id(), accountInfo);

            return new Result.Success<>(accountInfo);
        }

        return new Result.Failure<>(new Exception("Repo.AccountInfo, account not found, id: " + accountInfo.id()));
    }

    @Override
    public Result<AccountInfo> upsertAccountInfo(@NotNull AccountInfo accountInfo) {
        log.d(this, "accountInfo.id(): " + accountInfo.id());

        // Simulate network/database
        database.put(accountInfo.id(), accountInfo);

        return new Result.Success<>(accountInfo);
    }
}