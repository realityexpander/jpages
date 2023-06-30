package org.elegantobjects.jpages.App2.domain.account;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.account.Account;
import org.elegantobjects.jpages.App2.domain.account.AccountInfo;
import org.elegantobjects.jpages.App2.domain.common.IRepo;

public interface IAccountInfoRepo extends IRepo {
    Result<AccountInfo> fetchAccountInfo(UUID2<Account> id);
    Result<AccountInfo> updateAccountInfo(AccountInfo accountInfo);
    Result<AccountInfo> upsertAccountInfo(AccountInfo accountInfo);
}
