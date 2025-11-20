package com.health.agent.module.kb.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.agent.config.RAGConfig;
import com.health.agent.module.kb.vo.KnowledgeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 调用 Python rag-service 的客户端（向量检索与混合检索）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RAGServiceClient {

    private final RAGConfig ragConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate restTemplate;

    private RestTemplate rest() {
        if (restTemplate != null) return restTemplate;
        // 简易超时设置（非阻塞）：使用 HttpComponents/OkHttp 更佳，这里用默认即可
        restTemplate = new RestTemplate();
        return restTemplate;
    }

    /** 健康检查 */
    public Map<String, Object> health() {
        String url = ragConfig.getServiceBaseUrl() + "/health";
        try {
            ResponseEntity<Map> resp = rest().getForEntity(url, Map.class);
            return (Map<String, Object>) Optional.ofNullable(resp.getBody()).orElseGet(HashMap::new);
        } catch (Exception e) {
            log.warn("RAG service health failed: {}", e.getMessage());
            return Map.of("code", -1, "message", e.getMessage());
        }
    }

    /** 向量语义检索 */
    public List<KnowledgeVO> search(String q, Integer topK, String category) {
        if (topK == null || topK <= 0) topK = 5;
        UriComponentsBuilder b = UriComponentsBuilder
                .fromHttpUrl(ragConfig.getServiceBaseUrl() + "/search")
                .queryParam("q", q)
                .queryParam("topK", topK);
        if (category != null && !category.isBlank()) {
            b.queryParam("category", category);
        }
        try {
            ResponseEntity<Map> resp = rest().getForEntity(b.toUriString(), Map.class);
            Map body = resp.getBody();
            if (body != null && Objects.equals(0, body.get("code"))) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
                return toVOList(data);
            }
        } catch (Exception e) {
            log.warn("RAG vector search failed: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /** 混合检索 */
    public List<KnowledgeVO> hybridSearch(String q, Integer topK, String category, Double alpha, Double beta) {
        if (topK == null || topK <= 0) topK = 5;
        if (alpha == null) alpha = ragConfig.getAlpha();
        if (beta == null) beta = ragConfig.getBeta();
        String url = ragConfig.getServiceBaseUrl() + "/hybrid-search";
        Map<String, Object> payload = new HashMap<>();
        payload.put("q", q);
        payload.put("topK", topK);
        if (category != null && !category.isBlank()) payload.put("category", category);
        payload.put("alpha", alpha);
        payload.put("beta", beta);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(payload), headers);
            ResponseEntity<Map> resp = rest().postForEntity(url, entity, Map.class);
            Map body = resp.getBody();
            if (body != null && Objects.equals(0, body.get("code"))) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
                return toVOList(data);
            }
        } catch (Exception e) {
            log.warn("RAG hybrid search failed: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /** 原始文本入库（分片后入向量库） */
    public int ingestRaw(String title, String category, String text, String keywords, Integer chunkSize, Integer chunkOverlap) {
        String url = ragConfig.getServiceBaseUrl() + "/ingest";
        Map<String, Object> payload = new HashMap<>();
        payload.put("source", "raw");
        payload.put("title", title);
        payload.put("category", category);
        payload.put("text", text);
        if (keywords != null) payload.put("keywords", keywords);
        payload.put("chunkSize", Optional.ofNullable(chunkSize).orElse(500));
        payload.put("chunkOverlap", Optional.ofNullable(chunkOverlap).orElse(50));
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(payload), headers);
            ResponseEntity<Map> resp = rest().postForEntity(url, entity, Map.class);
            Map body = resp.getBody();
            if (body != null && Objects.equals(0, body.get("code"))) {
                Map data = (Map) body.get("data");
                Object ingested = data != null ? data.get("ingested") : 0;
                return ingested instanceof Number ? ((Number) ingested).intValue() : 0;
            }
        } catch (Exception e) {
            log.warn("RAG ingest raw failed: {}", e.getMessage());
        }
        return 0;
    }

    private List<KnowledgeVO> toVOList(List<Map<String, Object>> data) {
        if (data == null) return Collections.emptyList();
        return data.stream().map(item -> KnowledgeVO.builder()
                .id(safeLong(item.get("id")))
                .title(String.valueOf(item.getOrDefault("title", "")))
                .content(String.valueOf(item.getOrDefault("content", "")))
                .category(strOrNull(item.get("category")))
                .keywords(strOrNull(item.get("keywords")))
                .source(strOrNull(item.get("source")))
                .build()).collect(Collectors.toList());
    }

    private Long safeLong(Object v) {
        try {
            if (v == null) return null;
            if (v instanceof Number) return ((Number) v).longValue();
            String s = String.valueOf(v);
            if (s.isBlank()) return null;
            return Long.parseLong(s);
        } catch (Exception e) {
            return null;
        }
    }

    private String strOrNull(Object v) {
        if (v == null) return null;
        String s = String.valueOf(v);
        return s.isBlank() ? null : s;
    }
}