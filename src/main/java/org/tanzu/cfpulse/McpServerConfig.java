package org.tanzu.cfpulse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tanzu.cfpulse.cf.CfService;

import java.util.List;

@Configuration
public class McpServerConfig {

    private static final Logger logger = LoggerFactory.getLogger(McpServerConfig.class);

    @Bean
    public List<ToolCallback> registerTools(CfService cfService) {
        return List.of(ToolCallbacks.from(cfService));
    }
}
