-- 心理健康Agent平台初始数据
-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库（指定字符集）
CREATE DATABASE IF NOT EXISTS health_agent_db 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE health_agent_db;

-- 插入管理员数据 (密码: admin123，需要在应用中使用BCrypt加密)
INSERT INTO `admin` (`username`, `password`, `nickname`, `email`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@example.com', 1);

-- 插入角色数据
INSERT INTO `role` (`name`, `code`, `description`, `status`) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有所有权限', 1),
('内容管理员', 'CONTENT_ADMIN', '管理内容和文章', 1),
('数据分析员', 'ANALYST', '查看统计数据', 1);

-- 插入评估量表数据
INSERT INTO `assessment` (`name`, `description`, `type`, `total_score`, `question_count`, `time_limit`, `status`, `sort_order`) VALUES
('焦虑自评量表(SAS)', '用于评估个体的焦虑状况，包括心理和躯体症状', 'anxiety', 80, 20, 10, 1, 1),
('抑郁自评量表(SDS)', '用于评估个体的抑郁程度', 'depression', 80, 20, 10, 1, 2),
('压力感知量表(PSS)', '评估个体在过去一个月内对压力的感知程度', 'stress', 40, 10, 5, 1, 3);

-- 插入焦虑量表题目示例
INSERT INTO `assessment_question` (`assessment_id`, `content`, `type`, `options`, `score_rule`, `sort_order`) VALUES
(1, '我觉得比平常容易紧张和着急', 'single', '[{"value": "1", "label": "没有或很少时间"}, {"value": "2", "label": "小部分时间"}, {"value": "3", "label": "相当多时间"}, {"value": "4", "label": "绝大部分或全部时间"}]', '{"1": 1, "2": 2, "3": 3, "4": 4}', 1),
(1, '我无缘无故地感到害怕', 'single', '[{"value": "1", "label": "没有或很少时间"}, {"value": "2", "label": "小部分时间"}, {"value": "3", "label": "相当多时间"}, {"value": "4", "label": "绝大部分或全部时间"}]', '{"1": 1, "2": 2, "3": 3, "4": 4}', 2),
(1, '我容易心里烦乱或感到惊恐', 'single', '[{"value": "1", "label": "没有或很少时间"}, {"value": "2", "label": "小部分时间"}, {"value": "3", "label": "相当多时间"}, {"value": "4", "label": "绝大部分或全部时间"}]', '{"1": 1, "2": 2, "3": 3, "4": 4}', 3);

-- 插入抑郁量表题目示例
INSERT INTO `assessment_question` (`assessment_id`, `content`, `type`, `options`, `score_rule`, `sort_order`) VALUES
(2, '我感到情绪沮丧，郁闷', 'single', '[{"value": "1", "label": "没有或很少时间"}, {"value": "2", "label": "小部分时间"}, {"value": "3", "label": "相当多时间"}, {"value": "4", "label": "绝大部分或全部时间"}]', '{"1": 1, "2": 2, "3": 3, "4": 4}', 1),
(2, '我感到早晨心情最好', 'single', '[{"value": "1", "label": "没有或很少时间"}, {"value": "2", "label": "小部分时间"}, {"value": "3", "label": "相当多时间"}, {"value": "4", "label": "绝大部分或全部时间"}]', '{"1": 4, "2": 3, "3": 2, "4": 1}', 2),
(2, '我要哭或想哭', 'single', '[{"value": "1", "label": "没有或很少时间"}, {"value": "2", "label": "小部分时间"}, {"value": "3", "label": "相当多时间"}, {"value": "4", "label": "绝大部分或全部时间"}]', '{"1": 1, "2": 2, "3": 3, "4": 4}', 3);

-- 插入AI配置示例
INSERT INTO `ai_config` (`name`, `provider`, `model`, `api_url`, `api_key`, `parameters`, `is_default`, `status`) VALUES
('OpenAI GPT-3.5', 'openai', 'gpt-3.5-turbo', 'https://api.openai.com/v1', 'your-api-key-here', '{"temperature": 0.7, "max_tokens": 2000}', 1, 1);

-- 插入Prompt模板
INSERT INTO `prompt_template` (`name`, `type`, `content`, `variables`, `status`) VALUES
('心理咨询助手', 'chat', '你是一位专业、温暖、善解人意的心理健康助手。你的任务是倾听用户的困扰，给予支持和建议。\n\n重要原则：\n1. 保持同理心和温暖的态度\n2. 不要轻易下诊断\n3. 如果用户有严重的心理问题，建议其寻求专业心理咨询师的帮助\n4. 保护用户隐私\n5. 提供建设性的建议\n\n用户说：{{user_input}}', '{"user_input": "用户的输入内容"}', 1),
('评估结果解读', 'assessment', '根据用户的评估结果，提供专业的解读和建议。\n\n评估类型：{{assessment_type}}\n用户得分：{{score}}\n评估结果：{{result_level}}\n\n请提供：\n1. 结果解读\n2. 可能的原因分析\n3. 改善建议\n4. 是否需要寻求专业帮助', '{"assessment_type": "评估类型", "score": "得分", "result_level": "等级"}', 1);

-- 插入知识库数据示例
INSERT INTO `knowledge` (`title`, `content`, `category`, `keywords`, `source`, `status`) VALUES
('什么是焦虑症', '焦虑症是一种常见的心理健康问题，表现为持续的、过度的担忧和恐惧。主要症状包括：心跳加速、出汗、颤抖、呼吸困难、头晕等。焦虑症可以通过心理治疗和药物治疗得到有效控制。', '心理健康', '焦虑,焦虑症,心理健康', '专业资料', 1),
('如何应对压力', '应对压力的方法包括：1. 深呼吸和放松练习；2. 规律运动；3. 保持良好的睡眠习惯；4. 与朋友家人交流；5. 培养兴趣爱好；6. 学会时间管理；7. 必要时寻求专业帮助。', '心理健康', '压力,应对,心理调节', '专业资料', 1),
('抑郁症的预警信号', '抑郁症的常见预警信号包括：持续的悲伤或空虚感、对日常活动失去兴趣、睡眠问题、食欲改变、疲劳、注意力难以集中、自我价值感低落、有自杀想法等。如果出现这些症状持续两周以上，应及时寻求专业帮助。', '心理健康', '抑郁症,症状,预警', '专业资料', 1);

-- 插入文章数据示例
INSERT INTO `article` (`title`, `summary`, `content`, `cover_image`, `category`, `tags`, `author_id`, `status`, `published_at`) VALUES
('如何保持良好的心理健康', '心理健康对于整体健康至关重要。本文介绍了保持心理健康的实用方法。', '# 如何保持良好的心理健康\n\n心理健康是我们整体健康的重要组成部分。以下是一些保持良好心理健康的建议：\n\n## 1. 保持积极的生活态度\n- 培养感恩的心态\n- 关注生活中的美好事物\n- 学会接纳自己的不完美\n\n## 2. 建立良好的社会关系\n- 与家人朋友保持联系\n- 参加社交活动\n- 寻求支持系统\n\n## 3. 照顾好自己的身体\n- 规律运动\n- 健康饮食\n- 充足睡眠\n\n## 4. 学会管理压力\n- 识别压力源\n- 采用放松技巧\n- 合理安排时间\n\n记住，如果感到心理健康出现问题，及时寻求专业帮助是很重要的。', NULL, '心理健康', '心理健康,自我关爱', 1, 1, NOW()),
('认识焦虑：它并不可怕', '焦虑是一种常见的情绪反应。了解焦虑可以帮助我们更好地应对它。', '# 认识焦虑：它并不可怕\n\n焦虑是人类的一种正常情绪反应，但当焦虑过度或持续时间过长时，可能会影响我们的生活质量。\n\n## 什么是焦虑？\n焦虑是面对威胁或不确定情况时产生的担忧和恐惧感。适度的焦虑可以帮助我们保持警觉和做好准备。\n\n## 焦虑的症状\n- 心理症状：担忧、恐惧、注意力难以集中\n- 身体症状：心跳加速、出汗、肌肉紧张、胃部不适\n\n## 如何应对焦虑\n1. 深呼吸练习\n2. 正念冥想\n3. 规律运动\n4. 充足睡眠\n5. 限制咖啡因摄入\n6. 与他人交流\n\n如果焦虑严重影响了您的生活，请不要犹豫寻求专业心理咨询师的帮助。', NULL, '心理健康', '焦虑,情绪管理', 1, 1, NOW());

-- 创建测试用户 (密码: test123)
INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `status`) VALUES
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户', 'test@example.com', 1);
