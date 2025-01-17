package org.elegantobjects.jpages.LibraryApp.data.local;

import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.LibraryApp.common.util.Result;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.LibraryApp.domain.common.data.info.local.EntityInfo;

import java.util.Map;

/**
 * IDatabase is an interface for the Database class.
 *
 * This class should be wrapped and use domain-specific language for the EntityInfo class.
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

// DB uses Model.Entities
public interface IDatabase<TUUID2 extends IUUID2, TEntity extends EntityInfo> {
    Result<TEntity> getEntityInfo(UUID2<TUUID2> id);
    Result<TEntity> updateEntityInfo(TEntity entityInfo);
    Result<TEntity> addEntityInfo(TEntity entityInfo);
    Result<TEntity> upsertEntityInfo(TEntity entityInfo);
    Result<TEntity> deleteEntityInfo(TEntity entityInfo);
    Map<UUID2<TUUID2>, TEntity> getAllEntityInfo();
}
