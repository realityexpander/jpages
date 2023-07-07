package org.elegantobjects.jpages.LibraryApp.domain.common;

import org.elegantobjects.jpages.LibraryApp.common.Model;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;

// Domain objects contain the "Model.{XXX}.{Domain}Info" and the associated business logic to manipulate it
public class DomainInfo extends Model {
    protected DomainInfo(UUID2<?> id) {
        super(id);
    }
}
