package org.elegantobjects.jpages.LibraryApp.domain.book;

import org.elegantobjects.jpages.LibraryApp.common.util.Result;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.domain.common.IRepo;
/**
 * IBookInfoRepo is an interface for the BookInfoRepo class.
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

public interface IBookInfoRepo extends IRepo {
    Result<BookInfo> fetchBookInfo(UUID2<Book> id);
    Result<BookInfo> addBookInfo(BookInfo bookInfo);
    Result<BookInfo> updateBookInfo(BookInfo bookInfo);
    Result<BookInfo> upsertBookInfo(BookInfo bookInfo);
}