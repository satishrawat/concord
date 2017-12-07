package com.walmartlabs.concord.server.api.org;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.UUID;

@JsonInclude(Include.NON_NULL)
public class ResourceAccessEntry implements Serializable {

    @NotNull
    private final UUID teamId;

    @NotNull
    private final ResourceAccessLevel level;

    @JsonCreator
    public ResourceAccessEntry(@JsonProperty("teamId") UUID teamId,
                               @JsonProperty("level") ResourceAccessLevel level) {

        this.teamId = teamId;
        this.level = level;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public ResourceAccessLevel getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "ProjectAccessEntry{" +
                ", teamId=" + teamId +
                ", level=" + level +
                '}';
    }
}