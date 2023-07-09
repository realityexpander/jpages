package org.elegantobjects.jpages.LibraryApp.domain.book.data.network;

import org.elegantobjects.jpages.LibraryApp.common.util.Result;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.data.network.HttpClient;
import org.elegantobjects.jpages.LibraryApp.data.network.InMemoryAPI;
import org.elegantobjects.jpages.LibraryApp.data.network.URL;
import org.elegantobjects.jpages.LibraryApp.domain.book.Book;

import java.util.HashMap;
import java.util.Map;

/**
 * BookInfoApi encapsulates an in-memory API database simulation for the DTOBookInfo.<br>
 * <br>
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

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
