package org.elegantobjects.jpages.App2.domain.library;

import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.account.AccountInfo;
import org.elegantobjects.jpages.App2.domain.user.User;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.common.Role;
import org.elegantobjects.jpages.App2.domain.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

// Library Role Object - *ONLY* interacts with its own Repo, Context, and other Role Objects
public class Library extends Role<LibraryInfo> implements IUUID2 {
    private final LibraryInfoRepo repo;

    public Library(
        @NotNull LibraryInfo info,
        @NotNull Context context
    ) {
        super(info, context);
        this.repo = this.context.libraryInfoRepo();

        context.log.d(this,"Library (" + this.id() + ") created from Info");
    }
    public Library(
        @NotNull String json,
        @NotNull Class<LibraryInfo> clazz,
        @NotNull Context context
    ) {
        super(json, clazz, context);
        this.repo = this.context.libraryInfoRepo();

        context.log.d(this,"Library (" + this.id() + ") created from Json with class: " + clazz.getName());
    }
    public Library(
        @NotNull UUID2<Library> id,
        @NotNull Context context
    ) {
        super(id, context);
        this.repo = this.context.libraryInfoRepo();

        context.log.d(this,"Library (" + this.id() + ") created using id with no Info");
    }
    public Library(@NotNull String json, @NotNull Context context) { this(json, LibraryInfo.class, context); }
    public Library(@NotNull Context context) {
        this(UUID2.randomUUID2(Library.class), context);
    }
    // LEAVE for reference, for static Context instance implementation
    // Library() {
    //     this(UUID2.randomUUID());
    // }

    /////////////////////////
    // Static constructors //
    /////////////////////////

