package org.tanzu.cfpulse.cf;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.*;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.operations.spaces.SpaceSummary;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.List;

@Service
public class CfService {

    private final DefaultCloudFoundryOperations cloudFoundryOperations;

    public CfService(DefaultCloudFoundryOperations defaultCloudFoundryOperations) {
        this.cloudFoundryOperations = defaultCloudFoundryOperations;
    }

    /*
        Applications
    */
    private static final String APPLICATION_LIST = "Return the applications (apps) in my Cloud Foundry space";

    @Tool(description = APPLICATION_LIST)
    public List<ApplicationSummary> applicationsList() {
        return cloudFoundryOperations.applications().list().collectList().block();
    }

    private static final String PUSH_APPLICATION = "Push an application to the Cloud Foundry space.";
    private static final String NAME_PARAM = "Name of the Cloud Foundry application";
    private static final String PATH_PARAM = "Path to the compiled JAR file for the application";

    @Tool(description = PUSH_APPLICATION)
    public void pushApplication(@ToolParam(description = NAME_PARAM) String applicationName,
                                @ToolParam(description = PATH_PARAM) String path) {
        PushApplicationRequest request = PushApplicationRequest.builder().
                name(applicationName).
                path(Paths.get(path)).
                noStart(true).
                buildpack("java_buildpack_offline").
                build();
        SetEnvironmentVariableApplicationRequest envRequest = SetEnvironmentVariableApplicationRequest.builder().
                name(applicationName).variableName("JBP_CONFIG_OPEN_JDK_JRE").variableValue("{ jre: { version: 17.+ } }").
                build();
        StartApplicationRequest startApplicationRequest = StartApplicationRequest.builder().
                name(applicationName).
                build();

        cloudFoundryOperations.applications().push(request).block();
        cloudFoundryOperations.applications().setEnvironmentVariable(envRequest).block();
        cloudFoundryOperations.applications().start(startApplicationRequest).block();
    }

    private static final String SCALE_APPLICATION = "Scale the number of instances, memory, or disk size of an application. ";
    private static final String INSTANCES_PARAM = "The new number of instances of the Cloud Foundry application";
    private static final String MEMORY_PARAM = "The new memory limit, in megabytes, of the Cloud Foundry application";
    private static final String DISK_PARAM = "The new disk size, in megabytes, of the Cloud Foundry application";

    @Tool(description = SCALE_APPLICATION)
    public void scaleApplication(@ToolParam(description = NAME_PARAM) String applicationName,
                                 @ToolParam(description = INSTANCES_PARAM, required = false) Integer instances,
                                 @ToolParam(description = MEMORY_PARAM, required = false) Integer memory,
                                 @ToolParam(description = DISK_PARAM, required = false) Integer disk) {
        ScaleApplicationRequest scaleApplicationRequest = ScaleApplicationRequest.builder().
                name(applicationName).
                instances(instances).
                diskLimit(disk).
                memoryLimit(memory).
                build();
        cloudFoundryOperations.applications().scale(scaleApplicationRequest).block();
    }

    private static final String START_APPLICATION = "Start a Cloud Foundry application";

    @Tool(description = START_APPLICATION)
    public void startApplication(@ToolParam(description = NAME_PARAM) String applicationName) {
        StartApplicationRequest startApplicationRequest = StartApplicationRequest.builder().
                name(applicationName).
                build();
        cloudFoundryOperations.applications().start(startApplicationRequest).block();
    }

    private static final String STOP_APPLICATION = "Stop a running Cloud Foundry application";

    @Tool(description = STOP_APPLICATION)
    public void stopApplication(@ToolParam(description = NAME_PARAM) String applicationName) {
        StopApplicationRequest stopApplicationRequest = StopApplicationRequest.builder().
                name(applicationName).
                build();
        cloudFoundryOperations.applications().stop(stopApplicationRequest).block();
    }

    private static final String DELETE_APPLICATION = "Delete a Cloud Foundry application";

    @Tool(description = DELETE_APPLICATION)
    public void deleteApplication(@ToolParam(description = NAME_PARAM) String applicationName) {
        DeleteApplicationRequest deleteApplicationRequest = DeleteApplicationRequest.builder().
                name(applicationName).
                build();
        cloudFoundryOperations.applications().delete(deleteApplicationRequest).block();
    }

    /*
        Organizations
     */
    private static final String ORGANIZATION_LIST = "Return the organizations (orgs) in my Cloud Foundry foundation";

    @Tool(description = ORGANIZATION_LIST)
    public List<OrganizationSummary> organizationsList() {
        return cloudFoundryOperations.organizations().list().collectList().block();
    }

    /*
        Spaces
     */
    private static final String SPACE_LIST = "Returns the spaces in my Cloud Foundry organization (org)";

    @Tool(description = SPACE_LIST)
    public List<SpaceSummary> spacesList() {
        return cloudFoundryOperations.spaces().list().collectList().block();
    }
}
