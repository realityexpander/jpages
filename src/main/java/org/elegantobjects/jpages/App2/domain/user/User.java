package org.elegantobjects.jpages.App2.domain.user;

import org.elegantobjects.jpages.App2.common.util.Pair;
import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.account.Account;
import org.elegantobjects.jpages.App2.domain.account.AccountInfo;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.common.Role;
import org.elegantobjects.jpages.App2.domain.Context;
import org.elegantobjects.jpages.App2.domain.library.Library;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

// User Domain Object - Only interacts with its own Repo, Context, and other Domain Objects
public class User extends Role<UserInfo> implements IUUID2 {
    public final UUID2<User> id;
    private final UserInfoRepo repo;
    private final Account account; // User's Account Domain Object

    public User(
        @NotNull UserInfo info,
        Account account,
        Context context
    ) {
        super(info.id(), context);
        this.account = account;
        this.repo = context.userInfoRepo();
        this.id = info.id();

        context.log.d(this,"User (" + this.id.toString() + ") created from Info");
    }
    public User(
        @NotNull UUID2<User> id,
        Account account, Context context
    ) {
        super(id.toDomainUUID2(), context);
        this.account = account;
        this.repo = context.userInfoRepo();
        this.id = id;

        context.log.d(this,"User (" + this.id.toString() + ") created from id with no Info");
    }
    public User(
            String json,
            Class<UserInfo> clazz,  // class type of json object
            Account account,
            Context context
    ) {
        super(json, clazz, context);
        this.account = account;
        this.repo = context.userInfoRepo();
        this.id = this.info.id();

        context.log.d(this,"User (" + this.id.toString() + ") created Json with class: " + clazz.getName());
    }
    public User(String json, Account account, Context context) {
        this(json, UserInfo.class, account, context);
    }
    public User(Account account, Context context) {
        this(UUID2.randomUUID2(), account, context);
    }

    public String toString() {
        String str = "User (" + this.id.toString() + ") - ";

        if (null != this.info)
            str += "info=" + this.info.toPrettyJson();
        else
            str += "info=null";

        if (null != this.account)
            str += ", account=" + this.account;
        else
            str += ", account=null";

        return str;
    }

    public String toJson() {
        Pair<UserInfo, AccountInfo> pair = new Pair<>(info, account.info());
        return context.gson.toJson(pair);
    }

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

