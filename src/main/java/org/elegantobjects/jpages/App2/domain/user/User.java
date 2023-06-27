package org.elegantobjects.jpages.App2.domain.user;

import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.common.IRole;
import org.elegantobjects.jpages.App2.domain.Context;
import org.elegantobjects.jpages.App2.domain.library.Library;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

// User Domain Object - Only interacts with its own Repo, Context, and other Domain Objects
public class User extends IRole<UserInfo> implements IUUID2 {
    public final UUID2<User> id;
    private final UserInfoRepo repo;

    public User(
        @NotNull UserInfo info,
        Context context
    ) {
        super(info.id(), context);
        this.repo = context.userRepo();
        this.id = info.id();

        context.log.d(this,"User (" + this.id.toString() + ") created from Info");
    }
    public User(
        @NotNull UUID2<User> id,
        Context context
    ) {
        super(id.toDomainUUID2(), context);
        this.repo = context.userRepo();
        this.id = id;

        context.log.d(this,"User (" + this.id.toString() + ") created from id with no Info");
    }
    public User(
            String json,
            Class<UserInfo> clazz,  // class type of json object
            Context context
    ) {
        super(json, clazz, context);
        this.repo = context.userRepo();
        this.id = this.info.id();

        context.log.d(this,"User (" + this.id.toString() + ") created Json with class: " + clazz.getName());
    }
    public User(String json, Context context) {
        this(json, UserInfo.class, context);
    }
    public User(Context context) {
        this(UUID2.randomUUID2(), context);
    }
    // LEAVE for reference, for static Context instance implementation
    // User(UserUUID id) {
    //     this(id, null);
    // }

    /////////////////////////////////////
    // IRole/UUID2 Required Overrides  //
    /////////////////////////////////////

    @Override
    public Result<UserInfo> fetchInfoResult() {
        // context.log.d(this,"User (" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging

        infoResult = this.repo.fetchUserInfo(this.id);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<UserInfo>) infoResult).value();
        return infoResult;
    }

    @Override
    public Result<UserInfo> updateInfo(UserInfo updatedUserInfo) {
        context.log.d(this,"User (" + this.id + "),  userInfo: " + updatedUserInfo);

        // Update self optimistically
        super.updateInfo(updatedUserInfo);

        // Update the repo
        Result<UserInfo> infoResult = this.repo.updateUserInfo(updatedUserInfo);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        // Update self with Repo result
        this.info = ((Result.Success<UserInfo>) infoResult).value();
        return infoResult;
    }

    @Override
    public String getUUID2TypeStr() {
        return this.getClass().getName();
    }

    /////////////////////////////////////////////
    // User Domain Business Logic Methods      //
    // - Methods to modify it's DomainUserInfo //
    /////////////////////////////////////////////

    public Result<ArrayList<Book>> acceptBook(Book book) {
        context.log.d(this,"User (" + this.id.toString() + "),  book: " + this.id.toString());
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        Result<ArrayList<UUID2<Book>>> acceptResult = this.info.acceptBook(book.id);
        if(acceptResult instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<ArrayList<UUID2<Book>>>) acceptResult).exception());


        Result<UserInfo> result = this.updateInfo(this.info);
        if (result instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<UserInfo>) result).exception());

        return findAllAcceptedBooks();
    }

    public Result<ArrayList<UUID2<Book>>> unacceptBook(Book book) {
        context.log.d(this,"User (" + this.id + "), book: " + book.id.toString());
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        Result<ArrayList<UUID2<Book>>> unacceptResult = this.info.unacceptBook(book.id);
        if(unacceptResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<ArrayList<UUID2<Book>>>) unacceptResult).exception());
        }

        Result<UserInfo> result = this.updateInfo(this.info);
        if (result instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<UserInfo>) result).exception());
        }

        return unacceptResult;
    }

    public Result<ArrayList<Book>> findAllAcceptedBooks() {
        context.log.d(this,"User (" + this.id + ")");
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Create Book Domain list from the list of Book UUID2s
        ArrayList<Book> books = new ArrayList<>();
        for (UUID2<Book> bookId : this.info.findAllAcceptedBooks()) {
            books.add(new Book(bookId, this.context));
        }

        return new Result.Success<>(books);
    }

    // Note: *ONLY* the Domain Objects can take a Book from one User and give it to another User.
    // - Also notice that we are politely asking each Domain object to accept and unaccept a Book.
    // - No where is there any databases being accessed directly.
    // - All interactions are SOLELY directed via the Domain object's public methods.
    public Result<ArrayList<UUID2<Book>>> giveBookToUser(Book book, User receivingUser) {
        context.log.d(this,"User (" + this.id + ") - book: " + book.id + ", to receivingUser: " + receivingUser.id);
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Check this User has the Book
        if (!this.info.isBookAcceptedByUser(book.id))
            return new Result.Failure<>(new Exception("User (" + this.id + ") does not have book (" + book.id + ")"));

        // Add the Book to the receiving User (the receiving User will automatically update their own Info)
        Result<ArrayList<Book>> acceptBookResult = receivingUser.acceptBook(book);
        if (acceptBookResult instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<ArrayList<Book>>) acceptBookResult).exception());

        // Remove the Book from this User
        Result<ArrayList<UUID2<Book>>> unacceptBookResult = this.unacceptBook(book);
        if (unacceptBookResult instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<ArrayList<UUID2<Book>>>) unacceptBookResult).exception());

        // // LEAVE FOR REFERENCE
        // // Update UserInfo // no update needed as each method used performs its own updates.
        // // But if a different Local Object (like a hashmap) was changed after this event, an .updateInfo(…) would need to be performed.
        // Result<Model.Domain.UserInfo> result = this.updateInfo(this.info);
        // if (result instanceof Result.Failure)
        //    return new Result.Failure<>(((Result.Failure<Model.Domain.UserInfo>) result).exception());

        return unacceptBookResult;
    }

    public Result<UUID2<Book>> checkoutBookFromLibrary(Book book, Library library) {
        context.log.d(this,"User (" + this.id + "), book: " + this.id + ", library: " + library.id);
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        Result<Book> bookResult = library.checkOutBookToUser(book, this);
        if (bookResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Book>) bookResult).exception());
        }

        return new Result.Success<>(((Result.Success<Book>) bookResult).value().id);
    }
}