    public static Result<Library> fetchLibrary(
        @NotNull UUID2<Library> uuid2,
        @NotNull Context context
    ) {
        LibraryInfoRepo repo = context.libraryInfoRepo();

        Result<LibraryInfo> infoResult = repo.fetchLibraryInfo(uuid2);
        if (infoResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<LibraryInfo>) infoResult).exception());
        }

        LibraryInfo info = ((Result.Success<LibraryInfo>) infoResult).value();
        return new Result.Success<>(new Library(info, context));
    }

    ////////////////////////////////
    // Published Simple Getters   //
    ////////////////////////////////

    // Convenience method to get the Type-safe id from the Class
    @Override @SuppressWarnings("unchecked")
    public UUID2<Library> id() {
        return (UUID2<Library>) super.id();
    }

    /////////////////////////////////////
    // Role/UUID2 Required Overrides  //
    /////////////////////////////////////

    @Override
    public Result<LibraryInfo> fetchInfoResult() {
        // context.log.d(this,"Library (" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging

        infoResult = this.repo.fetchLibraryInfo(this.id());
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<LibraryInfo>) infoResult).value();

        return infoResult;
    }

    @Override
    public Result<LibraryInfo> updateInfo(@NotNull LibraryInfo updatedInfo) {
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
    public String uuid2TypeStr() {
        // Get the Class Inheritance Path from the Class Path
        return UUID2.calcUUID2TypeStr(this.getClass());
    }

    ///////////////////////////////////////////
    // Library Role Business Logic Methods   //
    // - Methods to modify it's LibraryInfo  //
    // - Communicate with other ROle objects //
    ///////////////////////////////////////////

    public Result<Book> checkOutBookToUser(@NotNull Book book, @NotNull User user) {
        context.log.d(this, format("Library (%s) - userId: %s, bookId: %s", this.id(), book.id(), user.id()));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        if (isUnableToFindOrRegisterUser(user))
            return new Result.Failure<>(new Exception("User is not known, userId: " + user.id()));

        // Note: this calls a wrapper to the User's Account Role object
        if (!user.isAccountInGoodStanding())
            return new Result.Failure<>(new Exception("User Account is not active, userId: " + user.id()));

        // Note: this calls a wrapper to the User's Account Role object
        if (user.hasReachedMaxAmountOfAcceptedPublicLibraryBooks())
            return new Result.Failure<>(new Exception("User has reached max num Books accepted, userId: " + user.id()));

        if(user.hasAcceptedBook(book))
            return new Result.Failure<>(new Exception("User has already accepted this Book, userId: " + user.id() + ", bookId: " + book.id()));

        // Get User's AccountInfo object
        AccountInfo userAccountInfo = user.accountInfo();
        if (userAccountInfo == null)
            return new Result.Failure<>(new Exception("User AccountInfo is null, userId: " + user.id()));

        // Check User fines are not exceeded
        if (userAccountInfo.isMaxFineExceeded())
            return new Result.Failure<>(new Exception("User has exceeded maximum fines, userId: " + user.id()));

        // Check out Book to User
        Result<Book> checkOutBookresult = this.info.checkOutBookToUser(book, user);
        if (checkOutBookresult instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<Book>) checkOutBookresult).exception());

        // Update Info, since we modified data for this Library
        Result<LibraryInfo> updateInfoResult = this.updateInfo(this.info);
        if (updateInfoResult instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<LibraryInfo>) updateInfoResult).exception());

        return new Result.Success<>(book);
    }

    public Result<Book> checkInBookFromUser(@NotNull Book book, @NotNull User user) {
        context.log.d(this, format("Library (%s) - checkInBookFromUser, bookId %s from userID %s\n", this.id(), book.id(), user.id()));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        if (isUnableToFindOrRegisterUser(user)) return new Result.Failure<>(new Exception("User is not known, id: " + user.id()));

        Result<Book> checkInBookResult = this.info.checkInBookFromUser(book, user);
        if (checkInBookResult instanceof Result.Failure) return new Result.Failure<>(((Result.Failure<Book>) checkInBookResult).exception());

        // Update Info, since we modified data for this Library
        Result<LibraryInfo> updateInfoResult = this.updateInfo(this.info);
        if (updateInfoResult instanceof Result.Failure) return new Result.Failure<>(((Result.Failure<LibraryInfo>) updateInfoResult).exception());

        return new Result.Success<>(book);
    }

    public Result<Book> transferCheckedOutBookSourceLibraryToThisLibrary(@NotNull Book bookToTransfer, @NotNull User user) {
        context.log.d(this, format("Library (%s) - bookId %s, userId %s", this.id(), bookToTransfer.id(), user.id()));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Check in Book to current Source Library
        Result<Book> checkInBookResult = checkInBookFromUser(bookToTransfer, user);
        if (checkInBookResult instanceof Result.Failure) return new Result.Failure<>(((Result.Failure<Book>) checkInBookResult).exception());

        // Transfer Book to this Library
        Result<Book> transferBookResult = this.transferBookSourceLibraryToThisLibrary(bookToTransfer);

        // Check out Book to User from this Library
        Result<Book> checkOutBookResult = checkOutBookToUser(bookToTransfer, user);
        if (checkOutBookResult instanceof Result.Failure) return new Result.Failure<>(((Result.Failure<Book>) checkOutBookResult).exception());

        return new Result.Success<>(bookToTransfer);
    }

    // Note: this does not change the Checkout status of the User
    public Result<Book> transferBookSourceLibraryToThisLibrary(@NotNull Book bookToTransfer) {
        context.log.d(this, format("Library (%s) - bookId %s", this.id(), bookToTransfer.id()));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Get the Book's Source Library
        Library fromSourceLibrary = bookToTransfer.sourceLibrary();
        if (fromSourceLibrary == null)
            return new Result.Failure<>(new Exception("Book's Source Library is null, bookId: " + bookToTransfer.id()));

        // Check `from` Source Library is same as this Library
        if (fromSourceLibrary.id().equals(this.id()))
            return new Result.Failure<>(new Exception("Book's Source Library is the same as this Library, bookId: " + bookToTransfer.id()));

        // Check if `from` Source Library is known
        if (fromSourceLibrary.fetchInfoFailureReason() != null)
            return new Result.Failure<>(new Exception("Book's Source Library is not known, bookId: " + bookToTransfer.id()));

        // Check if Book is known at `from` Source Library
        if(!fromSourceLibrary.info.isBookKnown(bookToTransfer))
            return new Result.Failure<>(new Exception("Book is not known at from Source Library, bookId: " + bookToTransfer.id()));


        // Remove Book from Library Inventory of Books at `from` Source Library
        Result<UUID2<Book>> removeBookResult = fromSourceLibrary.info.removeTransferringBookFromInventory(bookToTransfer);
        if (removeBookResult instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<UUID2<Book>>) removeBookResult).exception());

        // Update `from` Source Library Info, bc data was modified for `from` Source Library
        Result<LibraryInfo> updateInfoResult = fromSourceLibrary.updateInfo(fromSourceLibrary.info);
        if (updateInfoResult instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<LibraryInfo>) updateInfoResult).exception());


        // Add Book to this Library's Inventory of Books
        Result<UUID2<Book>> addBookResult = this.info.addTransferringBookToInventory(bookToTransfer);
        if (addBookResult instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<UUID2<Book>>) addBookResult).exception());


        // • Transfer Book's Source Library to this Library
        Result<Book> transferredBookResult = bookToTransfer.updateSourceLibrary(this); // note: this only modifies the Book Role object, not the BookInfo.
        if (transferredBookResult instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<Book>) transferredBookResult).exception());

        // Update Info, bc data was modified for this Library
        Result<LibraryInfo> updateInfoResult2 = this.updateInfo(this.info);
        if (updateInfoResult2 instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<LibraryInfo>) updateInfoResult2).exception());


        return transferredBookResult;
    }

    /////////////////////////////////
    // Published Helper Methods    //
    /////////////////////////////////

    // Note: This Library Role Object enforces the rule:
    //   - if a User is not known, they are added as a new user.   // todo change to Result<> return type
    public boolean isUnableToFindOrRegisterUser(@NotNull User user) {
        context.log.d(this, format("Library (%s) for user: %s", this.id(), user.id()));
        if (fetchInfoFailureReason() != null) return true;

        if (isKnownUser(user)) {
            return false;
        }

        // Automatically register a new User entry in the Library (if not already known)
        Result<UUID2<User>> addRegisteredUserResult = this.info.registerUser(user.id());
        //noinspection RedundantIfStatement
        if (addRegisteredUserResult instanceof Result.Failure) {
            return true;
        }

        return false;
    }

    public boolean isKnownBook(@NotNull Book book) {
        context.log.d(this, format("Library(%s) Book id: %s\n", this.id(), book.id()));
        if (fetchInfoFailureReason() != null) return false;

        return this.info.isBookKnown(book);
    }
    public boolean isUnknownBook(@NotNull Book book) {
        return !isKnownBook(book);
    }

    public boolean isKnownUser(@NotNull User user) {
        context.log.d(this, format("Library (%s) User id: %s", this.id(), user.id()));
        if (fetchInfoFailureReason() != null) return false;

        return this.info.isUserKnown(user);
    }

    public boolean isBookAvailable(@NotNull Book book) {
        context.log.d(this, format("Library (%s) Book id: %s\n", this.id(), book.id()));
        if (fetchInfoFailureReason() != null) return false;

        return this.info.isBookAvailableToCheckout(book);
    }

    public boolean isBookCheckedOutByAnyUser(@NotNull Book book) {  // todo return Result<>?
        context.log.d(this, format("Library (%s) Book id: %s", this.id(), book.id()));
        if (fetchInfoFailureReason() != null) return false;

        return this.info.isBookIdCheckedOutByAnyUser(book.id());
    }

    // Note: This method creates a new User Object from the User id found in the Book checkout record.
    public Result<User> getUserOfCheckedOutBook(@NotNull Book book) {
        context.log.d(this, format("Library (%s) Book id: %s", this.id(), book));
        if (fetchInfoFailureReason() != null)
            return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        if(this instanceof PrivateLibrary)
            return new Result.Failure<>(new Exception("PrivateLibrary does not support registration of users, libraryId: " + this.id()));

        // get the User's id from the Book checkout record
        Result<UUID2<User>> userIdResult = this.info.findUserIdOfCheckedOutBook(book);
        if (userIdResult instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<UUID2<User>>) userIdResult).exception());
        UUID2<User> userId = ((Result.Success<UUID2<User>>) userIdResult).value();

        Result<User> fetchUserResult = User.fetchUser(userId, context);
        if (fetchUserResult instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<User>) fetchUserResult).exception());
        User user = ((Result.Success<User>) fetchUserResult).value();

        return new Result.Success<>(user);
    }

    /////////////////////////////////////////
    // Published Role Reporting Methods  //
    /////////////////////////////////////////

    public Result<ArrayList<Book>> findBooksCheckedOutByUser(@NotNull User user) {
        context.log.d(this, format("Library (%s) User id: %s\n", this.id(), user));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Make sure User is Known
        if (isUnableToFindOrRegisterUser(user)) {
            return new Result.Failure<>(new Exception("User is not known, userId: " + user.id()));
        }

        Result<ArrayList<UUID2<Book>>> entriesResult = this.info.findBooksCheckedOutByUserId(user.id());
        if (entriesResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<ArrayList<UUID2<Book>>>) entriesResult).exception());
        }

        // Convert UUID2<Books to Books
        ArrayList<UUID2<Book>> bookIds = ((Result.Success<ArrayList<UUID2<Book>>>) entriesResult).value();
        ArrayList<Book> books = new ArrayList<>();
        for (UUID2<Book> entry : bookIds) {
            books.add(new Book(entry, this, context));
        }

        return new Result.Success<>(books);
    }

    public Result<HashMap<Book, Long>> calculateAvailableBookIdToNumberAvailableList() {
        context.log.d(this, "Library (" + this.id() + ")");
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        Result<HashMap<UUID2<Book>, Long>> entriesResult = this.info.calculateAvailableBookIdToCountOfAvailableBooksMap();
        if (entriesResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<HashMap<UUID2<Book>, Long>>) entriesResult).exception());
        }

        // Convert list of UUID2<Book> to list of Book
        // Note: the BookInfo is not fetched, so the Book only contains the id. This is by design.
        HashMap<UUID2<Book>, Long> bookIdToNumberAvailable =
                ((Result.Success<HashMap<UUID2<Book>, Long>>) entriesResult).value();
        HashMap<Book, Long> bookToNumberAvailable = new HashMap<>();
        for (Map.Entry<UUID2<Book>, Long> entry : bookIdToNumberAvailable.entrySet()) {
            bookToNumberAvailable.put(
                new Book(entry.getKey(), this, context),
                    entry.getValue()
            );
        }

        return new Result.Success<>(bookToNumberAvailable);
    }

    /////////////////////////////////////////
    // Published Testing Helper Methods    //
    /////////////////////////////////////////

    // Intention revealing method name
    public Result<Book> addTestBookToLibrary(@NotNull Book book, @NotNull Integer count) {
        context.log.d(this, format("Library (%s) book: %s, count: %s", this.id(), book, count));
        return addBookToLibrary(book, count);
    }
    public Result<Book> addBookToLibrary(@NotNull Book book, @NotNull Integer count) {
        context.log.d(this, format("Library (%s) book: %s, count: %s", this.id(), book, count));
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        Result<UUID2<Book>> addBookResult =  this.info.addTestBook(book.id(), count);
        if (addBookResult instanceof Result.Failure) return new Result.Failure<>(((Result.Failure<UUID2<Book>>) addBookResult).exception());

        // Update the Info
        Result<LibraryInfo> updateInfoResult = this.updateInfo(this.info);
        if (updateInfoResult instanceof Result.Failure) return new Result.Failure<>(((Result.Failure<LibraryInfo>) updateInfoResult).exception());

        return new Result.Success<>(book);
    }

    public void DumpDB(@NotNull Context context) {
        context.log.d(this,"Dumping Library DB:");
        context.log.d(this,this.toJson());
        System.out.println();
    }

}

