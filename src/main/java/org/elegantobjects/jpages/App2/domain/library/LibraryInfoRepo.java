package org.elegantobjects.jpages.App2.domain.library;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.log.ILog;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.book.Book;
import org.elegantobjects.jpages.App2.domain.common.IRepo;
import org.elegantobjects.jpages.App2.domain.common.Repo;

// Holds Library info for all the libraries in the system (simple CRUD operations)
public class LibraryInfoRepo extends Repo implements IRepo.LibraryInfoRepo {
    // simulate a database on server (UUID2<Library> is the key)
    private final UUID2.HashMap<Library, LibraryInfo> database = new UUID2.HashMap<>();

    public LibraryInfoRepo(ILog log) {
        super(log);
    }

    @Override
    public Result<LibraryInfo> fetchLibraryInfo(UUID2<Library> id) {
        log.d(this, "Repo.LibraryInfo - Fetching library info: " + id);

        // Simulate network/database
        if (database.containsKey(id)) {
            return new Result.Success<>(database.get(id));
        }

        return new Result.Failure<>(new Exception("Repo.LibraryInfo, Library not found, id: " + id));
    }

    @Override
    public Result<LibraryInfo> updateLibraryInfo(LibraryInfo libraryInfo) {
        log.d(this, "Repo.LibraryInfo - Updating library info: " + libraryInfo.id());

        // Simulate network/database
        if (database.containsKey(libraryInfo.id())) {
            database.put(libraryInfo.id(), libraryInfo);

            return new Result.Success<>(libraryInfo);
        }

        return new Result.Failure<>(new Exception("Repo.LibraryInfo, Library not found, id: " + libraryInfo.id()));
    }

    @Override
    public Result<LibraryInfo> upsertLibraryInfo(LibraryInfo libraryInfo) {
        log.d(this, "Repo.LibraryInfo - Upsert library info: " + libraryInfo.id());

        // Simulate network/database
        database.put(libraryInfo.id(), libraryInfo);

        return new Result.Success<>(libraryInfo);
    }

    ///////////////////////////////////
    /// Published Helper methods    ///
    ///////////////////////////////////

    public void populateWithFakeBooks(UUID2<Library> libraryId, int numberOfBooksToCreate) {
        log.d(this, "libraryId: " + libraryId + ", numberOfBooksToCreate: " + numberOfBooksToCreate);
        LibraryInfo library = database.get(libraryId);

        for (int i = 0; i < numberOfBooksToCreate; i++) {
            Result<UUID2<Book>> result = library.addTestBook(UUID2.createFakeUUID2(i, Book.class), 1);

            if (result instanceof Result.Failure) {
                Exception exception = ((Result.Failure<UUID2<Book>>) result).exception();
                log.d(this, exception.getMessage());
            }
        }
    }

}
