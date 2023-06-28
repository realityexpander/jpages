package org.elegantobjects.jpages.App2.domain.library;

import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.account.AccountInfo;
import org.elegantobjects.jpages.App2.domain.user.User;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.common.IRole;
import org.elegantobjects.jpages.App2.domain.Context;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

// Library Domain Object - *ONLY* interacts with its own Repo, Context, and other Domain Objects
public class Library extends IRole<LibraryInfo> implements IUUID2 {
    public final UUID2<Library> id;
    private final LibraryInfoRepo repo;

    public Library(
        @NotNull LibraryInfo info,
        Context context
    ) {
        super(info, context);
        this.repo = this.context.libraryInfoRepo();
        this.id = info.id();

        context.log.d(this,"Library (" + this.id + ") created from Info");
    }
    public Library(
        String json,
        Class<LibraryInfo> clazz,
        Context context
    ) {
        super(json, clazz, context);
        this.repo = this.context.libraryInfoRepo();
        this.id = this.info.id();

        context.log.d(this,"Library (" + this.id + ") created from Json with class: " + clazz.getName());
    }
    public Library(
        @NotNull UUID2<Library> id,
        Context context
    ) {
        super(id, context);
        this.repo = this.context.libraryInfoRepo();
        this.id = id;

        context.log.d(this,"Library (" + this.id + ") created using id with no Info");
    }
    public Library(String json, Context context) { this(json, LibraryInfo.class, context); }
    public Library(Context context) {
        this(UUID2.randomUUID2(), context);
    }
    // LEAVE for reference, for static Context instance implementation
    // Library() {
    //     this(UUID2.randomUUID());
    // }

    /////////////////////////////////////
    // IRole/UUID2 Required Overrides  //
    /////////////////////////////////////

    @Override
    public Result<LibraryInfo> fetchInfoResult() {
        // context.log.d(this,"Library (" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging

        infoResult = this.repo.fetchLibraryInfo(this.id);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<LibraryInfo>) infoResult).value();

        return infoResult;
    }

    @Override
    public Result<LibraryInfo> updateInfo(LibraryInfo updatedInfo) {
        // context.log.d(this,"Library (" + this.id.toString() + ") - updateInfo, newInfo: " + newInfo.toString());  // LEAVE for debugging

        // Update self optimistically
        super.updateInfo(updatedInfo);

        // Update the Repo
        Result<LibraryInfo> infoResult = this.repo.updateLibraryInfo(updatedInfo);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        // Update self with Repo result
        super.updateInfo(((Result.Success<LibraryInfo>) infoResult).value());
        return infoResult;
    }

    @Override
    public String getUUID2TypeStr() {
//        return this.getClass().getName();
        //        return UUID2.getUUID2TypeStr(Library.class);
        return UUID2.getUUID2TypeStr(this.getClass()); // todo test does this work?
    }


    ///////////////////////////////////////////
    // Library Domain Business Logic Methods //
    // - Methods to modify it's LibraryInfo  //
    ///////////////////////////////////////////

    public Result<Book> checkOutBookToUser(Book book, User user) {
        context.log.d(this, format("Library (%s) - checkOutBookToUser, userId: %s, bookId: %s", this.id, book.id, user.id));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        if (isUnableToFindOrAddUser(user)) {
            return new Result.Failure<>(new Exception("User is not known, userId: " + user.id));
        }

        if (!user.isAccountActive()) {
            return new Result.Failure<>(new Exception("User Account is not active, userId: " + user.id));
        }

        if (user.hasReachedMaxNumAcceptedBooks()) {
            return new Result.Failure<>(new Exception("User has reached max num Books accepted, userId: " + user.id));
        }

        // Get the User's AccountInfo object
        AccountInfo userAccountInfo = user.getUserAccountInfo();
        if (userAccountInfo == null) {
            return new Result.Failure<>(new Exception("User AccountInfo is null, userId: " + user.id));
        }

        // Check user fines are not exceeded
        if (userAccountInfo.isMaxFineExceeded()) {
            return new Result.Failure<>(new Exception("User has exceeded maximum fines, userId: " + user.id));
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
        Result<LibraryInfo> updateInfoResult = this.updateInfo(this.info);
        if (updateInfoResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<LibraryInfo>) updateInfoResult).exception());
        }

        return new Result.Success<>(book);
    }

    public Result<Book> checkInBookFromUser(Book book, User user) {
        context.log.d(this, format("Library (%s) - checkInBookFromUser, bookId %s from userID %s\n", this.id, book.id, user.id));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        if (isUnableToFindOrAddUser(user)) {
            return new Result.Failure<>(new Exception("User is not known, id: " + user.id));
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
        Result<LibraryInfo> updateInfoResult = this.updateInfo(this.info);
        if (updateInfoResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<LibraryInfo>) updateInfoResult).exception());
        }

        return new Result.Success<>(book);
    }

    /////////////////////////////////
    // Published Helper Methods    //
    /////////////////////////////////

    // This Library DomainObject enforces the rule: if a User is not known, they are added as a new user.
    public boolean isUnableToFindOrAddUser(User user) {
        context.log.d(this, format("Library (%s) for user: %s", this.id, user.id));
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

    /////////////////////////////////////////
    // Published Domain Reporting Methods  //
    /////////////////////////////////////////

    public Result<ArrayList<Book>> findBooksCheckedOutByUser(User user) {
        context.log.d(this, format("Library (%s) User id: %s\n", this.id, user));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Make sure User is Known
        if (isUnableToFindOrAddUser(user)) {
            return new Result.Failure<>(new Exception("User is not known, userId: " + user.id));
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

    /////////////////////////////////////////
    // Published Testing Helper Methods    //
    /////////////////////////////////////////

    public Result<Book> addTestBookToLibrary(Book book, Integer count) {
        context.log.d(this, format("Library (%s) book: %s, count: %s", this.id, book, count));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        Result<UUID2<Book>> addBookResult =  this.info.addTestBook(book.id, count);
        if (addBookResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<UUID2<Book>>) addBookResult).exception());
        }

        // Update the Info
        Result<LibraryInfo> updateInfoResult = this.updateInfo(this.info);
        if (updateInfoResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<LibraryInfo>) updateInfoResult).exception());
        }

        return new Result.Success<>(book);
    }

    public void DumpDB(Context context) {
        context.log.d(this,"\nDumping Library DB:");
        context.log.d(this,this.toJson());
        context.log.d(this,"\n");
    }
}