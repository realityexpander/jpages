package org.elegantobjects.jpages.App2.data;

import org.elegantobjects.jpages.App2.core.Result;
import org.elegantobjects.jpages.App2.core.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.core.IDatabase;
import org.elegantobjects.jpages.App2.data.core.InMemoryDatabase;
import org.elegantobjects.jpages.App2.data.core.URL;
import org.elegantobjects.jpages.App2.domain.Book;

import java.util.Map;

public class BookInfoDatabase {
    private final IDatabase<Book, Entity.BookInfo> database;

    BookInfoDatabase(IDatabase<Book, Entity.BookInfo> database) {
        this.database = database;
    }
    public BookInfoDatabase() {
        this(new InMemoryDatabase<>(new URL("memory://db.book.com"), "user", "password"));
    }

    public Result<Entity.BookInfo> getBookInfo(UUID2<Book> id) {
        return database.getEntityInfo(id);
    }

    public Result<Entity.BookInfo> updateBookInfo(Entity.BookInfo bookInfo) {
        return database.updateEntityInfo(bookInfo);
    }

    public Result<Entity.BookInfo> addBookInfo(Entity.BookInfo bookInfo) {
        return database.addEntityInfo(bookInfo);
    }

    public Result<Entity.BookInfo> upsertBookInfo(Entity.BookInfo bookInfo) {
        return database.upsertEntityInfo(bookInfo);
    }

    public Result<Entity.BookInfo> deleteBookInfo(Entity.BookInfo bookInfo) {
        return database.deleteEntityInfo(bookInfo);
    }

    public Map<UUID2<Book>, Entity.BookInfo> getAllBookInfos() {  // todo UUID2 keep
        return database.getAllEntityInfo();
    }
}