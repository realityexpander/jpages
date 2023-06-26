package org.elegantobjects.jpages.App2.data.common.local;

import org.elegantobjects.jpages.App2.data.local.Entity;
import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;

import java.util.Map;

// DB uses Model.Entities
public interface IDatabase<TUUID2 extends IUUID2, TEntity extends Entity> {
    Result<TEntity> getEntityInfo(UUID2<TUUID2> id);

    Result<TEntity> updateEntityInfo(TEntity entityInfo);

    Result<TEntity> addEntityInfo(TEntity entityInfo);

    Result<TEntity> upsertEntityInfo(TEntity entityInfo);

    Result<TEntity> deleteEntityInfo(TEntity entityInfo);

    Map<UUID2<TUUID2>, TEntity> getAllEntityInfo();
}
