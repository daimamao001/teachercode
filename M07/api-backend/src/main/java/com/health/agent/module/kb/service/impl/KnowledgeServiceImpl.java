package com.health.agent.module.kb.service.impl;

import cn.hutool.core.util.StrUtil;
import com.health.agent.module.ai.client.AIClient;
import com.health.agent.module.ai.dto.AIRequestDTO;
import com.health.agent.module.ai.dto.AIResponseDTO;
import com.health.agent.module.kb.entity.Knowledge;
import com.health.agent.module.kb.client.RAGServiceClient;
import com.health.agent.module.kb.mapper.KnowledgeMapper;
import com.health.agent.module.kb.service.IKnowledgeService;
import com.health.agent.module.kb.vo.KnowledgeVO;
import com.health.agent.module.kb.vo.RAGQueryResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements IKnowledgeService {

    private final KnowledgeMapper knowledgeMapper;
    private final AIClient aiClient;
    private final RAGServiceClient ragServiceClient;

    @Override
    @Transactional
    public int buildKnowledge(String title, String content, String category, String source, String keywords, int chunkSize, int chunkOverlap) {
        if (StrUtil.isBlank(title) || StrUtil.isBlank(content) || StrUtil.isBlank(category)) {
            throw new IllegalArgumentException("标题、内容、分类不能为空");
        }
        if (chunkSize <= 0) chunkSize = 500;
        if (chunkOverlap < 0) chunkOverlap = 0;

        List<String> chunks = splitText(content, chunkSize, chunkOverlap);
        int count = 0;
        for (int i = 0; i < chunks.size(); i++) {
            String chunkTitle = title + " (片段" + (i + 1) + ")";
            Knowledge k = Knowledge.builder()
                    .title(chunkTitle)
                    .content(chunks.get(i))
                    .category(category)
                    .keywords(StrUtil.emptyToNull(keywords))
                    .source(StrUtil.emptyToNull(source))
                    .status(1)
                    .embedding(null) // 预留：可后续写入向量
                    .build();
            count += knowledgeMapper.insert(k);
        }
        log.info("知识库构建完成：{} -> 分片数 {}", title, count);
        return count;
    }

    @Override
    public RAGQueryResultVO queryKnowledge(String question, String category, int topK, boolean withAnswer) {
        if (StrUtil.isBlank(question)) {
            throw new IllegalArgumentException("问题不能为空");
        }
        if (topK <= 0) topK = 5;

        List<Knowledge> list = knowledgeMapper.fulltextSearch(question, category, topK);
        if (list == null || list.isEmpty()) {
            list = knowledgeMapper.likeSearch(question, category, topK);
        }

        List<KnowledgeVO> matches = list.stream().map(k -> KnowledgeVO.builder()
                .id(k.getId())
                .title(k.getTitle())
                .content(k.getContent())
                .category(k.getCategory())
                .keywords(k.getKeywords())
                .source(k.getSource())
                .build()).collect(Collectors.toList());

        String context = buildContext(matches, 4000);

        String answer = null;
        if (withAnswer) {
            AIRequestDTO req = AIRequestDTO.builder()
                    .build()
                    .addSystemMessage("你是心理健康领域的知识助手。请严格依据提供的知识库片段回答用户问题。若片段不足以回答，请明确说明'信息不足'，并给出建议的检索方向。输出包含：简洁回答 + 参考片段标题列表。")
                    .addUserMessage("问题：" + question + "\n\n知识库片段：\n" + context);
            AIResponseDTO resp = aiClient.chat(req);
            if (Boolean.TRUE.equals(resp.getSuccess())) {
                answer = resp.getContent();
            } else {
                answer = "生成答案失败：" + resp.getErrorMessage();
            }
        }

        return RAGQueryResultVO.builder()
                .matches(matches)
                .context(context)
                .answer(answer)
                .build();
    }

    @Override
    public RAGQueryResultVO queryKnowledgeAdvanced(String question,
                                                   String category,
                                                   int topK,
                                                   boolean withAnswer,
                                                   String mode,
                                                   Double alpha,
                                                   Double beta) {
        if (StrUtil.isBlank(question)) {
            throw new IllegalArgumentException("问题不能为空");
        }
        if (topK <= 0) topK = 5;

        String m = (mode == null || mode.isBlank()) ? "keyword" : mode.toLowerCase();
        List<KnowledgeVO> matches;
        switch (m) {
            case "vector":
                matches = ragServiceClient.search(question, topK, category);
                break;
            case "hybrid":
                matches = ragServiceClient.hybridSearch(question, topK, category, alpha, beta);
                break;
            case "keyword":
            default:
                // 回退到原有关键词/全文检索
                return queryKnowledge(question, category, topK, withAnswer);
        }

        String context = buildContext(matches, 4000);
        String answer = null;
        if (withAnswer) {
            AIRequestDTO req = AIRequestDTO.builder()
                    .build()
                    .addSystemMessage("你是心理健康领域的知识助手。请严格依据提供的知识库片段回答用户问题。若片段不足以回答，请明确说明'信息不足'，并给出建议的检索方向。输出包含：简洁回答 + 参考片段标题列表。")
                    .addUserMessage("问题：" + question + "\n\n知识库片段：\n" + context);
            AIResponseDTO resp = aiClient.chat(req);
            if (Boolean.TRUE.equals(resp.getSuccess())) {
                answer = resp.getContent();
            } else {
                answer = "生成答案失败：" + resp.getErrorMessage();
            }
        }

        return RAGQueryResultVO.builder()
                .matches(matches)
                .context(context)
                .answer(answer)
                .build();
    }

    private List<String> splitText(String text, int size, int overlap) {
        List<String> result = new ArrayList<>();
        if (StrUtil.isBlank(text)) return result;
        int len = text.length();
        int start = 0;
        while (start < len) {
            int end = Math.min(len, start + size);
            String chunk = text.substring(start, end);
            result.add(chunk);
            if (end >= len) break;
            start = end - overlap;
            if (start < 0) start = 0;
        }
        return result;
    }

    private String buildContext(List<KnowledgeVO> matches, int maxChars) {
        StringBuilder sb = new StringBuilder();
        for (KnowledgeVO vo : matches) {
            sb.append("[标题] ").append(vo.getTitle()).append("\n")
              .append(vo.getContent()).append("\n---\n");
            if (sb.length() > maxChars) break;
        }
        String ctx = sb.toString();
        if (ctx.length() > maxChars) {
            ctx = ctx.substring(0, maxChars);
        }
        return ctx;
    }
}