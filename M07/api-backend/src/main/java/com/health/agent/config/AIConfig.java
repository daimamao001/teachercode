package com.health.agent.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI服务配置
 * 使用原生HTTP客户端，支持腾讯混元等AI服务提供商
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AIConfig {

    /**
     * AI服务提供商
     * 支持: hunyuan, deepseek, qwen等
     */
    private String provider = "hunyuan";

    /**
     * API密钥
     * - 腾讯混元: SecretId/SecretKey
     * - DeepSeek: sk-xxx
     * - 通义千问: sk-xxx
     */
    private String apiKey;

    /**
     * API基础URL
     * - 腾讯混元: https://api.hunyuan.cloud.tencent.com/v1
     * - DeepSeek: https://api.deepseek.com/v1
     * - 通义千问: https://dashscope.aliyuncs.com/compatible-mode/v1
     */
    private String apiUrl;

    /**
     * 模型名称
     * - 腾讯混元: hunyuan-turbo, hunyuan-pro等
     * - DeepSeek: deepseek-chat
     * - 通义千问: qwen-turbo, qwen-plus等
     */
    private String model;

    /** 请求超时时间（毫秒） */
    private Long timeout = 60000L;

    /** 最大生成Token数 */
    private Integer maxTokens = 2000;

    /** 温度参数（0-2） 越高越随机，越低越确定 */
    private Double temperature = 0.7;

    /**
     * 获取系统提示词（SFBT疗法）
     */
    public String getSystemPrompt() {
        return """
                你是一位专业的心理健康咨询助手，采用解决方案导向短期治疗（SFBT）的理念。
                
                核心原则：
                1. 关注解决方案而非问题本身
                2. 重视来访者的优势和资源
                3. 设定清晰、具体、可实现的目标
                4. 探索例外情况（问题不存在或较轻的时刻）
                5. 使用量尺技术评估进展
                
                对话风格：
                - 温暖、共情、非评判
                - 简洁明了，避免专业术语
                - 多使用开放式提问
                - 积极倾听，给予正向反馈
                
                注意事项：
                - 不做诊断，不开药物建议
                - 遇到严重心理危机，建议专业求助
                - 保持专业边界
                - 尊重来访者的价值观和选择
                
                请用温暖、专业的方式与用户对话，帮助他们找到自己的解决方案。
                """;
    }
}