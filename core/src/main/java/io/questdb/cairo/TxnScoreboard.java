/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2020 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.cairo;

import io.questdb.log.Log;
import io.questdb.log.LogFactory;
import io.questdb.std.Os;
import io.questdb.std.Unsafe;
import io.questdb.std.str.Path;

public class TxnScoreboard {

    private static final Log LOG = LogFactory.getLog(TxnScoreboard.class);

    public static void close(Path shmPath, long pTxnScoreboard) {
        if (close0(shmPath.address(), pTxnScoreboard) == 0) {
            LOG.info().$("close [p=").$(pTxnScoreboard).$(']').$();
            Unsafe.recordMemAlloc(-getScoreboardSize());
        }
    }

    public static void close(Path shmPath, long databaseIdLo, long databaseIdHi, CharSequence tableName, long pTxnScoreboard) {
        setShmName(shmPath, databaseIdLo, databaseIdHi, tableName);
        close(shmPath, pTxnScoreboard);
    }

    public static long create(Path shmPath, long databaseIdLo, long databaseIdHi, CharSequence tableName) {
        setShmName(shmPath, databaseIdLo, databaseIdHi, tableName);
        Unsafe.recordMemAlloc(getScoreboardSize());
        return create0(shmPath.address());
    }

    public static long newRef(long pTxnScoreboard) {
        if (pTxnScoreboard > 0) {
            return newRef0(pTxnScoreboard);
        }
        return pTxnScoreboard;
    }

    private static void setShmName(Path shmPath, long databaseIdLo, long databaseIdHi, CharSequence name) {
        if (Os.type == Os.WINDOWS) {
            shmPath.of("Local\\");
        } else {
            shmPath.of("/");
        }
        shmPath.put(databaseIdLo).put('-').put(databaseIdHi).put('-').put(name).$();
    }

    static boolean acquire(long pTxnScoreboard, long txn) {
        assert pTxnScoreboard > 0;
        LOG.info().$("acquire [p=").$(pTxnScoreboard).$(", txn=").$(txn).$(']').$();
        return acquire0(pTxnScoreboard, txn);
    }

    private native static boolean acquire0(long pTxnScoreboard, long txn);

    static void release(long pTxnScoreboard, long txn) {
        assert pTxnScoreboard > 0;
        LOG.info().$("release  [p=").$(pTxnScoreboard).$(", txn=").$(txn).$(']').$();
        release0(pTxnScoreboard, txn);
    }

    private native static long release0(long pTxnScoreboard, long txn);

    private native static long newRef0(long pTxnScoreboard);

    private static native long create0(long lpszName);

    static native long getCount(long pTxnScoreboard, long txn);

    static native long init(long pTxnScoreboard, long txn);

    static native long getMin(long pTxnScoreboard);

    private static native long close0(long lpszName, long pTxnScoreboard);

    private static native long getScoreboardSize();
}
