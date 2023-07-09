package org.elegantobjects.jpages.LibraryApp.common.data.network;

import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.LibraryApp.common.util.Result;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;

/**
 * IAPI is an interface for the API class.
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

// API uses Model.DTOs
public interface IAPI<TUUID2 extends IUUID2, TDTOInfo> {
    Result<TDTOInfo> getDtoInfo(UUID2<TUUID2> id);
    Result<TDTOInfo> getDtoInfo(String id);
    Result<TDTOInfo> addDtoInfo(TDTOInfo dtoInfo);
    Result<TDTOInfo> updateDtoInfo(TDTOInfo dtoInfo);
    Result<TDTOInfo> upsertDtoInfo(TDTOInfo dtoInfo);
    Result<TDTOInfo> deleteDtoInfo(TDTOInfo dtoInfo);
}
