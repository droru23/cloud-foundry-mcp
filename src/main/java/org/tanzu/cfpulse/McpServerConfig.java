package org.tanzu.cfpulse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.server.McpAsyncServer;
import org.springframework.ai.mcp.server.McpServer;
import org.springframework.ai.mcp.server.McpServerFeatures;
import org.springframework.ai.mcp.server.transport.StdioServerTransport;
import org.springframework.ai.mcp.server.transport.WebMvcSseServerTransport;
import org.springframework.ai.mcp.spec.McpSchema;
import org.springframework.ai.mcp.spec.ServerMcpTransport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import org.tanzu.cfpulse.cf.CfFunctions;

@Configuration
public class McpServerConfig {

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
                .resources(false, false)
                .tools(true) // Tool support with list changes notifications
                .prompts(false)
                .logging() // Logging support
                .build();

        // Create the server with both tool and resource capabilities
        var server = McpServer.async(transport).
                serverInfo("CF Pulse MCP Server", "1.0.0").
                capabilities(capabilities).
                tools(applicationsListTool(),pushApplicationTool(),scaleApplicationTool(),startApplicationTool(),
                        stopApplicationTool(),deleteApplicationTool(),organizationsListTool(),spacesListTool()).
                build();
        return server;
    }

    // Applications
    private static final String DESCRIPTION_APPLICATION_LIST = "Return the applications (apps) in my Cloud Foundry space";
    private McpServerFeatures.AsyncToolRegistration applicationsListTool() {
        return new McpServerFeatures.AsyncToolRegistration(
                new McpSchema.Tool("applicationsList", DESCRIPTION_APPLICATION_LIST,
                        """
                                {
                                    "type": "object",
                                    "properties": {
                                    },
                                    "required": []
                                }
                                """),
                cfFunctions.applicationsListFunction());
    }

    private static final String DESCRIPTION_PUSH_APPLICATION = "Pushes an application to the Cloud Foundry space.";
    private McpServerFeatures.AsyncToolRegistration pushApplicationTool() {
        return new McpServerFeatures.AsyncToolRegistration(new McpSchema.Tool("pushApplication", DESCRIPTION_PUSH_APPLICATION,
                """
                        {
                        	"type": "object",
                        	"properties": {
                        		"name": {
                        			"type": "string",
                        			"description": "Name of the Cloud Foundry application"
                        		},
                        		"path": {
                        			"type": "string",
                        			"description": "Path to the compiled JAR file for the application"
                        		}
                        	},
                        	"required": ["name", "path"]
                        }
                        """),
                cfFunctions.pushApplicationFunction());
    }

    private static final String DESCRIPTION_SCALE_APPLICATION = "Scale the number of instances, memory, or disk size of an application. ";
    private McpServerFeatures.AsyncToolRegistration scaleApplicationTool() {
        return new McpServerFeatures.AsyncToolRegistration(new McpSchema.Tool("scaleApplication", DESCRIPTION_SCALE_APPLICATION,
                """
                        {
                        	"type": "object",
                        	"properties": {
                        		"name": {
                        			"type": "string",
                        			"description": "Name of the Cloud Foundry application"
                        		},
                        		"instances": {
                        			"type": "number",
                        			"description": "The new number of instances of the Cloud Foundry application"
                        		},
                        		"memory": {
                        			"type": "number",
                        			"description": "The new memory limit, in megabytes, of the Cloud Foundry application"
                        		},
                        		"disk": {
                        			"type": "number",
                        			"description": "The new disk size, in megabytes, of the Cloud Foundry application"
                        		}
                        	},
                        	"required": ["name"]
                        }
                        """),
                cfFunctions.scaleApplicationFunction());
    }

    private static final String DESCRIPTION_START_APPLICATION = "Start a Cloud Foundry application";
    private McpServerFeatures.AsyncToolRegistration startApplicationTool() {
        return new McpServerFeatures.AsyncToolRegistration(new McpSchema.Tool("startApplication", DESCRIPTION_START_APPLICATION,
                """
                        {
                            "type": "object",
                            "properties": {
                                "name": {
                                    "type": "string",
                                    "description": "Name of the Cloud Foundry application"
                                }
                            },
                            "required": ["name"]
                        }
                        """),
                cfFunctions.startApplicationFunction());
    }

    private static final String DESCRIPTION_STOP_APPLICATION = "Stop a running Cloud Foundry application";
    private McpServerFeatures.AsyncToolRegistration stopApplicationTool() {
        return new McpServerFeatures.AsyncToolRegistration(new McpSchema.Tool("stopApplication", DESCRIPTION_STOP_APPLICATION,
                """
                        {
                            "type": "object",
                            "properties": {
                                "name": {
                                    "type": "string",
                                    "description": "Name of the Cloud Foundry application"
                                }
                            },
                            "required": ["name"]
                        }
                        """),
                cfFunctions.stopApplicationFunction());
    }

    private static final String DESCRIPTION_DELETE_APPLICATION = "Delete a Cloud Foundry application";
    private McpServerFeatures.AsyncToolRegistration deleteApplicationTool() {
        return new McpServerFeatures.AsyncToolRegistration(new McpSchema.Tool("deleteApplication", DESCRIPTION_DELETE_APPLICATION,
                """
                        {
                            "type": "object",
                            "properties": {
                                "name": {
                                    "type": "string",
                                    "description": "Name of the Cloud Foundry application"
                                }
                            },
                            "required": ["name"]
                        }
                        """),
                cfFunctions.deleteApplicationFunction());
    }

    private static final String DESCRIPTION_ORGANIZATION_LIST = "Return the organizations (orgs) in my Cloud Foundry foundation";
    private McpServerFeatures.AsyncToolRegistration organizationsListTool() {
        return new McpServerFeatures.AsyncToolRegistration(
                new McpSchema.Tool("organizationsList", DESCRIPTION_ORGANIZATION_LIST,
                        """
                                {
                                    "type": "object",
                                    "properties": {
                                    },
                                    "required": []
                                }
                                """),
                cfFunctions.organizationsListFunction());
    }

    private static final String DESCRIPTION_SPACE_LIST = "Returns the spaces in my Cloud Foundry organization (org)";
    private McpServerFeatures.AsyncToolRegistration spacesListTool() {
        return new McpServerFeatures.AsyncToolRegistration(
                new McpSchema.Tool("spacesList", DESCRIPTION_SPACE_LIST,
                        """
                                {
                                    "type": "object",
                                    "properties": {
                                    },
                                    "required": []
                                }
                                """),
                cfFunctions.spacesListFunction());
    }
}
