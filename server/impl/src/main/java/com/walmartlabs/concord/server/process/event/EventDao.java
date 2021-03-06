package com.walmartlabs.concord.server.process.event;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2018 Walmart Inc.
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import com.walmartlabs.concord.db.AbstractDao;
import com.walmartlabs.concord.db.MainDB;
import com.walmartlabs.concord.server.process.ProcessKey;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.walmartlabs.concord.server.jooq.Tables.PROCESS_EVENTS;
import static org.jooq.impl.DSL.*;

@Named
public class EventDao extends AbstractDao {

    @Inject
    public EventDao(@MainDB Configuration cfg) {
        super(cfg);
    }

    public List<ProcessEventEntry> list(ProcessKey processKey, Timestamp geTimestamp, String eventType, int limit) {
        return list(processKey, geTimestamp, eventType, null, limit);
    }

    public List<ProcessEventEntry> list(ProcessKey processKey, Timestamp geTimestamp, String eventType, Map<String, Object> dataFilter, int limit) {
        try (DSLContext tx = DSL.using(cfg)) {

            SelectConditionStep<Record4<UUID, String, Timestamp, String>> q = tx
                    .select(PROCESS_EVENTS.EVENT_ID,
                            PROCESS_EVENTS.EVENT_TYPE,
                            PROCESS_EVENTS.EVENT_DATE,
                            PROCESS_EVENTS.EVENT_DATA.cast(String.class))
                    .from(PROCESS_EVENTS)
                    .where(PROCESS_EVENTS.INSTANCE_ID.eq(processKey.getInstanceId())
                            .and(PROCESS_EVENTS.INSTANCE_CREATED_AT.eq(processKey.getCreatedAt())));

            if (geTimestamp != null) {
                q.and(PROCESS_EVENTS.EVENT_DATE.ge(geTimestamp));
            }

            if (eventType != null) {
                q.and(PROCESS_EVENTS.EVENT_TYPE.eq(eventType));
            }

            if (dataFilter != null) {
                dataFilter.forEach((k, v) -> {
                    q.and(field("{0}->>{1}", Object.class, PROCESS_EVENTS.EVENT_DATA, inline(k)).eq(v));
                });
            }

            if (limit > 0) {
                q.limit(limit);
            }

            return q.orderBy(PROCESS_EVENTS.EVENT_DATE)
                    .fetch(EventDao::toEntry);
        }
    }

    public void insert(ProcessKey processKey, String eventType, String eventData) {
        tx(tx -> insert(tx, processKey, eventType, eventData));
    }

    // TODO shouldn't eventData be Serializable or Object?
    public void insert(DSLContext tx, ProcessKey processKey, String eventType, String eventData) {
        tx.insertInto(PROCESS_EVENTS)
                .columns(PROCESS_EVENTS.INSTANCE_ID,
                        PROCESS_EVENTS.INSTANCE_CREATED_AT,
                        PROCESS_EVENTS.EVENT_TYPE,
                        PROCESS_EVENTS.EVENT_DATE,
                        PROCESS_EVENTS.EVENT_DATA)
                .values(value(processKey.getInstanceId()),
                        value(processKey.getCreatedAt()),
                        value(eventType),
                        currentTimestamp(),
                        field("?::jsonb", eventData))
                .execute();
    }

    public void insert(DSLContext tx, List<ProcessKey> processKeys, String eventType, String eventData) {
        String sql = tx.insertInto(PROCESS_EVENTS,
                PROCESS_EVENTS.INSTANCE_ID,
                PROCESS_EVENTS.INSTANCE_CREATED_AT,
                PROCESS_EVENTS.EVENT_TYPE,
                PROCESS_EVENTS.EVENT_DATE,
                PROCESS_EVENTS.EVENT_DATA)
                .values(value((UUID) null), null, null, currentTimestamp(), field("?::jsonb"))
                .getSQL();

        tx.connection(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (ProcessKey pk : processKeys) {
                    ps.setObject(1, pk.getInstanceId());
                    ps.setTimestamp(2, pk.getCreatedAt());
                    ps.setString(3, eventType);
                    ps.setString(4, eventData);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        });
    }

    private static ProcessEventEntry toEntry(Record4<UUID, String, Timestamp, String> r) {
        return ImmutableProcessEventEntry.builder()
                .id(r.value1())
                .eventType(r.value2())
                .eventDate(r.value3())
                .data(r.value4())
                .build();
    }
}
