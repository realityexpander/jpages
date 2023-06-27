package org.elegantobjects.jpages.App2.data.common.local;

import org.elegantobjects.jpages.App2.common.Model;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;

// Entities for Databases
// Simple data holder class for transferring data to/from the Domain from Database
public class Entity extends Model {
    protected Entity(UUID2<?> id) {
        super(id);
    }
}
