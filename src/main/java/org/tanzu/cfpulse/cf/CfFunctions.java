package org.tanzu.cfpulse.cf;

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.ScaleApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.cloudfoundry.operations.applications.StopApplicationRequest;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.operations.spaces.SpaceSummary;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
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
    public List<ApplicationSummary> applicationsList() {
        return cloudFoundryOperations.applications().list().collectList().block();
    }

    public record PulseScaleApplicationRequest(String applicationName, int instances, int diskLimit, int memoryLimit) {
    }

    public void scaleApplication(PulseScaleApplicationRequest request) {
        ScaleApplicationRequest scaleApplicationRequest = ScaleApplicationRequest.builder().
                name(request.applicationName).
                instances(request.instances).
                diskLimit(request.diskLimit).
                memoryLimit(request.memoryLimit).
                build();
        cloudFoundryOperations.applications().scale(scaleApplicationRequest).block();
    }

    public record PulseStartApplicationRequest(String applicationName) {
    }

    public void startApplication(PulseStartApplicationRequest request) {
        StartApplicationRequest startApplicationRequest = StartApplicationRequest.builder().
                name(request.applicationName).
                build();
        cloudFoundryOperations.applications().start(startApplicationRequest).block();
    }

    public record PulseStopApplicationRequest(String applicationName) {
    }

    public void stopApplication(PulseStopApplicationRequest request) {
        StopApplicationRequest stopApplicationRequest = StopApplicationRequest.builder().
                name(request.applicationName).
                build();
        cloudFoundryOperations.applications().stop(stopApplicationRequest).block();
    }

    /*
        Organizations
     */
    public List<OrganizationSummary> organizationsList() {
        return cloudFoundryOperations.organizations().list().collectList().block();
    }

    /*
        Spaces
     */
    public List<SpaceSummary> spacesList() {
        return cloudFoundryOperations.spaces().list().collectList().block();
    }
}
