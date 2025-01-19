package org.tanzu.cfpulse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.server.McpAsyncServer;
import org.springframework.ai.mcp.server.McpServer;
import org.springframework.ai.mcp.server.transport.StdioServerTransport;
import org.springframework.ai.mcp.server.transport.WebMvcSseServerTransport;
import org.springframework.ai.mcp.spec.McpSchema;
import org.springframework.ai.mcp.spec.ServerMcpTransport;
import org.springframework.ai.mcp.spring.ToolHelper;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import org.tanzu.cfpulse.cf.CfFunctions;

import java.util.List;

@Configuration
@EnableWebMvc
public class McpServerConfig implements WebMvcConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(McpServerConfig.class);
	private final CfFunctions cfFunctions;

	public McpServerConfig(CfFunctions cfFunctions) {
		this.cfFunctions = cfFunctions;
	}

	@Bean
	@ConditionalOnProperty(prefix = "transport", name = "mode", havingValue = "sse")
	public WebMvcSseServerTransport webMvcSseServerTransport() {
		return new WebMvcSseServerTransport(new ObjectMapper(), "/mcp/message");
	}

	@Bean
	@ConditionalOnProperty(prefix = "transport", name = "mode", havingValue = "sse")
	public RouterFunction<ServerResponse> routerFunction(WebMvcSseServerTransport transport) {
		return transport.getRouterFunction();
	}

	@Bean
	@ConditionalOnProperty(prefix = "transport", name = "mode", havingValue = "stdio")
	public StdioServerTransport stdioServerTransport() {
		return new StdioServerTransport();
	}

	@Bean
	public McpAsyncServer mcpServer(ServerMcpTransport transport) {

		var capabilities = McpSchema.ServerCapabilities.builder()
			.resources(false, true) // No subscribe support, but list changes notifications
			.tools(true) // Tool support with list changes notifications
			.prompts(true) // Prompt support with list changes notifications
			.logging() // Logging support
			.build();

		// Create the server with both tool and resource capabilities
		var server = McpServer.using(transport).
				serverInfo("CF Pulse MCP Server", "1.0.0").
				capabilities(capabilities).
				tools(cfToolRegistrations()).
				async();
		
		return server;
	}

	public List<McpServer.ToolRegistration> cfToolRegistrations() {

		return ToolHelper.toToolRegistration(
				// Applications
				FunctionCallback.builder().
						method("applicationsList").
						targetObject(cfFunctions).
						description("Return the applications (apps) in my Cloud Foundry space").
						build(),
				FunctionCallback.builder().
						method("scaleApplication", CfFunctions.PulseScaleApplicationRequest.class).
						targetObject(cfFunctions).
						description("Scale the number of instances, memory, or disk size of an application. Instances, memoryLimit, and diskLimit arguments can be null").
						build(),
				FunctionCallback.builder().
						method("startApplication", CfFunctions.PulseStartApplicationRequest.class).
						targetObject(cfFunctions).
						description("Start a running Cloud Foundry application").
						build(),
				FunctionCallback.builder().
						method("stopApplication", CfFunctions.PulseStopApplicationRequest.class).
						targetObject(cfFunctions).
						description("Stop a running Cloud Foundry application").
						build(),

				// Organizations
				FunctionCallback.builder().
						method("organizationsList").
						targetObject(cfFunctions).
						description("Return the organizations (orgs) in my Cloud Foundry foundation").
						build(),

				// Spaces
				FunctionCallback.builder().
						method("spacesList").
						targetObject(cfFunctions).
						description("Returns the spaces in my Cloud Foundry organization (org)").
						build()
		);
	}
}
