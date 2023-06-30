package org.elegantobjects.jpages.App2.domain.book;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.book.BookInfo;
import org.elegantobjects.jpages.App2.domain.common.IRepo;

public interface IBookInfoRepo extends IRepo {
    Result<BookInfo> fetchBookInfo(UUID2<Book> id);
    Result<BookInfo> addBookInfo(BookInfo bookInfo);
    Result<BookInfo> updateBookInfo(BookInfo bookInfo);
    Result<BookInfo> upsertBookInfo(BookInfo bookInfo);
}
