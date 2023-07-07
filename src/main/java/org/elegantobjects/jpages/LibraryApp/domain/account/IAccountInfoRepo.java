package org.elegantobjects.jpages.LibraryApp.domain.account;

import org.elegantobjects.jpages.LibraryApp.common.util.Result;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.domain.common.IRepo;

public interface IAccountInfoRepo extends IRepo {
    Result<AccountInfo> fetchAccountInfo(UUID2<Account> id);
    Result<AccountInfo> updateAccountInfo(AccountInfo accountInfo);
    Result<AccountInfo> upsertAccountInfo(AccountInfo accountInfo);
}
