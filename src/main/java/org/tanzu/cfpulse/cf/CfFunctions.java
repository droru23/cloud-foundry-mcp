package org.tanzu.cfpulse.cf;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.*;
import org.springframework.ai.mcp.spec.McpSchema;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class CfFunctions {

    private final DefaultCloudFoundryOperations cloudFoundryOperations;

    public CfFunctions(DefaultCloudFoundryOperations defaultCloudFoundryOperations) {
        this.cloudFoundryOperations = defaultCloudFoundryOperations;
    }

    /*
        Applications
    */
    public Function<Map<String, Object>, McpSchema.CallToolResult> applicationsListFunction() {
        return arguments -> {
            List<McpSchema.Content> textContents = cloudFoundryOperations.applications().list().
                    <McpSchema.Content>map(applicationSummary -> new McpSchema.TextContent(applicationSummary.toString())).
                    collectList().block();
            return new McpSchema.CallToolResult(textContents, false);
        };
    }

    public Function<Map<String, Object>, McpSchema.CallToolResult> pushApplicationFunction() {
        return arguments -> {
            String name = (String) arguments.get("name");
            String path = (String) arguments.get("path");

            PushApplicationRequest.Builder builder = PushApplicationRequest.builder().name(name).path(Paths.get(path)).noStart(true).buildpack("java_buildpack_offline");
            PushApplicationRequest request = builder.build();
            cloudFoundryOperations.applications().push(request).block();
            SetEnvironmentVariableApplicationRequest envRequest = SetEnvironmentVariableApplicationRequest.builder().
                    name(name).variableName("JBP_CONFIG_OPEN_JDK_JRE").variableValue("{ jre: { version: 17.+ } }").build();
            cloudFoundryOperations.applications().setEnvironmentVariable(envRequest).block();
            StartApplicationRequest startApplicationRequest = StartApplicationRequest.builder().name(name).build();
            cloudFoundryOperations.applications().start(startApplicationRequest).block();
            return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Done")), false);
        };
    }

    public Function<Map<String, Object>, McpSchema.CallToolResult> scaleApplicationFunction() {
        return arguments -> {
            String name = (String) arguments.get("name");
            Integer instances = (Integer) arguments.get("instances");
            Integer memory = (Integer) arguments.get("memory");
            Integer disk = (Integer) arguments.get("disk");

            ScaleApplicationRequest scaleApplicationRequest = ScaleApplicationRequest.builder().
                    name(name).
                    instances(instances).
                    diskLimit(disk).
                    memoryLimit(memory).
                    build();
            cloudFoundryOperations.applications().scale(scaleApplicationRequest).block();
            return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Done")), false);
        };
    }

    public Function<Map<String, Object>, McpSchema.CallToolResult> startApplicationFunction() {
        return arguments -> {
            String name = (String) arguments.get("name");

            StartApplicationRequest startApplicationRequest = StartApplicationRequest.builder().
                    name(name).
                    build();
            cloudFoundryOperations.applications().start(startApplicationRequest).block();
            return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Done")), false);
        };
    }

    public Function<Map<String, Object>, McpSchema.CallToolResult> stopApplicationFunction() {
        return arguments -> {
            String name = (String) arguments.get("name");

            StopApplicationRequest stopApplicationRequest = StopApplicationRequest.builder().
                    name(name).
                    build();
            cloudFoundryOperations.applications().stop(stopApplicationRequest).block();
            return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Done")), false);
        };
    }

    /*
        Organizations
     */
    public Function<Map<String, Object>, McpSchema.CallToolResult> organizationsListFunction() {
        return arguments -> {
            List<McpSchema.Content> textContents = cloudFoundryOperations.organizations().list().
                            <McpSchema.Content>map(organizationSummary -> new McpSchema.TextContent(organizationSummary.toString())).
                    collectList().block();
            return new McpSchema.CallToolResult(textContents, false);
        };
    }

    /*
        Spaces
     */
    public Function<Map<String, Object>, McpSchema.CallToolResult> spacesListFunction() {
        return arguments -> {
            List<McpSchema.Content> textContents = cloudFoundryOperations.spaces().list().
                            <McpSchema.Content>map(spaceSummary -> new McpSchema.TextContent(spaceSummary.toString())).
                    collectList().block();
            return new McpSchema.CallToolResult(textContents, false);
        };
    }
}
