package org.elegantobjects.jpages.LibraryApp.data.common.local;

import org.elegantobjects.jpages.LibraryApp.common.Model;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;

// Entities for Databases
// Simple data holder class for transferring data to/from the Domain from Database
public class EntityInfo extends Model {
    protected EntityInfo(UUID2<?> id) {
        super(id);
    }
}
