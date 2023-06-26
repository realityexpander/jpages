package org.elegantobjects.jpages.App2.core.uuid2;

// Marker interface for Domain classes that use UUID2<{Domain}>.
public interface IUUID2 {  // Keep this in global namespace to reduce wordiness at declaration sites (avoiding: UUID2<UUID2.hasUUID2> wordiness)
    String getUUID2TypeStr();  // Defines the Type of the UUID2 as a String.
                               // - Usually the full class name of the Domain object
                               // - ie: "org.elegantobjects.jpages.Model$Domain$BookInfo"
}
