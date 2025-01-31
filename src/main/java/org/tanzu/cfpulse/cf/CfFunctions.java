package org.tanzu.cfpulse.cf;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.*;
import org.springframework.ai.mcp.spec.McpSchema;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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
    public Function<Map<String, Object>, Mono<McpSchema.CallToolResult>> applicationsListFunction() {
        return noArgs -> cloudFoundryOperations.applications().list().
                        <McpSchema.Content>map(applicationSummary -> new McpSchema.TextContent(applicationSummary.toString())).
                collectList().
                map(textContents -> new McpSchema.CallToolResult(textContents, false));
    }

    public Function<Map<String, Object>, Mono<McpSchema.CallToolResult>> pushApplicationFunction() {
        return arguments -> {
            String name = (String) arguments.get("name");
            String path = (String) arguments.get("path");

            PushApplicationRequest request = PushApplicationRequest.builder().
                    name(name).path(Paths.get(path)).noStart(true).buildpack("java_buildpack_offline").
                    build();
            SetEnvironmentVariableApplicationRequest envRequest = SetEnvironmentVariableApplicationRequest.builder().
                    name(name).variableName("JBP_CONFIG_OPEN_JDK_JRE").variableValue("{ jre: { version: 17.+ } }").
                    build();
            StartApplicationRequest startApplicationRequest = StartApplicationRequest.builder().
                    name(name).
                    build();

            return cloudFoundryOperations.applications().
                    push(request).
                    then(cloudFoundryOperations.applications().setEnvironmentVariable(envRequest)).
                    then(cloudFoundryOperations.applications().start(startApplicationRequest)).
                    then(Mono.just(new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Done")), false)));
        };
    }

    public Function<Map<String, Object>, Mono<McpSchema.CallToolResult>> scaleApplicationFunction() {
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
            return cloudFoundryOperations.applications().scale(scaleApplicationRequest).
                    then(Mono.just(new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Done")), false)));
        };
    }

    public Function<Map<String, Object>, Mono<McpSchema.CallToolResult>> startApplicationFunction() {
        return arguments -> {
            String name = (String) arguments.get("name");

            StartApplicationRequest startApplicationRequest = StartApplicationRequest.builder().
                    name(name).
                    build();
            return cloudFoundryOperations.applications().start(startApplicationRequest).
                    then(Mono.just(new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Done")), false)));
        };
    }

    public Function<Map<String, Object>, Mono<McpSchema.CallToolResult>> stopApplicationFunction() {
        return arguments -> {
            String name = (String) arguments.get("name");

            StopApplicationRequest stopApplicationRequest = StopApplicationRequest.builder().
                    name(name).
                    build();
            return cloudFoundryOperations.applications().stop(stopApplicationRequest).
                    then(Mono.just(new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Done")), false)));
        };
    }

    public Function<Map<String, Object>, Mono<McpSchema.CallToolResult>> deleteApplicationFunction() {
        return arguments -> {
            String name = (String) arguments.get("name");

            DeleteApplicationRequest deleteApplicationRequest = DeleteApplicationRequest.builder().
                    name(name).
                    build();
            return cloudFoundryOperations.applications().delete(deleteApplicationRequest).
                    then(Mono.just(new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("Done")), false)));
        };
    }

    /*
        Organizations
     */
    public Function<Map<String, Object>, Mono<McpSchema.CallToolResult>> organizationsListFunction() {
        return noArgs -> cloudFoundryOperations.organizations().list().
                        <McpSchema.Content>map(organizationSummary -> new McpSchema.TextContent(organizationSummary.toString())).
                collectList().
                map(textContents -> new McpSchema.CallToolResult(textContents, false));
    }

    /*
        Spaces
     */
    public Function<Map<String, Object>, Mono<McpSchema.CallToolResult>> spacesListFunction() {
        return noArgs -> cloudFoundryOperations.spaces().list().
                        <McpSchema.Content>map(spaceSummary -> new McpSchema.TextContent(spaceSummary.toString())).
                collectList().
        map( textContents -> new McpSchema.CallToolResult(textContents, false));
    }
}
