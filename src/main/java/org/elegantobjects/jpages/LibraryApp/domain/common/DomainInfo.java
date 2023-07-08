package org.elegantobjects.jpages.LibraryApp.domain.common;

import org.elegantobjects.jpages.LibraryApp.common.Model;
import org.elegantobjects.jpages.LibraryApp.common.util.uuid2.UUID2;

/**
 * DomainInfo is a base class for all DomainInfo classes.<br>
 * <br>
 * Domain object encapsulate this DomainInfo class to provide mutable information about the domain object.<br>
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

public class DomainInfo extends Model {
    protected DomainInfo(UUID2<?> id) {
        super(id);
    }
}
