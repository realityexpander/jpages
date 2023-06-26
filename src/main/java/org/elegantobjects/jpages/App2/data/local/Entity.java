package org.elegantobjects.jpages.App2.data.local;

import org.elegantobjects.jpages.App2.common.Model;
import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;

// Entities for Databases
// Simple data holder class for transferring data to/from the Domain from Database
public class Entity extends Model {
    Entity(UUID2<IUUID2> id, String className) {
        super(id, className);
    }

}
