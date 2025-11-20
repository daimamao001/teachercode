package com.health.agent.module.ai.client;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.health.agent.common.exception.BusinessException;
import com.health.agent.module.ai.dto.AIRequestDTO;
import com.health.agent.module.ai.dto.AIResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * 腾讯混元大模型客户端实现（传统签名方式）
 * 
 * 注意：此实现已被TencentHunyuanClient替代
 * 保留此类仅作为备份参考，不再作为Spring Bean加载
 * 
 * @author Health Agent Team
 * @date 2025-01-01
 * @deprecated 使用 TencentHunyuanClient 替代
 */
@Slf4j
// @Component("hunyuanLegacyClient")  // 已禁用，使用 TencentHunyuanClient 替代
@Deprecated
public class HunyuanClient implements AIClient {
    
    @Value("${ai.provider}")
    private String provider;
    
    @Value("${ai.model}")
    private String defaultModel;
    
    @Value("${ai.secret-id}")
    private String secretId;
    
    @Value("${ai.secret-key}")
    private String secretKey;
    
    @Value("${ai.timeout:60000}")
    private Integer timeout;
    
    @Value("${ai.max-tokens:2000}")
    private Integer defaultMaxTokens;
    
    // 混元API端点
    private static final String SERVICE = "hunyuan";
    private static final String HOST = "hunyuan.tencentcloudapi.com";
    private static final String ENDPOINT = "https://" + HOST;
    private static final String VERSION = "2023-09-01";
    private static final String ACTION = "ChatCompletions";
    private static final String REGION = "ap-guangzhou";
    private static final String ALGORITHM = "TC3-HMAC-SHA256";
    
