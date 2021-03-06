package com.walmartlabs.concord.server.org;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2019 Walmart Inc.
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.walmartlabs.concord.server.user.UserType;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.UUID;

@Value.Immutable
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonSerialize(as = ImmutableEntityOwner.class)
@JsonDeserialize(as = ImmutableEntityOwner.class)
public interface EntityOwner extends Serializable {

    @Nullable
    UUID id();

    @Nullable
    String username();

    @Nullable
    String displayName();

    @Nullable
    UserType userType();

    static EntityOwner of(UUID id, String username, String displayName, UserType userType) {
        return ImmutableEntityOwner.builder()
                .id(id)
                .username(username)
                .displayName(displayName)
                .userType(userType)
                .build();
    }
}
