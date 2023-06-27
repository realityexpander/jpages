package org.elegantobjects.jpages.App2.data.common.network;

import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public
class InMemoryAPI<TUUID2 extends IUUID2, TDTOInfo extends DTO> implements IAPI<TUUID2, TDTOInfo> {
    private final URL url;
    private final HttpClient client;

    // Simulate a database accessed via a network API
    private final UUID2.HashMap<TUUID2, TDTOInfo> database = new UUID2.HashMap<>();

    public InMemoryAPI(URL url, HttpClient client) {
        this.url = url;
        this.client = client;
    }
    InMemoryAPI() {
        this(
            new URL("http://localhost:8080"),
            new HttpClient()
        );
    }

    @Override
    public Result<TDTOInfo> getDtoInfo(String id) {
        try {
            @SuppressWarnings("unchecked")
            UUID2<TUUID2> uuid = (UUID2<TUUID2>) UUID2.fromString(id);
            return getDtoInfo(uuid);
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }
    }

    @Override
    public Result<TDTOInfo> getDtoInfo(UUID2<TUUID2> id) {
        // Simulate the network request
        if (!database.containsKey(id)) {
            return new Result.Failure<>(new Exception("API: DTOInfo not found, id=" + id));
        }

        return new Result.Success<>(database.get(id));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<TDTOInfo> updateDtoInfo(TDTOInfo dtoInfo) {
        try {
            // Simulate Network
            database.put((UUID2<TUUID2>) dtoInfo.id(), dtoInfo);
        } catch (Exception e) {
            return new Result.Failure<>(e);
        }

        return new Result.Success<>(dtoInfo);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<TDTOInfo> addDtoInfo(TDTOInfo dtoInfo) {
        // Simulate Network
        if (database.containsKey((UUID2<TUUID2>) dtoInfo.id())) {
            return new Result.Failure<>(new Exception("API: DtoInfo already exists, use UPDATE, id=" + dtoInfo.id()));
        }

        database.put((UUID2<TUUID2>) dtoInfo.id(), dtoInfo);

        return new Result.Success<>(dtoInfo);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<TDTOInfo> upsertDtoInfo(TDTOInfo dtoInfo) {
        // Simulate Network
        if (database.containsKey((UUID2<TUUID2>) dtoInfo.id())) {
            return updateDtoInfo(dtoInfo);
        } else {
            return addDtoInfo(dtoInfo);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<TDTOInfo> deleteDtoInfo(TDTOInfo dtoInfo) {
        // Simulate Network
        if (database.remove((UUID2<TUUID2>) dtoInfo.id()) == null) {
            return new Result.Failure<>(new Exception("API: Failed to delete DtoInfo"));
        }

        return new Result.Success<>(dtoInfo);
    }

    public Map<UUID2<TUUID2>, TDTOInfo> getAllDtoInfos() {
        Map<UUID2<TUUID2>, TDTOInfo> map = new HashMap<>();

        // Simulate Network
        for (Map.Entry<UUID, TDTOInfo> entry : database.entrySet()) {
            map.put(new UUID2<>(entry.getKey()), entry.getValue());
        }

        return map;
    }
}