        // Success, so update self.
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
//        return this.getClass().getName();
//        return UUID2.getUUID2TypeStr(User.class);
        return UUID2.getUUID2TypeStr(this.getClass()); // todo test does this work?
    }

    /////////////////////////////////////////////
    // User Domain Business Logic Methods      //
    // - Methods to modify it's DomainUserInfo //
    /////////////////////////////////////////////

    // Note: This delegates to its internal Account Role object.
    // - User has no intimate knowledge of the AccountInfo object, other than
    //   its public methods.
    // - Method shows how to combine User and Account Roles to achieve functionality.
    // - This method uses the UserInfo object to calculate the number of books the user has
    //   and then delegates to the AccountInfo object to determine if the
    //   number of books has reached the max.
    public Result<ArrayList<Book>> acceptBook(Book book) {
        context.log.d(this,"User (" + this.id + "),  bookId: " + book.id);
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        if(hasReachedMaxAmountOfAcceptedLibraryBooks()) return new Result.Failure<>(new Exception("User (" + this.id + ") has reached maximum amount of accepted Library Books"));

        Result<ArrayList<UUID2<Book>>> acceptResult =
                this.info.acceptBook(
                    book.id,
                    book.sourceLibrary().id
                );
        if(acceptResult instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<ArrayList<UUID2<Book>>>) acceptResult).exception());


        Result<UserInfo> result = this.updateInfo(this.info);
        if (result instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<UserInfo>) result).exception());

        return findAllAcceptedBooks();
    }

    public Result<ArrayList<UUID2<Book>>> unacceptBook(@NotNull Book book) {
        context.log.d(this,"User (" + this.id + "), bookId: " + book.id);
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

    public AccountInfo accountInfo() {
        return this.account.info();
    }

    // Note: This delegates to this User's internal Account Role object.
    // - User has no intimate knowledge of the Account object, other than
    //   its public methods.
    public Boolean isAccountInGoodStanding() {
        context.log.d(this,"User (" + this.id + ")");
        AccountInfo accountinfo = this.account.info();
        if (accountinfo == null) {
            context.log.e(this,"User (" + this.id + ") - AccountInfo is null");
            return false;
        }

        return accountinfo.isAccountInGoodStanding();
    }

    // Note: This delegates to this User's internal Account Role object.
    public Boolean hasReachedMaxAmountOfAcceptedLibraryBooks() {
        context.log.d(this,"User (" + this.id + ")");
        AccountInfo accountInfo = this.accountInfo();
        if (accountInfo == null) {
            context.log.e(this,"User (" + this.id + ") - AccountInfo is null");
            return false;
        }

        int numLibraryBooksAcceptedByUser = this.info.calculateAmountOfAcceptedLibraryBooks();

        // Note: This User Role Object delegates to its internal Account Role Object.
        return accountInfo.hasReachedMaxAmountOfAcceptedLibraryBooks(numLibraryBooksAcceptedByUser);
    }

    public Result<ArrayList<Book>> findAllAcceptedBooks() {
        context.log.d(this,"User (" + this.id + ")");
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Create  list of Domain Books from the list of Accepted Book ids
        ArrayList<Book> books = new ArrayList<>();
        for (Map.Entry<UUID2<Book>, UUID2<Library>> entry :
                this.info.findAllAcceptedBookIdToLibraryIdMap().entrySet()
        ) {
            UUID2<Book> bookId = entry.getKey();
            UUID2<Library> libraryId = entry.getValue();

            Book book = new Book(bookId, new Library(libraryId, context), context);

            books.add(book);
        }

        return new Result.Success<>(books);
    }

    // Note: *ONLY* the Domain Objects can take a Book from one User and give it to another User.
    // - Also notice that we are politely asking each Domain object to accept and unaccept a Book.
    // - No where is there any databases being accessed directly.
    // - All interactions are SOLELY directed via the Domain object's public methods.
    public Result<ArrayList<UUID2<Book>>> giveBookToUser(@NotNull Book book, @NotNull User receivingUser) {
        context.log.d(this,"User (" + this.id + ") - book: " + book.id + ", to receivingUser: " + receivingUser.id);
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Check this User has the Book
        if (!this.info.isBookAcceptedByThisUser(book.id))
            return new Result.Failure<>(new Exception("User (" + this.id + ") does not have book (" + book.id + ")"));

        // Have Library Swap the checkout of Book from this User to the receiving User
        Result<Book> swapCheckoutResult =
                book.sourceLibrary().info()
                    .transferBookCheckoutFromUserToUser(
                          book,
                          this,
                          receivingUser
                    );
        if (swapCheckoutResult instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<Book>) swapCheckoutResult).exception());

        // Now transfer the BookId from the fromUser to the toUser
        this.unacceptBook(book);
        receivingUser.acceptBook(book);

        Result<UserInfo> result = this.updateInfo(this.info); // todo check if this is needed
        if (result instanceof Result.Failure)
            return new Result.Failure<>(((Result.Failure<UserInfo>) result).exception());

        return new Result.Success<>(new ArrayList<>(Arrays.asList(book.id)));

        // // LEAVE FOR REFERENCE
        // // Update UserInfo // no update needed as each method used performs its own updates.
        // // But if a different Local Object (like a hashmap) was changed after this event, an .updateInfo(…) would need to be performed.
    }

    // Convenience method to checkout a Book from a Library
    // - Is it OK to also have this method in the Library Role Object?
    //   I'm siding with yes, since it just delegates to the Library Role Object.
    public Result<UUID2<Book>> checkOutBookFromLibrary(Book book, Library library) {
        context.log.d(this,"User (" + this.id + "), book: " + this.id + ", library: " + library.id);
        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));

        // Note: Simply delegating to the Library Role Object
        Result<Book> bookResult = library.checkOutBookToUser(book, this);
        if (bookResult instanceof Result.Failure) {
            return new Result.Failure<>(((Result.Failure<Book>) bookResult).exception());
        }

        return new Result.Success<>(((Result.Success<Book>) bookResult).value().id);
    }
}
