package org.elegantobjects.jpages.App2.domain.common;

import com.google.gson.Gson;
import org.elegantobjects.jpages.App2.common.util.log.ILog;
import org.elegantobjects.jpages.App2.domain.Repo;

// Context is a singleton class that holds all the repositories and global objects like Gson
public interface IContext {
    Repo.BookInfo bookInfoRepo = null;
    Repo.UserInfo userInfoRepo = null;
    Repo.LibraryInfo libraryInfoRepo = null;
    Gson gson = null;
    ILog log = null;
}
