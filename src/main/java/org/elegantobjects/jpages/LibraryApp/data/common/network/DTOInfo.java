package org.elegantobjects.jpages.LibraryApp.data.common.network;

import org.elegantobjects.jpages.LibraryApp.common.Model;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;

// Data Transfer Objects for APIs
// - Simple data holder class for transferring data to/from the Domain from API
// - Objects can be created from JSON
public class DTOInfo extends Model {
    protected DTOInfo(UUID2<?> id) {
        super(id);
    }
}
