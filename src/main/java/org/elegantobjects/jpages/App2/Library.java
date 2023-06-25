package org.elegantobjects.jpages.App2;

import org.elegantobjects.jpages.App2.domain.Book;
import org.elegantobjects.jpages.App2.domain.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

// Library Domain Object - *ONLY* interacts with its own Repo, Context, and other Domain Objects
public class Library extends IRole<Model.Domain.LibraryInfo> implements IUUID2 {
    public final UUID2<Library> id;
    private Repo.LibraryInfo repo = null;

    Library(Model.Domain.LibraryInfo info, Context context) {
        super(info, context);
        this.repo = this.context.libraryRepo();
        this.id = info.id;

        context.log.d(this,"Library (" + this.id + ") created");
    }
    Library(UUID2<Library> id, Context context) {
        super(id.toDomainUUID2(), context);
        this.repo = this.context.libraryRepo();
        this.id = id;

        context.log.d(this,"Library (" + this.id + ") created");
    }
    Library(String json, Class<Model.Domain.LibraryInfo> classType, Context context) {
        super(json, classType, context);
        this.repo = this.context.libraryRepo();
        this.id = this.info.id;
    }
    Library(String json, Context context) { this(json, Model.Domain.LibraryInfo.class, context); }
    Library(Context context) {
        super(UUID2.randomUUID2(), context);
        this.repo = this.context.libraryRepo();
        this.id = this.info.id;
    }

    // LEAVE for reference, for static Context instance implementation
    // Library() {
    //     this(UUID2.randomUUID());
    // }

