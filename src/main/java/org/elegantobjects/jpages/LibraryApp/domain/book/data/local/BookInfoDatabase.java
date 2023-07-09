package org.elegantobjects.jpages.LibraryApp.domain.book.data.local;

import org.elegantobjects.jpages.LibraryApp.common.util.Result;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.common.data.local.IDatabase;
import org.elegantobjects.jpages.LibraryApp.common.data.local.InMemoryDatabase;
import org.elegantobjects.jpages.LibraryApp.common.data.network.URL;
import org.elegantobjects.jpages.LibraryApp.domain.book.Book;

import java.util.Map;

/**
 * BookInfoDatabase
 * <p>
 * This class uses domain specific language and wraps an implementation of the IDatabase interface
 * for EntityBookInfo.
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

public class BookInfoDatabase {
    private final IDatabase<Book, EntityBookInfo> database;

    public
    BookInfoDatabase(IDatabase<Book, EntityBookInfo> database) {
        this.database = database;
    }
    public
    BookInfoDatabase() {
        this(new InMemoryDatabase<>(new URL("memory://db.book.com"), "user", "password"));
    }

    public Result<EntityBookInfo> getBookInfo(UUID2<Book> id) {
        return database.getEntityInfo(id);
    }
    public Result<EntityBookInfo> updateBookInfo(EntityBookInfo bookInfo) {
        return database.updateEntityInfo(bookInfo);
    }
    public Result<EntityBookInfo> addBookInfo(EntityBookInfo bookInfo) {
        return database.addEntityInfo(bookInfo);
    }
    public Result<EntityBookInfo> upsertBookInfo(EntityBookInfo bookInfo) {
        return database.upsertEntityInfo(bookInfo);
    }
    public Result<EntityBookInfo> deleteBookInfo(EntityBookInfo bookInfo) {
        return database.deleteEntityInfo(bookInfo);
    }
    public Map<UUID2<Book>, EntityBookInfo> getAllBookInfos() {
        return database.getAllEntityInfo();
    }
}