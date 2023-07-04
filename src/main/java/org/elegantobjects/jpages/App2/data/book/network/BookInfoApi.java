package org.elegantobjects.jpages.App2.data.book.network;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.common.network.HttpClient;
import org.elegantobjects.jpages.App2.data.common.network.InMemoryAPI;
import org.elegantobjects.jpages.App2.data.common.network.URL;
import org.elegantobjects.jpages.App2.domain.book.Book;

import java.util.HashMap;
import java.util.Map;

// Note: Use Domain-specific language to define the API
public class BookInfoApi {
    private final InMemoryAPI<Book, DTOBookInfo> api;

    public BookInfoApi() {
        this(new InMemoryAPI<>(new URL("memory://api.book.com"), new HttpClient()));
    }
    BookInfoApi(InMemoryAPI<Book, DTOBookInfo> api) {
        this.api = api;
    }

    public Result<DTOBookInfo> getBookInfo(String id) {
        return api.getDtoInfo(id);
    }
    public Result<DTOBookInfo> getBookInfo(UUID2<Book> id) {
        return api.getDtoInfo(id);
    }
    public Result<DTOBookInfo> addBookInfo(DTOBookInfo bookInfo) {
        return api.addDtoInfo(bookInfo);
    }
    public Result<DTOBookInfo> updateBookInfo(DTOBookInfo bookInfo) {
        return api.updateDtoInfo(bookInfo);
    }
    public Result<DTOBookInfo> upsertBookInfo(DTOBookInfo bookInfo) {
        return api.upsertDtoInfo(bookInfo);
    }
    public Result<DTOBookInfo> deleteBookInfo(DTOBookInfo bookInfo) {
        return api.deleteDtoInfo(bookInfo);
    }
    public Map<UUID2<Book>, DTOBookInfo> getAllBookInfos() {
        return new HashMap<>(api.getAllDtoInfos());
    }
}
