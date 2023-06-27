package org.elegantobjects.jpages.App2.data.book.local;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.common.local.IDatabase;
import org.elegantobjects.jpages.App2.data.common.local.InMemoryDatabase;
import org.elegantobjects.jpages.App2.data.common.network.URL;
import org.elegantobjects.jpages.App2.domain.book.Book;

import java.util.Map;

public class BookInfoDatabase {
    private final IDatabase<Book, BookInfoEntity> database;

    BookInfoDatabase(IDatabase<Book, BookInfoEntity> database) {
        this.database = database;
    }
    public BookInfoDatabase() {
        this(new InMemoryDatabase<>(new URL("memory://db.book.com"), "user", "password"));
    }

    public Result<BookInfoEntity> getBookInfo(UUID2<Book> id) {
        return database.getEntityInfo(id);
    }

    public Result<BookInfoEntity> updateBookInfo(BookInfoEntity bookInfo) {
        return database.updateEntityInfo(bookInfo);
    }

    public Result<BookInfoEntity> addBookInfo(BookInfoEntity bookInfo) {
        return database.addEntityInfo(bookInfo);
    }

    public Result<BookInfoEntity> upsertBookInfo(BookInfoEntity bookInfo) {
        return database.upsertEntityInfo(bookInfo);
    }

    public Result<BookInfoEntity> deleteBookInfo(BookInfoEntity bookInfo) {
        return database.deleteEntityInfo(bookInfo);
    }

    public Map<UUID2<Book>, BookInfoEntity> getAllBookInfos() {  // todo UUID2 keep
        return database.getAllEntityInfo();
    }
}