package org.elegantobjects.jpages.App2.domain.account;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.log.ILog;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.common.IRepo;
import org.elegantobjects.jpages.App2.domain.common.Repo;

// Holds Account info for all the users in the system (simple CRUD operations)
public class AccountInfoRepo extends Repo implements IRepo.AccountInfoRepo {

    // simulate a local database on server (UUID2<Account> is the key)
//    private final UUID2.HashMap<Account, AccountInfo> database = new UUID2.HashMap<>(Account.class);
    private final UUID2.HashMap<Account, AccountInfo> database = new UUID2.HashMap<>();

    public AccountInfoRepo(ILog log) {
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
    public Result<AccountInfo> updateAccountInfo(AccountInfo accountInfo) {
        log.d(this, "accountInfo.id: " + accountInfo.id);

        // Simulate network/database
        if (database.containsKey(accountInfo.id())) {
            database.put(accountInfo.id(), accountInfo);

            return new Result.Success<>(accountInfo);
        }

        return new Result.Failure<>(new Exception("Repo.AccountInfo, account not found, id: " + accountInfo.id()));
    }

    @Override
    public Result<AccountInfo> upsertAccountInfo(AccountInfo accountInfo) {
        log.d(this, "accountInfo.id: " + accountInfo.id);

        // Simulate network/database
        database.put(accountInfo.id(), accountInfo);

        return new Result.Success<>(accountInfo);
    }

    ///////////////////////////////////
    /// Published Helper methods    ///
    ///////////////////////////////////

    public void populateWithFakeAuditMessages(UUID2<Account> accountId, int numberOfMessagesToCreate) {
        log.d(this, "accountId: " + accountId + ", numberOfAccountsToCreate: " + numberOfMessagesToCreate);
        AccountInfo account = database.get(accountId);

        for (int i = 0; i < numberOfMessagesToCreate; i++) {
            account.addTestAuditLogMessage(
                "Test Audit message " + i + " for account: " + account.id()
            );
        }
    }

}