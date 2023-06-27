package org.elegantobjects.jpages.App2.domain.common;

import org.elegantobjects.jpages.App2.common.ModelInfo;
import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;

import java.util.*;

// Domain objects contain the "Model.{XXX}.{Domain}Info" and the associated business logic to manipulate it
public class DomainInfo extends ModelInfo {

    // next lines are ugly java boilerplate to allow call to super() with a UUID2
    protected DomainInfo(UUID2<?> id, String className) {
        super(id, className);
    }
    DomainInfo(UUID uuid, String className) {
        super(new UUID2<IUUID2>(uuid), className);
    }
    DomainInfo(String id, String className) {
        super(UUID2.fromString(id), className);
    }

    // This is primarily for JSON deserialization purposes
    @Override
    public UUID2<?> id() {
        return super.id();
    }
}
