package org.elegantobjects.jpages.LibraryApp.common.util.uuid2;

/**
 * <b>{@code IUUID2}</b> Marker interface.<br>
 *
 * Marker interface for any Domain <b>{@code Role}</b> class for use in <b>{@code UUID2<{Domain}>}</b> identifiers.<br>
 *
 * This interface is also used to get the type of the UUID2 as a String.
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.11
 */

public interface IUUID2 {  // Keep this in global namespace to reduce wordiness at declaration sites (avoiding: UUID2<UUID2.hasUUID2> wordiness)
    String uuid2TypeStr();  // Returns the Type of the UUID2 as a String.
                            // - Usually the class inheritance hierarchy path of the object
                            // - ie: "Model.DomainInfo.BookInfo" or "Role.Book"
}
