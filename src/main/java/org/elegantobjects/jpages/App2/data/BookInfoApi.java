package org.elegantobjects.jpages.App2.data;

import org.elegantobjects.jpages.App2.core.Result;
import org.elegantobjects.jpages.App2.core.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.core.HttpClient;
import org.elegantobjects.jpages.App2.data.core.InMemoryAPI;
import org.elegantobjects.jpages.App2.data.core.URL;
import org.elegantobjects.jpages.App2.domain.Book;

import java.util.HashMap;
import java.util.Map;

// Note: Use Domain-specific language to define the API
public class BookInfoApi {
    private final InMemoryAPI<Book, DTO.BookInfo> api;

    public BookInfoApi() {
        this(new InMemoryAPI<>(new URL("memory://api.book.com"), new HttpClient()));
    }
    BookInfoApi(InMemoryAPI<Book, DTO.BookInfo> api) {
        this.api = api;
    }

    public Result<DTO.BookInfo> getBookInfo(String id) {
        return api.getDtoInfo(id);
    }
    public Result<DTO.BookInfo> getBookInfo(UUID2<Book> id) {
        return api.getDtoInfo(id);
    }
    public Result<DTO.BookInfo> addBookInfo(DTO.BookInfo bookInfo) {
        return api.addDtoInfo(bookInfo);
    }
    public Result<DTO.BookInfo> updateBookInfo(DTO.BookInfo bookInfo) {
        return api.updateDtoInfo(bookInfo);
    }
    public Result<DTO.BookInfo> upsertBookInfo(DTO.BookInfo bookInfo) {
        return api.upsertDtoInfo(bookInfo);
    }
    public Result<DTO.BookInfo> deleteBookInfo(DTO.BookInfo bookInfo) {
        return api.deleteDtoInfo(bookInfo);
    }

    public Map<UUID2<Book>, DTO.BookInfo> getAllBookInfos() {
        return new HashMap<>(api.getAllDtoInfos());
    }
}
