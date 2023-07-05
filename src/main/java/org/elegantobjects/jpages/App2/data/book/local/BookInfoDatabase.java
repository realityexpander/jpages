package org.elegantobjects.jpages.App2.data.book.local;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.common.local.IDatabase;
import org.elegantobjects.jpages.App2.data.common.local.InMemoryDatabase;
import org.elegantobjects.jpages.App2.data.common.network.URL;
import org.elegantobjects.jpages.App2.domain.book.Book;

import java.util.Map;

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
    public Map<UUID2<Book>, EntityBookInfo> getAllBookInfos() {  // todo UUID2 keep
        return database.getAllEntityInfo();
    }
}