package org.and1ss.tinyboot.framework.database.transaction;

import java.sql.Savepoint;

public interface TransactionManager {

    void setIsolationLevel(int isolationLevel);

    void beginTransaction();

    boolean isInsideTransaction();

    Savepoint createSavepoint();

    void rollbackToSavepoint(Savepoint savepoint);

    void commitTransaction();

    void rollbackTransaction();

    void closeTransaction();
}
