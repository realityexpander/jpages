package org.elegantobjects.jpages.LibraryApp.domain.library.data;

import org.elegantobjects.jpages.LibraryApp.common.util.Result;
import org.elegantobjects.jpages.LibraryApp.common.util.log.ILog;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.domain.book.Book;
import org.elegantobjects.jpages.LibraryApp.domain.common.data.repo.Repo;
import org.elegantobjects.jpages.LibraryApp.domain.library.Library;
import org.elegantobjects.jpages.LibraryApp.domain.library.PrivateLibrary;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * LibraryInfoRepo is a repository for LibraryInfo objects.<br>
 * <br>
 * Holds Library info for all the libraries in the system (simple CRUD operations).
 * <br>
 * Simulates a database on a server via in-memory HashMap.<br>
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

public class LibraryInfoRepo extends Repo implements ILibraryInfoRepo {
    // simulate a database on server
    private final UUID2.HashMap<UUID2<Library>, LibraryInfo> database = new UUID2.HashMap<>();

    public
    LibraryInfoRepo(@NotNull  ILog log) {
        super(log);
    }

    @Override
    public Result<LibraryInfo> fetchLibraryInfo(UUID2<Library> id) {
        log.d(this, "libraryId: " + id);

        // Simulate network/database
        if (database.containsKey(id)) {
            return new Result.Success<>(database.get(id));
        }

        return new Result.Failure<>(new Exception("Repo.LibraryInfo, Library not found, id: " + id));
    }

    @Override
    public Result<LibraryInfo> updateLibraryInfo(@NotNull LibraryInfo libraryInfo) {
        log.d(this, "libraryInfo.id: " + libraryInfo.id());

        // Simulate network/database
        if (database.containsKey(libraryInfo.id())) {
            database.put(libraryInfo.id(), libraryInfo);

            return new Result.Success<>(libraryInfo);
        }

        return new Result.Failure<>(new Exception("Repo.LibraryInfo, Library not found, id: " + libraryInfo.id()));
    }

    @Override
    public Result<LibraryInfo> upsertLibraryInfo(@NotNull LibraryInfo libraryInfo) {
        log.d(this, "libraryInfo.id: " + libraryInfo.id());

        // Simulate network/database
        database.put(libraryInfo.id(), libraryInfo);

        return new Result.Success<>(libraryInfo);
    }

    ///////////////////////////////////
    /// Published Helper methods    ///
    ///////////////////////////////////

    public void populateWithFakeBooks(@NotNull UUID2<Library> libraryId, int numberOfBooksToCreate) {
        log.d(this, "libraryId: " + libraryId + ", numberOfBooksToCreate: " + numberOfBooksToCreate);
        LibraryInfo libraryInfo = database.get(libraryId);

        for (int i = 0; i < numberOfBooksToCreate; i++) {
            Result<UUID2<Book>> result =
                    libraryInfo.addTestBook(UUID2.createFakeUUID2(1000+i*100, Book.class), 1);

            if (result instanceof Result.Failure) {
                Exception exception = ((Result.Failure<UUID2<Book>>) result).exception();
                log.d(this, exception.getMessage());
            }
        }
    }

    public void removeAllOrphanPrivateLibrariesWithNoBooksInInventory() {
        log.d(this, "removeAllPrivateLibrariesWithNoBooksInInventory");

        for (UUID2<Library> entry : database.keySet()) {
            String uuid2TypeStr = entry.uuid2TypeStr();
            LibraryInfo libraryInfo = database.get(entry);

            if (Objects.equals(uuid2TypeStr, UUID2.calcUUID2TypeStr(PrivateLibrary.class))
                    && libraryInfo.findAllKnownBookIds().isEmpty()) {
                database.remove(entry);
            }
        }
    }

}
