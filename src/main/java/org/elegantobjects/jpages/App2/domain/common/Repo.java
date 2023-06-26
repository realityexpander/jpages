package org.elegantobjects.jpages.App2.domain.common;


import org.elegantobjects.jpages.App2.common.util.Result;
import org.elegantobjects.jpages.App2.common.util.uuid2.UUID2;
import org.elegantobjects.jpages.App2.data.local.EntityBookInfo;
import org.elegantobjects.jpages.App2.data.network.BookInfoApi;
import org.elegantobjects.jpages.App2.data.local.BookInfoDatabase;
import org.elegantobjects.jpages.App2.common.util.log.ILog;
import org.elegantobjects.jpages.App2.common.util.log.Log;
import org.elegantobjects.jpages.App2.data.network.DTOBookInfo;
import org.elegantobjects.jpages.App2.domain.Book;
import org.elegantobjects.jpages.App2.domain.Library;
import org.elegantobjects.jpages.App2.domain.User;
import org.elegantobjects.jpages.App2.domain.common.IRepo;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

// Since the Repo only accepts/returns Domain.{Domain}Info objects, this lives in the domain layer.
// - Internally, it accesses the data layer, and does conversions between the layers.
public class Repo implements IRepo {
    protected final ILog log;

    Repo(ILog log) {
        this.log = log;
    }

}
