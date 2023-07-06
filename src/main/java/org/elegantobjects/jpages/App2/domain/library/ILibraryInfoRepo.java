package org.elegantobjects.jpages.App2.domain.library;

import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.domain.common.IRepo;

public interface ILibraryInfoRepo extends IRepo {
    Result<LibraryInfo> fetchLibraryInfo(UUID2<Library> id);
    Result<LibraryInfo> updateLibraryInfo(LibraryInfo libraryInfo);
    Result<LibraryInfo> upsertLibraryInfo(LibraryInfo libraryInfo);
}
