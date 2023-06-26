package org.elegantobjects.jpages.App2.domain.repoData;

import org.elegantobjects.jpages.App2.common.util.uuid2.IUUID2;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.common.Model;

import java.util.*;

// Domain objects contain the "Model.{XXX}.{Domain}Info" and the associated business logic to manipulate it
public class Domain extends Model {

    // next lines are ugly java boilerplate to allow call to super() with a UUID2
    protected Domain(UUID2<?> id, String className) {
        super(id, className);
    }
    Domain(UUID uuid, String className) {
        super(new UUID2<IUUID2>(uuid), className);
    }
    Domain(String id, String className) {
        super(UUID2.fromString(id), className);
    }

    // This is primarily for JSON deserialization purposes
    @Override
    public UUID2<?> id() {
        return super.id();
    }
}