    @Override
    public Result<Model.Domain.LibraryInfo> fetchInfoResult() {
        // context.log.d(this,"Library (" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging

        infoResult = this.repo.fetchLibraryInfo(this.id);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<Model.Domain.LibraryInfo>) infoResult).value();

        return infoResult;
    }

    @Override
    public Result<Model.Domain.LibraryInfo> updateInfo(Model.Domain.LibraryInfo updatedInfo) {
        // context.log.d(this,"Library (" + this.id.toString() + ") - updateInfo, newInfo: " + newInfo.toString());  // LEAVE for debugging

        // Update self optimistically
        super.updateInfo(updatedInfo);

        // Update the Repo
        Result<Model.Domain.LibraryInfo> infoResult = this.repo.updateLibraryInfo(updatedInfo);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        // Update self with Repo result
        super.updateInfo(((Result.Success<Model.Domain.LibraryInfo>) infoResult).value());
        return infoResult;
    }

    @Override
    public String getUUID2TypeStr() {
        return this.getClass().getName();
    }

    ///////////////////////////////////////////
    // Library Domain Business Logic Methods //
    ///////////////////////////////////////////

    public Result<Book> checkOutBookToUser(Book book, User user) {
        context.log.d(this, format("Library (%s) - checkOutBookToUser, user: %s, book: %s", this.id, this.id.toString(), this.id.toString()));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        if (isUnableToFindOrAddUser(user)) {
            return new Result.Failure<>(new Exception("User is not known, id: " + this.id));
        }

        // Check out Book to User
        Result<Book> checkOutBookresult = this.info.checkOutBookToUser(book, user);
        if (checkOutBookresult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Book>) checkOutBookresult).exception());
        }

        // User receives Book
        Result<ArrayList<Book>> receiveBookResult = user.acceptBook(book);
        if (receiveBookResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<ArrayList<Book>>) receiveBookResult).exception());
        }

        // Update the Info
        Result<Model.Domain.LibraryInfo> updateInfoResult = this.updateInfo(this.info);
        if (updateInfoResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Model.Domain.LibraryInfo>) updateInfoResult).exception());
        }

        return new Result.Success<>(book);
    }

    public Result<Book> checkInBookFromUser(Book book, User user) {
        context.log.d(this, format("Library (%s) - checkInBookFromUser, book %s from user %s\n", this.id, this.id, this.id));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        if (isUnableToFindOrAddUser(user)) {
            return new Result.Failure<>(new Exception("User is not known, id: " + this.id));
        }

        Result<Book> checkInBookResult = this.info.checkInBookFromUser(book, user);
        if (checkInBookResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Book>) checkInBookResult).exception());
        }

        Result<ArrayList<UUID2<Book>>> userReturnedBookResult = user.unacceptBook(book);
        if (userReturnedBookResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<ArrayList<UUID2<Book>>>) userReturnedBookResult).exception());
        }

        // Update the Info
        Result<Model.Domain.LibraryInfo> updateInfoResult = this.updateInfo(this.info);
        if (updateInfoResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Model.Domain.LibraryInfo>) updateInfoResult).exception());
        }

        return new Result.Success<>(book);
    }

    // This Library DomainObject enforces the rule: if a User is not known, they are added as a new user.
    public boolean isUnableToFindOrAddUser(User user) {
        context.log.d(this, format("Library (%s) user: %s", this.id, this.id));
        if (fetchInfoFailureReason() != null) return true;

        if (isKnownUser(user)) {
            return false;
        }

        // Create a new User entry in the Library
        Result<UUID2<User>> addRegisteredUserResult = this.info.registerUser(user.id);
        //noinspection RedundantIfStatement
        if (addRegisteredUserResult instanceof Result.Failure) {
            return true;
        }

        return false;
    }

    public boolean isKnownBook(Book book) {
        context.log.d(this, format("Library(%s) Book id: %s\n", this.id, book.id));
        if (fetchInfoFailureReason() != null) return false;

        return this.info.isBookIdKnown(book);
    }

    public boolean isKnownUser(User user) {
        context.log.d(this, format("Library (%s) User id: %s", this.id, user.id));
        if (fetchInfoFailureReason() != null) return false;

        return this.info.isUserIdKnown(user);
    }

    public boolean isBookAvailable(Book book) {
        context.log.d(this, format("Library (%s) Book id: %s\n", this.id, book.id));
        if (fetchInfoFailureReason() != null) return false;

        return this.info.isBookIdAvailable(book);
    }

    public Result<ArrayList<Book>> findBooksCheckedOutByUser(User user) {
        context.log.d(this, format("Library (%s) User id: %s\n", this.id, user));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Make sure User is Known
        if (isUnableToFindOrAddUser(user)) {
            return new Result.Failure<>(new Exception("User is not known, id: " + this.id));
        }

        Result<ArrayList<UUID2<Book>>> entriesResult = this.info.findBooksCheckedOutByUserId(user.id);
        if (entriesResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<ArrayList<UUID2<Book>>>) entriesResult).exception());
        }

        // Convert UUID2<Books to Books
        ArrayList<UUID2<Book>> bookIds = ((Result.Success<ArrayList<UUID2<Book>>>) entriesResult).value();
        ArrayList<Book> books = new ArrayList<>();
        for (UUID2<Book> entry : bookIds) {
            books.add(new Book(entry, context));
        }

        return new Result.Success<>(books);
    }

    public Result<HashMap<Book, Integer>> calculateAvailableBookIdToNumberAvailableList() {
        context.log.d(this, "Library (" + this.id + ")");
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        Result<HashMap<UUID2<Book>, Integer>> entriesResult = this.info.calculateAvailableBookIdToCountOfAvailableBooksList();
        if (entriesResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<HashMap<UUID2<Book>, Integer>>) entriesResult).exception());
        }

        // Convert list of UUID2<Book> to list of Book
        // Note: the BookInfo is not fetched, so the Book only contains the id. This is by design.
        HashMap<UUID2<Book>, Integer> bookIdToNumberAvailable = ((Result.Success<HashMap<UUID2<Book>, Integer>>) entriesResult).value();
        HashMap<Book, Integer> bookToNumberAvailable = new HashMap<>();
        for (Map.Entry<UUID2<Book>, Integer> entry : bookIdToNumberAvailable.entrySet()) {
            bookToNumberAvailable.put(new Book(entry.getKey(), context), entry.getValue());
        }

        return new Result.Success<>(bookToNumberAvailable);
    }

    public Result<Book> addTestBookToLibrary(Book book, Integer count) {
        context.log.d(this, format("Library (%s) book: %s, count: %s", this.id, book, count));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        Result<UUID2<Book>> addBookResult =  this.info.addTestBook(book.id, count);
        if (addBookResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<UUID2<Book>>) addBookResult).exception());
        }

        // Update the Info
        Result<Model.Domain.LibraryInfo> updateInfoResult = this.updateInfo(this.info);
        if (updateInfoResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Model.Domain.LibraryInfo>) updateInfoResult).exception());
        }

        return new Result.Success<>(book);
    }

    public void DumpDB(Context context) {
        context.log.d(this,"\nDumping Library DB:");
        context.log.d(this,this.toJson());
        context.log.d(this,"\n");
    }
}