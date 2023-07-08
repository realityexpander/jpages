package org.elegantobjects.jpages.LibraryApp.domain.library;

import org.elegantobjects.jpages.LibraryApp.common.util.Result;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.domain.common.IRepo;

/**
 * ILibraryInfoRepo is an interface for the LibraryInfoRepo class.
 * <br>
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

public interface ILibraryInfoRepo extends IRepo {
    Result<LibraryInfo> fetchLibraryInfo(UUID2<Library> id);
    Result<LibraryInfo> updateLibraryInfo(LibraryInfo libraryInfo);
    Result<LibraryInfo> upsertLibraryInfo(LibraryInfo libraryInfo);
}
