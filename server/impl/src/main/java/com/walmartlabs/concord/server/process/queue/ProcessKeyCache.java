package com.walmartlabs.concord.server.process.queue;

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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.walmartlabs.concord.server.process.ProcessKey;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Named
@Singleton
public class ProcessKeyCache implements com.walmartlabs.concord.server.sdk.ProcessKeyCache {

    private final ProcessQueueDao queueDao;
    private final LoadingCache<UUID, ProcessKey> cache;

    @Inject
    public ProcessKeyCache(ProcessQueueDao queueDao) {
        this.queueDao = queueDao;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(10 * 1024L)
                .concurrencyLevel(32)
                .build(new CacheLoader<UUID, ProcessKey>() {
                    @Override
                    public ProcessKey load(UUID key) {
                        return queueDao.getKey(key);
                    }
                });
    }

    @Override
    public ProcessKey get(UUID instanceId) {
        try {
            return cache.get(instanceId);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<ProcessKey> getUncached(UUID instanceId) {
        return Optional.ofNullable(queueDao.getKey(instanceId));
    }
}
