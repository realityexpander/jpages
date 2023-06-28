package org.elegantobjects.jpages.App2.domain.account;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.Context;
import org.elegantobjects.jpages.App2.domain.common.Role;
import org.jetbrains.annotations.NotNull;

import static java.lang.String.format;

// Account Domain Object - *ONLY* interacts with its own Repo, Context, and other Domain Objects
public class Account extends Role<AccountInfo> implements IUUID2 {
    public final UUID2<Account> id;
    private final AccountInfoRepo repo;

    public Account(
        @NotNull AccountInfo info,
        Context context
    ) {
        super(info, context);
        this.repo = this.context.accountInfoRepo();
        this.id = info.id();

        context.log.d(this,"Account (" + this.id + ") created from Info");
    }
    public Account(
        String json,
        Class<AccountInfo> clazz,
        Context context
    ) {
        super(json, clazz, context);
        this.repo = this.context.accountInfoRepo();
        this.id = this.info.id();

        context.log.d(this,"Account (" + this.id + ") created from Json with class: " + clazz.getName());
    }
    public Account(
        @NotNull UUID2<Account> id,
        Context context
    ) {
        super(id, context);
        this.repo = this.context.accountInfoRepo();
        this.id = id;

        context.log.d(this,"Account(" + this.id + ") created using id with no Info");
    }
    public Account(String json, Context context) { this(json, AccountInfo.class, context); }
    public Account(Context context) {
        this(UUID2.randomUUID2(), context);
    }
    // LEAVE for reference, for static Context instance implementation
    // Account() {
    //     this(UUID2.randomUUID());
    // }

    /////////////////////////////////////
    // IRole/UUID2 Required Overrides  //
    /////////////////////////////////////

    @Override
    public Result<AccountInfo> fetchInfoResult() {
        // context.log.d(this, "Account(" + this.id.toString() + ") - fetchInfoResult"); // LEAVE for debugging

        infoResult = this.repo.fetchAccountInfo(this.id);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        this.info = ((Result.Success<AccountInfo>) infoResult).value();

        return infoResult;
    }

    @Override
    public Result<AccountInfo> updateInfo(AccountInfo updatedInfo) {
        // context.log.d(this,"Account (" + this.id.toString() + ") - updateInfo, newInfo: " + newInfo.toString());  // LEAVE for debugging

        // Update self optimistically
        super.updateInfo(updatedInfo);

        // Update the Repo
        Result<AccountInfo> infoResult = this.repo.updateAccountInfo(updatedInfo);
        if (infoResult instanceof Result.Failure) {
            return infoResult;
        }

        // Update self with Repo result
        super.updateInfo(((Result.Success<AccountInfo>) infoResult).value());
        return infoResult;
    }

    @Override
    public String getUUID2TypeStr() {
//        return UUID2.getUUID2TypeStr(Account.class);
        return UUID2.getUUID2TypeStr(this.getClass()); // todo test does this work?
    }

    ///////////////////////////////////////////
    // Account Domain Business Logic Methods //
    // - Methods to modify it's LibraryInfo  //
    ///////////////////////////////////////////



//    // This Account DomainObject enforces the rule: if a User is not known, they are added as a new user.
//    public boolean isUnableToFindOrAddUser(User user) {


//    public Result<HashMap<Book, Integer>> calculateAvailableBookIdToNumberAvailableList() {

//    public Result<Book> addTestBookToLibrary(Book book, Integer count) {
//        context.log.d(this, format("Account (%s) book: %s, count: %s", this.id, book, count));
//        if (fetchInfoFailureReason() != null) return new Result.Failure<>(new Exception(fetchInfoFailureReason()));
//
//        Result<UUID2<Book>> addBookResult =  this.info.addTestBook(book.id, count);
//        if (addBookResult instanceof Result.Failure) {
//            return new Result.Failure<>(((Result.Failure<UUID2<Book>>) addBookResult).exception());
//        }
//
//        // Update the Info
//        Result<AccountInfo> updateInfoResult = this.updateInfo(this.info);
//        if (updateInfoResult instanceof Result.Failure) {
//            return new Result.Failure<>(((Result.Failure<AccountInfo>) updateInfoResult).exception());
//        }
//
//        return new Result.Success<>(book);
//    }

    public void DumpDB(Context context) {
        context.log.d(this,"\nDumping Account DB:");
        context.log.d(this, this.toJson());
        context.log.d(this,"\n");
    }
}