package org.elegantobjects.jpages.App2;

import org.elegantobjects.jpages.App2.domain.Book;

import java.util.Map;

class BookInfoDatabase {
    private final IDatabase<Book, Model.Entity.BookInfo> database;

    BookInfoDatabase(IDatabase<Book, Model.Entity.BookInfo> database) {
        this.database = database;
    }
    BookInfoDatabase() {
        this(new InMemoryDatabase<>(new URL("memory://db.book.com"), "user", "password"));
    }

    public Result<Model.Entity.BookInfo> getBookInfo(UUID2<Book> id) {
        return database.getEntityInfo(id);
    }

    public Result<Model.Entity.BookInfo> updateBookInfo(Model.Entity.BookInfo bookInfo) {
        return database.updateEntityInfo(bookInfo);
    }

    public Result<Model.Entity.BookInfo> addBookInfo(Model.Entity.BookInfo bookInfo) {
        return database.addEntityInfo(bookInfo);
    }

    public Result<Model.Entity.BookInfo> upsertBookInfo(Model.Entity.BookInfo bookInfo) {
        return database.upsertEntityInfo(bookInfo);
    }

    public Result<Model.Entity.BookInfo> deleteBookInfo(Model.Entity.BookInfo bookInfo) {
        return database.deleteEntityInfo(bookInfo);
    }

    public Map<UUID2<Book>, Model.Entity.BookInfo> getAllBookInfos() {  // todo UUID2 keep
        return database.getAllEntityInfo();
    }
}