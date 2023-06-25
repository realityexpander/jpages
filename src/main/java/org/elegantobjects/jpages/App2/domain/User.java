package org.elegantobjects.jpages.App2.domain;

import org.elegantobjects.jpages.App2.*;

import java.util.ArrayList;

// User Domain Object - Only interacts with its own Repo, Context, and other Domain Objects
public class User extends IRole<Model.Domain.UserInfo> implements IUUID2 {
    public final UUID2<User> id;
    private final Repo.UserInfo repo;

    User(Model.Domain.UserInfo info, Context context) {
        super(info, context);
        this.repo = context.userRepo();
        this.id = info.id();

        context.log.d(this,"User (" + this.id.toString() + ") created");
    }
//    public User(UUID2<User> id, Context context) {
////        super(id.toDomainUUID2(), context);  // todo fix
//        super(id, context);
//        this.repo = context.userRepo();
//        this.id = id;
//
//        context.log.d(this,"User (" + this.id.toString() + ") created");
//    }
    User(String json, Class<Model.Domain.UserInfo> classType, Context context) {
        super(json, classType, context);
        this.repo = context.userRepo();
        this.id = this.info.id();

        context.log.d(this,"User (" + this.id.toString() + ") created");
    }
    User(String json, Context context) {
        this(json, Model.Domain.UserInfo.class, context);
    }
//    User(Context context) {
//        this(UUID2.randomUUID2(), context);  // todo add back
//    }

    // LEAVE for reference, for static Context instance implementation
    // User(UserUUID id) {
    //     this(id, null);
    // }

    @Override
    public Result<Model.Domain.UserInfo> fetchInfoResult() {
        // context.log.d(this,"User (" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging

        infoResult = this.repo.fetchUserInfo(this.id);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<Model.Domain.UserInfo>) infoResult).value();
        return infoResult;
    }

    @Override
    public Result<Model.Domain.UserInfo> updateInfo(Model.Domain.UserInfo updatedUserInfo) {
        context.log.d(this,"User (" + this.id + "),  userInfo: " + updatedUserInfo);

        // Update self optimistically
        super.updateInfo(updatedUserInfo);

        // Update the repo
        Result<Model.Domain.UserInfo> infoResult = this.repo.updateUserInfo(updatedUserInfo);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        // Update self with Repo result
        this.info = ((Result.Success<Model.Domain.UserInfo>) infoResult).value();
        return infoResult;
    }

    ///////////////////////////////////////////
    // User Domain Business Logic Methods    //
    ///////////////////////////////////////////

    public Result<ArrayList<Book>> acceptBook(Book book) {
        context.log.d(this,"User (" + this.id.toString() + "),  book: " + this.id.toString());
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        Result<ArrayList<UUID2<Book>>> acceptResult = this.info.acceptBook(book.id);
        if(acceptResult instanceof Result.Failure)
            return new Result.Failure<>(new Exception("Failed to acceptBook, book: " + book.id.toString()));

        Result<Model.Domain.UserInfo> result = this.updateInfo(this.info);
        if (result instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<Model.Domain.UserInfo>) result).exception());

        return findAllAcceptedBooks();
    }

    public Result<ArrayList<Book>> findAllAcceptedBooks() {
        context.log.d(this,"User (" + this.id.toString() + ")");
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Create Book Domain list from the list of Book UUID2s
        ArrayList<Book> books = new ArrayList<>();
        for (UUID2<Book> bookId : this.info.findAllAcceptedBooks()) {
            books.add(new Book(bookId, this.context));
        }

        return new Result.Success<>(books);
    }

    public Result<ArrayList<UUID2<Book>>> unacceptBook(Book book) {
        context.log.d(this,"User (" + this.id.toString() + ") - returnBook,  book: " + book.id.toString() + " to user: " + this.id.toString());
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        Result<ArrayList<UUID2<Book>>> unacceptResult = this.info.unacceptBook(book.id);
        if(unacceptResult instanceof Result.Failure) {
            return new Result.Failure<>(new Exception("Failed to unaccept book from User, book: " + book.id.toString()));
        }

        Result<Model.Domain.UserInfo> result = this.updateInfo(this.info);
        if (result instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Model.Domain.UserInfo>) result).exception());
        }

        return unacceptResult;
    }

    // Note: *ONLY* the Domain Object can take a Book from one User and give it to another User.
    public Result<ArrayList<UUID2<Book>>> giveBookToUser(Book book, User receivingUser) {
        context.log.d(this,"User (" + id + ") - giveBookToUser,  book: " + book.id + ", user: " + this.id);
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Check this User has the Book
        if (!this.info.isBookAcceptedByUser(book.id))
            return new Result.Failure<>(new Exception("User (" + this.id + ") does not have book (" + book.id + ")"));

        // Add the Book to the receiving User
        Result<ArrayList<Book>> acceptBookResult = receivingUser.acceptBook(book);
        if (acceptBookResult instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<ArrayList<Book>>) acceptBookResult).exception());

        // Remove the Book from this User
        Result<ArrayList<UUID2<Book>>> unacceptBookResult = this.unacceptBook(book);
        if (unacceptBookResult instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<ArrayList<UUID2<Book>>>) unacceptBookResult).exception());

        // Update UserInfo
        Result<Model.Domain.UserInfo> result = this.updateInfo(this.info);
        if (result instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<Model.Domain.UserInfo>) result).exception());

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

    @Override
    public String getUUID2TypeStr() {
        return this.getClass().getName();
    }
}
