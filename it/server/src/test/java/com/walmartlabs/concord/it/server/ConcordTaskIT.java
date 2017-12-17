package com.walmartlabs.concord.it.server;

import com.walmartlabs.concord.server.api.org.OrganizationEntry;
import com.walmartlabs.concord.server.api.org.OrganizationResource;
import com.walmartlabs.concord.server.api.org.ResourceAccessEntry;
import com.walmartlabs.concord.server.api.org.ResourceAccessLevel;
import com.walmartlabs.concord.server.api.org.project.ProjectEntry;
import com.walmartlabs.concord.server.api.org.project.ProjectResource;
import com.walmartlabs.concord.server.api.org.project.ProjectVisibility;
import com.walmartlabs.concord.server.api.org.team.*;
import com.walmartlabs.concord.server.api.process.ProcessEntry;
import com.walmartlabs.concord.server.api.process.ProcessResource;
import com.walmartlabs.concord.server.api.process.ProcessStatus;
import com.walmartlabs.concord.server.api.process.StartProcessResponse;
import com.walmartlabs.concord.server.api.security.apikey.ApiKeyResource;
import com.walmartlabs.concord.server.api.security.apikey.CreateApiKeyRequest;
import com.walmartlabs.concord.server.api.security.apikey.CreateApiKeyResponse;
import com.walmartlabs.concord.server.api.user.CreateUserRequest;
import com.walmartlabs.concord.server.api.user.UserResource;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.walmartlabs.concord.it.common.ITUtils.archive;
import static com.walmartlabs.concord.it.common.ServerClient.assertLog;
import static com.walmartlabs.concord.it.common.ServerClient.waitForStatus;

public class ConcordTaskIT extends AbstractServerIT {

    @Test(timeout = 60000)
    public void testStartArchive() throws Exception {
        // create a new org

        String orgName = "org_" + randomString();

        OrganizationResource orgResource = proxy(OrganizationResource.class);
        orgResource.createOrUpdate(new OrganizationEntry(orgName));

        // add the user A

        UserResource userResource = proxy(UserResource.class);

        String userAName = "userA_" + randomString();
        userResource.createOrUpdate(new CreateUserRequest(userAName));

        ApiKeyResource apiKeyResource = proxy(ApiKeyResource.class);
        CreateApiKeyResponse apiKeyA = apiKeyResource.create(new CreateApiKeyRequest(userAName));

        // create the user A's team

        String teamName = "team_" + randomString();

        TeamResource teamResource = proxy(TeamResource.class);
        CreateTeamResponse ctr = teamResource.createOrUpdate(orgName, new TeamEntry(teamName));

        teamResource.addUsers(orgName, teamName, Collections.singleton(new TeamUserEntry(userAName, TeamRole.MEMBER)));

        // switch to the user A and create a new private project

        setApiKey(apiKeyA.getKey());

        String projectName = "project_" + randomString();

        ProjectResource projectResource = proxy(ProjectResource.class);
        projectResource.createOrUpdate(orgName, new ProjectEntry(projectName, ProjectVisibility.PRIVATE));

        // grant the team access to the project

        projectResource.updateAccessLevel(orgName, projectName, new ResourceAccessEntry(ctr.getId(), ResourceAccessLevel.READER));

        // start a new process using the project as the user A

        byte[] payload = archive(ProcessRbacIT.class.getResource("concordTask").toURI());
        Map<String, Object> input = new HashMap<>();
        input.put("archive", payload);
        input.put("org", orgName);
        input.put("project", projectName);

        StartProcessResponse spr = start(input);

        ProcessResource processResource = proxy(ProcessResource.class);
        ProcessEntry pir = waitForStatus(processResource, spr.getInstanceId(), ProcessStatus.FINISHED);

        // ---

        byte[] ab = getLog(pir.getLogFileName());
        assertLog(".*Done!.*", ab);
    }
}