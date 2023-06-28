package org.elegantobjects.jpages.App2.domain.user;

import org.elegantobjects.jpages.App2.domain.common.DomainInfo;
import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.common.Model;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

public class UserInfo extends DomainInfo
    implements
        Model.ToInfoDomain<UserInfo>
{
    private final UUID2<User> id;  // note this is a UUID2<User> not a UUID2<UserInfo>, it is the id of the User.
    private final String name;
    private final String email;
    private final ArrayList<UUID2<Book>> acceptedBooks;

    UserInfo(
            @NotNull
            UUID2<User> id,        // note this is a UUID2<User> not a UUID2<UserInfo>, it is the id of the User.
            String name,
            String email,
            ArrayList<UUID2<Book>> acceptedBooks
    ) {
        super(id);
        this.id = id;
        this.name = name;
        this.email = email;
        this.acceptedBooks = acceptedBooks;
    }
    UserInfo(@NotNull UserInfo userInfo) {
        this(
            userInfo.id,
            userInfo.name,
            userInfo.email,
            userInfo.acceptedBooks
        );
    }
    UserInfo(UUID uuid, String name, String email, ArrayList<UUID2<Book>> acceptedBooks) {
        this(new UUID2<User>(uuid, User.class), name, email, acceptedBooks);
    }
    UserInfo(String uuid, String name, String email, ArrayList<UUID2<Book>> acceptedBooks) {
        this(UUID.fromString(uuid), name, email);
    }
    public UserInfo(UUID2<User> uuid2, String name, String email) {
        this(uuid2, name, email, new ArrayList<UUID2<Book>>());
    }
    UserInfo(UUID uuid, String name, String email) {
        this(new UUID2<User>(uuid, User.class), name, email);
    }
    UserInfo(String uuid, String name, String email) {
        this(UUID.fromString(uuid), name, email);
    }

    ///////////////////////////////
    // Published Simple Getters  //  // note: no setters, all changes are made through business logic methods.
    ///////////////////////////////

    @Override
    public UUID2<User> id() {
        return id;
    }
    public String name() {
        return this.name;
    }
    public String email() {
        return this.email;
    }

    @Override
    public String toString() {
        //return "User: " + this.name + " (" + this.email + "), acceptedBooks: " + this.acceptedBooks;
        return toPrettyJson();
    }

    ////////////////////////////////////////
    // User Info Business Logic Methods   //
    ////////////////////////////////////////

    public Result<ArrayList<UUID2<Book>>> acceptBook(UUID2<Book> bookId) {
        if (this.acceptedBooks.contains(bookId)) {
            return new Result.Failure<>(new Exception("Book already accepted by user, book id:" + bookId));
        }

        try {
            this.acceptedBooks.add(bookId);
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }

        return new Result.Success<>(findAllAcceptedBooks());
    }

    public Result<ArrayList<UUID2<Book>>> unacceptBook(UUID2<Book> bookId) {
        if (!this.acceptedBooks.contains(bookId)) {
            return new Result.Failure<>(new Exception("Book not in acceptedBooks List for user, book id:" + bookId));
        }

        try {
            this.acceptedBooks.remove(bookId);
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }

        return new Result.Success<>(findAllAcceptedBooks());
    }

    public ArrayList<UUID2<Book>> findAllAcceptedBooks() {
        return new ArrayList<UUID2<Book>>(this.acceptedBooks);
    }

    public boolean isBookAcceptedByUser(UUID2<Book> bookId) {
        return this.acceptedBooks.contains(bookId);
    }

    public int calculateAmountOfAcceptedBooks() {
        return this.acceptedBooks.size();
    }

    ///////////////////////////////
    // ToDomain implementation   //
    ///////////////////////////////

    // note: no DB or API for UserInfo (so no .ToEntity() or .ToDTO())
    @Override
    public UserInfo toDeepCopyDomainInfo() {
        // Note: Must return a deep copy (no original references)
        UserInfo domainInfoCopy = new UserInfo(this);

        // deep copy of acceptedBooks
        domainInfoCopy.acceptedBooks.clear();
        for (UUID2<Book> bookId : this.acceptedBooks) {
            domainInfoCopy.acceptedBooks.add(new UUID2<Book>(bookId.uuid(), Book.class));
        }

        return domainInfoCopy;
    }

    @Override
    public UUID2<?> getDomainInfoId() {
        return this.id;
    }

}