    @Override
    public AIResponseDTO chat(AIRequestDTO request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 参数校验
            validateRequest(request);
            
            // 构建请求�?
            JSONObject requestBody = buildRequestBody(request);
            
            // 构建签名和请求头
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            TreeMap<String, String> headers = buildHeaders(requestBody.toString(), timestamp);
            
            // 发送请�?
            log.info("发送混元AI请求，model: {}, messages: {}", 
                    request.getModel(), request.getMessages().size());
            
            HttpResponse response = HttpRequest.post(ENDPOINT)
                    .headerMap(headers, false)
                    .body(requestBody.toString())
                    .timeout(timeout)
                    .execute();
            
            // 解析响应
            AIResponseDTO result = parseResponse(response, request);
            result.setResponseTime(System.currentTimeMillis() - startTime);
            
            log.info("混元AI响应成功，耗时: {}ms, tokens: {}", 
                    result.getResponseTime(),
                    result.getUsage() != null ? result.getUsage().getTotalTokens() : 0);
            
            return result;
            
        } catch (Exception e) {
            log.error("调用混元AI失败", e);
            return AIResponseDTO.builder()
                    .success(false)
                    .errorMessage("AI调用失败: " + e.getMessage())
                    .responseTime(System.currentTimeMillis() - startTime)
                    .build();
        }
    }
    
    /**
     * 校验请求参数
     */
    private void validateRequest(AIRequestDTO request) {
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            throw new BusinessException("消息列表不能为空");
        }
    }
    
    /**
     * 构建请求�?
     */
    private JSONObject buildRequestBody(AIRequestDTO request) {
        JSONObject body = new JSONObject();
        
        // 模型
        body.set("Model", StrUtil.isNotBlank(request.getModel()) ? request.getModel() : defaultModel);
        
        // 消息列表
        JSONArray messages = new JSONArray();
        for (AIRequestDTO.Message msg : request.getMessages()) {
            JSONObject message = new JSONObject();
            message.set("Role", msg.getRole());
            message.set("Content", msg.getContent());
            messages.add(message);
        }
        body.set("Messages", messages);
        
        // 可选参�?
        if (request.getMaxTokens() != null) {
            body.set("MaxTokens", request.getMaxTokens());
        } else {
            body.set("MaxTokens", defaultMaxTokens);
        }
        
        if (request.getTemperature() != null) {
            body.set("Temperature", request.getTemperature());
        }
        
        if (request.getTopP() != null) {
            body.set("TopP", request.getTopP());
        }
        
        // 是否流式
        body.set("Stream", request.getStream() != null && request.getStream());
        
        return body;
    }
    
    /**
     * 构建请求头（包含腾讯云签名）
     */
    private TreeMap<String, String> buildHeaders(String payload, String timestamp) throws Exception {
        // 时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = sdf.format(new Date(Long.parseLong(timestamp) * 1000));
        
        // ************* 步骤 1：拼接规范请求串 *************
        String httpRequestMethod = "POST";
        String canonicalUri = "/";
        String canonicalQueryString = "";
        String canonicalHeaders = "content-type:application/json\n" + 
                                 "host:" + HOST + "\n";
        String signedHeaders = "content-type;host";
        String hashedRequestPayload = sha256Hex(payload);
        String canonicalRequest = httpRequestMethod + "\n" +
                                canonicalUri + "\n" +
                                canonicalQueryString + "\n" +
                                canonicalHeaders + "\n" +
                                signedHeaders + "\n" +
                                hashedRequestPayload;
        
        // ************* 步骤 2：拼接待签名字符�?*************
        String credentialScope = date + "/" + SERVICE + "/tc3_request";
        String hashedCanonicalRequest = sha256Hex(canonicalRequest);
        String stringToSign = ALGORITHM + "\n" +
                            timestamp + "\n" +
                            credentialScope + "\n" +
                            hashedCanonicalRequest;
        
        // ************* 步骤 3：计算签�?*************
        byte[] secretDate = hmac256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmac256(secretDate, SERVICE);
        byte[] secretSigning = hmac256(secretService, "tc3_request");
        String signature = bytesToHex(hmac256(secretSigning, stringToSign));
        
        // ************* 步骤 4：拼�?Authorization *************
        String authorization = ALGORITHM + " " +
                             "Credential=" + secretId + "/" + credentialScope + ", " +
                             "SignedHeaders=" + signedHeaders + ", " +
                             "Signature=" + signature;
        
        // 构建请求�?
        TreeMap<String, String> headers = new TreeMap<>();
        headers.put("Authorization", authorization);
        headers.put("Content-Type", "application/json");
        headers.put("Host", HOST);
        headers.put("X-TC-Action", ACTION);
        headers.put("X-TC-Timestamp", timestamp);
        headers.put("X-TC-Version", VERSION);
        headers.put("X-TC-Region", REGION);
        
        return headers;
    }
    
    /**
     * 解析响应
     */
    private AIResponseDTO parseResponse(HttpResponse response, AIRequestDTO request) {
        String body = response.body();
        
        if (response.getStatus() != 200) {
            log.error("混元API返回错误: status={}, body={}", response.getStatus(), body);
            return AIResponseDTO.failure("API调用失败: " + body);
        }
        
        try {
            JSONObject json = JSONUtil.parseObj(body);
            JSONObject responseData = json.getJSONObject("Response");
            
            // 检查错�?
            if (responseData.containsKey("Error")) {
                JSONObject error = responseData.getJSONObject("Error");
                String errorMsg = error.getStr("Message", "Unknown error");
                log.error("混元API返回错误: {}", errorMsg);
                return AIResponseDTO.failure(errorMsg);
            }
            
            // 提取回复内容
            JSONArray choices = responseData.getJSONArray("Choices");
            if (choices == null || choices.isEmpty()) {
                return AIResponseDTO.failure("响应中没有choices");
            }
            
            JSONObject choice = choices.getJSONObject(0);
            JSONObject message = choice.getJSONObject("Message");
            String content = message.getStr("Content");
            
            // 提取Token使用情况
            AIResponseDTO.Usage usage = null;
            if (responseData.containsKey("Usage")) {
                JSONObject usageJson = responseData.getJSONObject("Usage");
                usage = AIResponseDTO.Usage.builder()
                        .promptTokens(usageJson.getInt("PromptTokens", 0))
                        .completionTokens(usageJson.getInt("CompletionTokens", 0))
                        .totalTokens(usageJson.getInt("TotalTokens", 0))
                        .build();
            }
            
            return AIResponseDTO.builder()
                    .success(true)
                    .content(content)
                    .model(request.getModel())
                    .usage(usage)
                    .requestId(responseData.getStr("RequestId"))
                    .build();
                    
        } catch (Exception e) {
            log.error("解析混元响应失败", e);
            return AIResponseDTO.failure("解析响应失败: " + e.getMessage());
        }
    }
    
    // ================= 加密工具方法 =================
    
    private static String sha256Hex(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(d);
    }
    
    private static byte[] hmac256(byte[] key, String msg) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, mac.getAlgorithm());
        mac.init(secretKeySpec);
        return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}



