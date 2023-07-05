package org.elegantobjects.jpages.App2.domain.common;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.account.Account;
import org.elegantobjects.jpages.App2.domain.account.AccountInfo;
import org.elegantobjects.jpages.App2.domain.library.LibraryInfo;
import org.elegantobjects.jpages.App2.domain.user.UserInfo;
import org.elegantobjects.jpages.App2.domain.library.Library;
import org.elegantobjects.jpages.App2.domain.user.User;

// Repo only accepts/returns Domain Models, and internally converts to/from DTOs/Entities/Domains
// - works with the network API & local database to perform CRUD operations, and also performs validation.
// - can also be used to implement caching.
// The Repo can easily accept fake APIs & Database for testing.
public interface IRepo {} // Marker interface for all `Repo` classes
