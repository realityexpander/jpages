package org.elegantobjects.jpages.LibraryApp.data.common.local;

import org.elegantobjects.jpages.LibraryApp.common.Model;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;

/**
 * EntityInfo<br>
 * <br>
 * "Dumb" Data Transfer Object for Database EntityBookInfo<br>
 * <br>
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

public class EntityInfo extends Model {
    protected EntityInfo(UUID2<?> id) {
        super(id);
    }
}
