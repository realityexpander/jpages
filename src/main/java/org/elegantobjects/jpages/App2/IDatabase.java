package org.elegantobjects.jpages.App2;

import java.util.Map;

// DB uses Model.Entities
public interface IDatabase<TUUID2 extends IUUID2, TEntity extends Model.Entity> {
    Result<TEntity> getEntityInfo(UUID2<TUUID2> id);

    Result<TEntity> updateEntityInfo(TEntity entityInfo);

    Result<TEntity> addEntityInfo(TEntity entityInfo);

    Result<TEntity> upsertEntityInfo(TEntity entityInfo);

    Result<TEntity> deleteEntityInfo(TEntity entityInfo);

    Map<UUID2<TUUID2>, TEntity> getAllEntityInfo();
}
