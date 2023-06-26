package org.elegantobjects.jpages.App2.data;

import org.elegantobjects.jpages.App2.IUUID2;
import org.elegantobjects.jpages.App2.Result;
import org.elegantobjects.jpages.App2.UUID2;

// API uses Model.DTOs
public interface IAPI<TUUID2 extends IUUID2, TDTOInfo> {
    Result<TDTOInfo> getDtoInfo(UUID2<TUUID2> id);

    Result<TDTOInfo> getDtoInfo(String id);

    Result<TDTOInfo> addDtoInfo(TDTOInfo dtoInfo);

    Result<TDTOInfo> updateDtoInfo(TDTOInfo dtoInfo);

    Result<TDTOInfo> upsertDtoInfo(TDTOInfo dtoInfo);

    Result<TDTOInfo> deleteDtoInfo(TDTOInfo dtoInfo);
}
