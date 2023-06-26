package org.elegantobjects.jpages.App2.data.network;

import org.elegantobjects.jpages.App2.common.ModelInfo;
import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;

// Data Transfer Objects for APIs
// - Simple data holder class for transferring data to/from the Domain from API
// - Objects can be created from JSON
public class DTO extends ModelInfo {
    public DTO(UUID2<IUUID2> id, String className) {
        super(id, className);
    }

}
