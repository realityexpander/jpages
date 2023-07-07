package org.elegantobjects.jpages.LibraryApp.common.util.uuid2;

// Marker interface for Domain classes that use UUID2<{Domain}>.
public interface IUUID2 {  // Keep this in global namespace to reduce wordiness at declaration sites (avoiding: UUID2<UUID2.hasUUID2> wordiness)
    String uuid2TypeStr();  // Returns the Type of the UUID2 as a String.
                               // - Usually the last 3 segments of the class hierarchy of the object
                               // - ie: "Model.DomainInfo.BookInfo" or "Role.Book"
}
