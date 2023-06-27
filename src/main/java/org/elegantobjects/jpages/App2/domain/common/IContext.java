package org.elegantobjects.jpages.App2.domain.common;

import com.google.gson.Gson;
import org.elegantobjects.jpages.App2.common.util.log.ILog;
import org.elegantobjects.jpages.App2.domain.book.BookInfoRepo;
import org.elegantobjects.jpages.App2.domain.library.LibraryInfoRepo;
import org.elegantobjects.jpages.App2.domain.user.UserInfoRepo;

// Context is a singleton class that holds all the repositories and global objects like Gson
public interface IContext {
    BookInfoRepo bookInfoRepo = null;
    UserInfoRepo userInfoRepo = null;
    LibraryInfoRepo libraryInfoRepo = null;
    Gson gson = null;
    ILog log = null;
}
