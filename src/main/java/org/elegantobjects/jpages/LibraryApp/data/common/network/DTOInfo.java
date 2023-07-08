package org.elegantobjects.jpages.LibraryApp.data.common.network;

import org.elegantobjects.jpages.LibraryApp.common.Model;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;

/**
 * DTOInfo is a data holder class for transferring data to/from the Domain from API.<br>
 * <br>
 *  Data Transfer Objects for APIs<br>
 *  - Simple data holder class for transferring data to/from the Domain from API<br>
 *  - Objects can be created from JSON<br>
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

public class DTOInfo extends Model {
    protected DTOInfo(UUID2<?> id) {
        super(id);
    }
}
