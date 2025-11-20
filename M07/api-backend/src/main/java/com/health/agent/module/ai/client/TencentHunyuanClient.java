package com.health.agent.module.ai.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.agent.config.AIConfig;
import com.health.agent.module.ai.dto.AIRequestDTO;
import com.health.agent.module.ai.dto.AIResponseDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 腾讯混元大模型HTTP客户端实现
 * 使用原生HTTP请求，绕过OpenAI SDK的限制
 * 
 * @author Health Agent Team
 * @since 2025-01-01
 */
@Slf4j
@Component
@Primary
public class TencentHunyuanClient implements AIClient {
    
    private final AIConfig aiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public TencentHunyuanClient(AIConfig aiConfig) {
        this.aiConfig = aiConfig;
        this.restTemplate = createRestTemplate();
        this.objectMapper = new ObjectMapper();
        log.info("✅ 腾讯混元HTTP客户端初始化成功");
    }
    
    private RestTemplate createRestTemplate() {
        // 使用SimpleClientHttpRequestFactory配置超时
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000); // 30秒连接超时
        factory.setReadTimeout(60000);    // 60秒读取超时
        
        RestTemplate template = new RestTemplate(factory);
        log.info("RestTemplate配置完成 - 连接超时: 30s, 读取超时: 60s");
        return template;
    }
    
    @Override
    public AIResponseDTO chat(AIRequestDTO request) {
        try {
            log.info("========== 腾讯混元API请求详情 ==========");
            log.info("API URL: {}", aiConfig.getApiUrl());
            log.info("Model: {}", aiConfig.getModel());
            log.info("Max Tokens: {}", aiConfig.getMaxTokens());
            log.info("Temperature: {}", aiConfig.getTemperature());
            log.info("消息数量: {}", request.getMessages().size());
            
            // 构建请求体
            HunyuanRequest hunyuanRequest = new HunyuanRequest();
            hunyuanRequest.setModel(aiConfig.getModel());
            hunyuanRequest.setMaxTokens(aiConfig.getMaxTokens());
            hunyuanRequest.setTemperature(aiConfig.getTemperature());
            hunyuanRequest.setStream(false);
            
            // 转换消息格式
            List<HunyuanMessage> messages = request.getMessages().stream()
                    .map(msg -> {
                        HunyuanMessage hunyuanMsg = new HunyuanMessage();
                        hunyuanMsg.setRole(msg.getRole());
                        hunyuanMsg.setContent(msg.getContent());
                        return hunyuanMsg;
                    })
                    .collect(Collectors.toList());
            hunyuanRequest.setMessages(messages);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiConfig.getApiKey());
            
            // 创建请求实体
            HttpEntity<HunyuanRequest> entity = new HttpEntity<>(hunyuanRequest, headers);
            
            log.info("发送请求到: {}/chat/completions", aiConfig.getApiUrl());
            long startTime = System.currentTimeMillis();
            
            // 发送HTTP请求
            ResponseEntity<HunyuanResponse> response = restTemplate.exchange(
                    aiConfig.getApiUrl() + "/chat/completions",
                    HttpMethod.POST,
                    entity,
                    HunyuanResponse.class
            );
            
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("请求完成，耗时: {}ms", elapsed);
            
            // 处理响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                HunyuanResponse hunyuanResponse = response.getBody();
                
                if (hunyuanResponse.getChoices() != null && !hunyuanResponse.getChoices().isEmpty()) {
                    String content = hunyuanResponse.getChoices().get(0).getMessage().getContent();
                    
                    // 构建使用情况
                    AIResponseDTO.Usage usage = null;
                    if (hunyuanResponse.getUsage() != null) {
                        usage = AIResponseDTO.Usage.builder()
                                .promptTokens(hunyuanResponse.getUsage().getPromptTokens())
                                .completionTokens(hunyuanResponse.getUsage().getCompletionTokens())
                                .totalTokens(hunyuanResponse.getUsage().getTotalTokens())
                                .build();
                    }
                    
                    log.info("AI响应成功 - Tokens: {}, Reply length: {}", 
                            usage != null ? usage.getTotalTokens() : 0, content.length());
                    
                    return AIResponseDTO.builder()
                            .content(content)
                            .model(aiConfig.getModel())
                            .usage(usage)
                            .success(true)
                            .build();
                } else {
                    throw new RuntimeException("响应中没有有效的选择项");
                }
            } else {
                throw new RuntimeException("HTTP请求失败，状态码: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("========== 腾讯混元API请求失败 ==========");
            log.error("错误类型: {}", e.getClass().getName());
            log.error("错误信息: {}", e.getMessage());
            log.error("API URL: {}", aiConfig.getApiUrl());
            log.error("Model: {}", aiConfig.getModel());
            log.error("完整堆栈:", e);
            
            throw new RuntimeException("腾讯混元API调用失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void chatStream(AIRequestDTO request, StreamCallback callback) {
        // 流式实现可以后续添加
        throw new UnsupportedOperationException("腾讯混元流式模式暂未实现");
    }
    
    // 腾讯混元API请求格式
    @Data
    public static class HunyuanRequest {
        private String model;
        private List<HunyuanMessage> messages;
        
        @JsonProperty("max_tokens")
        private Integer maxTokens;
        
        private Double temperature;
        private Boolean stream;
    }
    
    @Data
    public static class HunyuanMessage {
        private String role;
        private String content;
    }
    
    // 腾讯混元API响应格式
    @Data
    public static class HunyuanResponse {
        private String id;
        private String object;
        private Long created;
        private String model;
        private List<HunyuanChoice> choices;
        private HunyuanUsage usage;
    }
    
    @Data
    public static class HunyuanChoice {
        private Integer index;
        private HunyuanMessage message;
        
        @JsonProperty("finish_reason")
        private String finishReason;
    }
    
    @Data
    public static class HunyuanUsage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}