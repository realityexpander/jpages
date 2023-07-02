package org.elegantobjects.jpages.App2.domain.user;

import org.elegantobjects.jpages.App2.domain.common.DomainInfo;
import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.common.Model;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.library.Library;
import org.elegantobjects.jpages.App2.domain.library.PrivateLibrary;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class UserInfo extends DomainInfo
    implements
        Model.ToInfoDomain<UserInfo>
{
    public final UUID2<User> id;  // note this is a UUID2<User> not a UUID2<UserInfo>, it is the id of the User.
    public final String name;
    public final String email;
    private final HashMap<UUID2<Book>, UUID2<Library>> acceptedBookIdToSourceLibraryIdMap; // BookId -> LibraryId

    UserInfo(
            @NotNull
            UUID2<User> id,        // note this is a UUID2<User> not a UUID2<UserInfo>, it is the id of the User.
            String name,
            String email,
            HashMap<UUID2<Book>, UUID2<Library>> acceptedBookIdToSourceLibraryIdMap
    ) {
        super(id);
        this.id = id;
        this.name = name;
        this.email = email;
        this.acceptedBookIdToSourceLibraryIdMap = acceptedBookIdToSourceLibraryIdMap;
    }
    UserInfo(@NotNull UserInfo userInfo) {
        this(
            userInfo.id,
            userInfo.name,
            userInfo.email,
            userInfo.acceptedBookIdToSourceLibraryIdMap
        );
    }
    UserInfo(UUID uuid, String name, String email, HashMap<UUID2<Book>, UUID2<Library>> acceptedBookIdToSourceLibraryIdMap) {
        this(new UUID2<User>(uuid, User.class), name, email, acceptedBookIdToSourceLibraryIdMap);
    }
    public UserInfo(UUID2<User> uuid2, String name, String email) {
        this(uuid2, name, email, new HashMap<>());
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

    @Override
    public String toString() {
        //return "User: " + this.name + " (" + this.email + "), acceptedBooks: " + this.acceptedBooks;
        return toPrettyJson();
    }

    ////////////////////////////////////////
    // User Info Business Logic Methods   //
    ////////////////////////////////////////

    public boolean isBookAcceptedByThisUser(UUID2<Book> bookId) {
        return this.acceptedBookIdToSourceLibraryIdMap.containsKey(bookId);
    }

    public Result<ArrayList<UUID2<Book>>> acceptBook(UUID2<Book> bookId, UUID2<Library> LibraryId) {
        if (this.acceptedBookIdToSourceLibraryIdMap.containsKey(bookId)) {
            return new Result.Failure<>(new Exception("Book already accepted by user, book id:" + bookId));
        }

        try {
            this.acceptedBookIdToSourceLibraryIdMap.put(bookId, LibraryId);
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }

        return new Result.Success<>(findAllAcceptedBookIds());
    }

    public Result<ArrayList<UUID2<Book>>> unacceptBook(UUID2<Book> bookId) {
        if (!this.acceptedBookIdToSourceLibraryIdMap.containsKey(bookId)) {
            return new Result.Failure<>(new Exception("Book not in accepted Books List for user, book id:" + bookId));
        }

        try {
            this.acceptedBookIdToSourceLibraryIdMap.remove(bookId);
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }

        return new Result.Success<>(findAllAcceptedBookIds());
    }

    ////////////////////////////////////////
    // Published Reporting Methods        //
    ////////////////////////////////////////

    public ArrayList<UUID2<Book>> findAllAcceptedBookIds() {
        return new ArrayList<UUID2<Book>>(
            this.acceptedBookIdToSourceLibraryIdMap.keySet()
        );
    }

    public HashMap<UUID2<Book>, UUID2<Library>> findAllAcceptedBookIdToLibraryIdMap() {
        return new HashMap<>(this.acceptedBookIdToSourceLibraryIdMap);
    }

    public ArrayList<UUID2<Book>> findAllAcceptedPublicLibraryBookIds() {
        ArrayList<UUID2<Book>> acceptedPublicLibraryBookIds = new ArrayList<>();

        for (UUID2<Book> bookId : this.acceptedBookIdToSourceLibraryIdMap.keySet()) {
            if (
                this.acceptedBookIdToSourceLibraryIdMap
                    .get(bookId)
                    .uuid2TypeStr()
                    .equals(UUID2.calcUUID2TypeStr(Library.class))
            ) {
                acceptedPublicLibraryBookIds.add(bookId);
            }
        }

        return acceptedPublicLibraryBookIds;
    }

    public ArrayList<UUID2<Book>> findAllAcceptedPrivateLibraryBookIds() {
        ArrayList<UUID2<Book>> acceptedPrivateLibraryBookIds = new ArrayList<>();
        for (UUID2<Book> bookId : this.acceptedBookIdToSourceLibraryIdMap.keySet()) {
            if (this.acceptedBookIdToSourceLibraryIdMap
                    .get(bookId)
                    .uuid2TypeStr()
                    .equals(UUID2.calcUUID2TypeStr(PrivateLibrary.class))
            ) {
                acceptedPrivateLibraryBookIds.add(bookId);
            }
        }
        return acceptedPrivateLibraryBookIds;
    }

    public int calculateAmountOfAcceptedBooks() {
        return this.acceptedBookIdToSourceLibraryIdMap.size();
    }
    public int calculateAmountOfAcceptedPublicLibraryBooks() {
        return findAllAcceptedPublicLibraryBookIds().size();
    }
    public int calculateAmountOfAcceptedPrivateLibraryBooks() {
        return findAllAcceptedPrivateLibraryBookIds().size();
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
        domainInfoCopy.acceptedBookIdToSourceLibraryIdMap.clear();
        for (UUID2<Book> bookId : this.acceptedBookIdToSourceLibraryIdMap.keySet()) {
            domainInfoCopy.acceptedBookIdToSourceLibraryIdMap
                .put(
                    bookId,
                    this.acceptedBookIdToSourceLibraryIdMap.get(bookId)
                );
        }

        return domainInfoCopy;
    }

    @Override
    public UUID2<?> getDomainInfoId() {
        return this.id;
    }

}
