# AI辅助开发记录

## 文档信息表

| 项目 | 内容 |
|------|------|
| **项目名称** | 校园自习室预约系统（Campus Study Room Reservation System） |
| **文档名称** | AI辅助开发记录 |
| **版本号** | V1.0 |
| **编制日期** | 2026年6月 |
| **开发团队** | 校园自习室开发小组 |
| **使用AI工具** | Claude Opus 4.8、智谱GLM-4 |
| **项目架构** | Spring Cloud微服务 + Vue3前端 + Docker容器化部署 |

---

## 1. 概述

本项目是一个基于微服务架构的校园自习室预约系统，涵盖用户认证、自习室管理、座位预约、考勤签到、AI智能推荐与智能客服等核心模块。在开发过程中，团队积极引入AI工具辅助编码，主要在以下环节获得了显著效率提升：

- **算法设计与实现**：协同过滤推荐算法、RAG知识库检索算法的代码骨架生成
- **服务层开发**：智谱大模型API调用封装、降级兜底策略设计
- **基础设施编排**：Docker Compose多服务健康检查与启动顺序配置
- **数据库优化**：索引设计与SQL调优
- **前端联调**：Vue3 + Element Plus组件开发与接口对接

使用的AI工具包括 **Claude Opus**（用于复杂算法设计、代码生成、架构建议）和 **智谱GLM-4**（用于智能客服大模型接入、推荐理由生成）。

---

## 2. 详细开发记录

### 记录一：协同过滤推荐算法实现

| 项目 | 内容 |
|------|------|
| **编号** | AI-001 |
| **日期** | 2026年5月15日 |
| **开发模块** | campus-ai 服务 — 智能推荐引擎 |
| **开发人员** | 骆家武 |

#### 2.1.1 遇到的问题/需求

系统需要为每位用户智能推荐自习室座位。初期考虑使用基于内容的推荐，但无法挖掘"相似用户"的群体偏好。团队决定引入**用户-物品协同过滤（User-Based Collaborative Filtering）**算法，基于用户历史预约行为计算用户间的余弦相似度，从而推荐相似用户偏好的房间。难点在于：

1. 如何高效构建用户-房间偏好矩阵
2. 如何处理新用户冷启动问题
3. 如何在Java中实现余弦相似度计算并保证性能

#### 2.1.2 给AI的Prompt

```
我正在开发一个校园自习室预约系统的智能推荐模块，使用Spring Boot。
需要实现一个基于用户的协同过滤推荐算法：

1. 数据模型：每个用户有多个预约记录（ReservationRecord包含userId, roomId, seatId, count）
2. 算法要求：
   - 构建用户-房间偏好矩阵（userId -> Map<roomId, count>）
   - 计算目标用户与其他用户的余弦相似度
   - 取Top-10相似用户，聚合他们对房间的偏好
   - 排除目标用户已经去过的房间
   - 返回Top-N推荐结果
3. 需要处理冷启动：新用户没有历史记录时返回空列表，由上层内容推荐兜底
4. 代码风格：使用Java 17，Spring Service，Lombok，Stream API

请生成完整的CollaborativeFilterService.java代码，包含：
- recommendRooms(Long userId, int topN) 方法
- contentBasedRecommend() 冷启动兜底方法（基于房间热门程度）
- 私有辅助方法：buildUserRoomMatrix、cosineSimilarity
- 内部record Recommendation(Long roomId, double score)
```

#### 2.1.3 AI的输出摘要

Claude给出了完整的代码骨架，核心思路包括：

1. **矩阵构建**：使用 `Map<Long, Map<Long, Integer>>` 表示用户-房间偏好矩阵，外层key是userId，内层key是roomId，value是预约次数count
2. **余弦相似度**：遍历两个用户向量的所有key（房间ID），计算点积和各自模长，公式为 `dot / (sqrt(norm1) * sqrt(norm2))`
3. **Top-N聚合**：对相似用户按相似度排序取前10，加权聚合其房间偏好（权重为相似度 × 预约次数），排除目标用户已访问房间
4. **冷启动处理**：当目标用户无历史记录时，直接返回空列表，由调用方触发内容推荐兜底
5. **内容推荐**：统计各房间总预约次数，过滤掉用户已去过的房间，按热度排序返回

#### 2.1.4 人工修改与调整

| 修改项 | 说明 |
|--------|------|
| 相似度阈值过滤 | AI代码中保留了sim <= 0的相似用户，人工添加 `if (sim > 0)` 过滤，避免负相关用户干扰推荐 |
| 归一化逻辑 | AI生成的归一化直接使用相似度之和，人工调整为 `score / totalSim`，使分数落在合理区间 |
| 内容推荐增强 | 在AI基础上增加了 `preferredBuilding`、`preferWindow`、`preferPower` 参数，支持按用户偏好过滤 |
| 日志记录 | 添加 `@Slf4j` 和冷启动日志，便于线上排查 |
| 持久化层 | 补充 `ReservationRecordMapper` 的SQL查询，AI未提供Mapper层实现 |

#### 2.1.5 效果评价

- **节省时间**：算法核心逻辑约30分钟由AI生成，人工调试和优化约1.5小时，总计约2小时完成；若从零手写预计需要4-5小时
- **质量**：余弦相似度计算逻辑正确，数据结构选择合理，Stream API使用规范
- **踩坑**：AI最初生成的代码未处理 `targetVector` 为null的情况，导致NullPointerException；人工添加 `getOrDefault` 和空判断后修复

---

### 记录二：RAG知识库检索客服实现

| 项目 | 内容 |
|------|------|
| **编号** | AI-002 |
| **日期** | 2026年5月18日 |
| **开发模块** | campus-ai 服务 — 智能客服RAG检索 |
| **开发人员** | 骆家武 |

#### 2.2.1 遇到的问题/需求

智能客服需要基于知识库回答用户问题。由于项目预算和复杂度限制，不引入向量数据库（如Milvus、Elasticsearch），需要在MySQL中实现轻量级的RAG（Retrieval-Augmented Generation）检索。要求：

1. 支持中文分词和关键词匹配
2. 对标题、关键词、内容设置不同的权重
3. 考虑文档浏览量作为辅助排序因子
4. 检索结果用于构造增强版prompt，供大模型生成回答

#### 2.2.2 给AI的Prompt

```
我正在实现一个轻量级RAG知识库检索服务，用于校园自习室智能客服。
技术约束：
- 不使用向量数据库，直接在MySQL的knowledge_base表上检索
- 表结构：knowledge_id, category, title, content, keywords, view_count, is_active
- 需要支持中文和英文混合查询

请帮我设计并实现RagService.java：

1. 中文分词策略：
   - 先按空格和标点拆分
   - 对中文部分再按2-gram提取（如"如何预约" -> "如何", "何预", "预约"）
   - 过滤长度小于2的token
2. 打分规则：
   - title匹配：+3.0分
   - keywords匹配：+2.0分
   - content匹配：+1.0分
   - 浏览量辅助：log(view_count + 1) * 0.1
3. 返回按分数降序排列的Top-K文档
4. 使用Spring Boot + MyBatis，Java 17

请生成完整的Service代码，包含tokenize()和calculateScore()方法。
```

#### 2.2.3 AI的输出摘要

Claude生成的核心思路：

1. **分词策略**：采用"空格标点拆分 + 中文2-gram"的混合方案。英文/数字按空格和标点切分，中文文本再按相邻两个字符组合提取
2. **权重打分**：title(3分) > keywords(2分) > content(1分)，体现字段重要性差异
3. **浏览量因子**：使用 `Math.log(Math.max(doc.getViewCount(), 1) + 1) * 0.1`，避免热门文档过度主导排序
4. **去重处理**：使用 `stream().distinct()` 对token去重，避免重复计分

#### 2.2.4 人工修改与调整

| 修改项 | 说明 |
|--------|------|
| 空值安全 | AI代码中 `doc.getTitle()` 等未做空值处理，人工添加 `== null ? ""` 保护 |
| 大小写统一 | 添加 `.toLowerCase()` 确保大小写不敏感匹配 |
| 2-gram范围 | AI生成的2-gram循环边界有off-by-one风险，人工修正为 `i < part.length() - 1` |
| 性能优化 | 添加 `if (score > 0)` 过滤无匹配文档，减少后续排序数据量 |
| 停用词 | 实际应用中补充了常见停用词过滤（如"的","了","吗"），AI未提供 |

#### 2.2.5 效果评价

- **节省时间**：分词和打分逻辑约20分钟由AI生成，人工调优和边界处理约1小时；相比从零研究中文分词方案节省约3小时
- **质量**：权重设计合理，2-gram策略对中文短查询效果良好，浏览量平滑因子设计巧妙
- **踩坑**：初期测试发现"如何预约自习室"查询时，2-gram产生了大量无意义组合（如"何预"），后补充停用词过滤和长度校验（`part.length() >= 2`）后改善

---

### 记录三：智谱大模型调用与本地兜底设计

| 项目 | 内容 |
|------|------|
| **编号** | AI-003 |
| **日期** | 2026年5月20日 |
| **开发模块** | campus-ai 服务 — 大模型API封装与降级策略 |
| **开发人员** | 骆家武 |

#### 2.3.1 遇到的问题/需求

系统需要接入大模型提供智能客服对话和推荐理由生成。选择智谱GLM-4作为主力模型，但面临以下挑战：

1. 智谱API需要配置API Key，在开发/测试环境可能没有Key
2. API调用存在网络超时、服务不可用等风险
3. 需要统一的异常处理和降级机制，确保客服服务不中断
4. 需要本地兜底方案，在无网络或大模型不可用时仍能回答常见问题

#### 2.3.2 给AI的Prompt

```
我需要为校园自习室系统封装一个智谱GLM-4大模型调用服务，使用Spring Boot。

需求：
1. 配置项：base-url（默认https://open.bigmodel.cn/api/paas/v4）、api-key、model（默认glm-4）
2. 方法：
   - chat(String systemPrompt, String userMessage, List<Map<String,String>> history)：通用对话
   - generateRecommendationReason(String building, String seatType, boolean hasPower, boolean preferWindow, boolean preferPower)：生成推荐理由
3. 降级策略：
   - 如果apiKey未配置或为空，使用本地知识库回答
   - 如果API调用失败（超时、异常），也使用本地知识库回答
   - 本地回答需要覆盖：预约流程、签到签退、取消预约、违规处理、开放时间、电源/靠窗等常见问题
4. 使用RestTemplate发起HTTP请求，Jackson解析JSON响应

请生成完整的ZhipuAiService.java，包含完整的本地兜底逻辑。
```

#### 2.3.3 AI的输出摘要

Claude生成的核心架构：

1. **配置注入**：使用 `@Value` 注入 `base-url`、`api-key`、`model`，支持外部化配置
2. **请求构造**：使用 `ObjectNode` 构建JSON请求体，包含system消息、history消息和user消息
3. **异常降级**：try-catch包裹API调用，任何异常都路由到 `localChatAnswer()`
4. **本地兜底**：基于关键词匹配的硬编码回答，覆盖10类常见问题，使用 `contains()` 辅助方法进行多关键词匹配
5. **推荐理由生成**：本地版本使用StringBuilder拼接固定模板，根据seatType（window/corner/power）生成不同描述

#### 2.3.4 人工修改与调整

| 修改项 | 说明 |
|--------|------|
| API Key空判断 | AI仅检查null，人工补充 `.isBlank()` 处理空白字符串 |
| 响应解析增强 | AI的 `extractContent` 较简单，人工增加对 `choices` 数组非空判断和默认值处理 |
| 本地回答扩展 | AI生成5条本地回答，人工扩展至10条，覆盖更多场景（靠窗、AI推荐、问候语等） |
| 日志级别 | 添加 `log.warn` 记录降级事件，`log.error` 记录API异常，便于监控 |
| 环境变量注入 | 在docker-compose.yml中添加 `ZHIPU_API_KEY: ${ZHIPU_API_KEY:-}` 支持运行时注入 |

#### 2.3.5 效果评价

- **节省时间**：API封装和基础降级逻辑约25分钟由AI生成，人工扩展本地知识库和调优约1.5小时；相比从零研究智谱API文档节省约2小时
- **质量**：降级策略设计合理，双层兜底（配置层 + 运行时层）确保高可用，本地回答覆盖主要场景
- **踩坑**：初期AI生成的 `extractContent` 在智谱返回特殊格式时解析失败，人工增加多层path访问和try-catch后稳定；另外发现本地兜底中的emoji（如"📚"）在某些终端显示异常，保留但记录为已知问题

---

### 记录四：AI控制器与多策略推荐整合

| 项目 | 内容 |
|------|------|
| **编号** | AI-004 |
| **日期** | 2026年5月22日 |
| **开发模块** | campus-ai 服务 — AIController REST API |
| **开发人员** | 骆家武、何展霖 |

#### 2.4.1 遇到的问题/需求

需要将协同过滤推荐、内容推荐、RAG检索、智谱大模型等能力整合为统一的REST API，供前端调用。具体需求：

1. `/api/ai/recommendations`：获取智能推荐，支持协同过滤 + 内容推荐兜底 + 推荐理由生成
2. `/api/ai/chat`：AI客服对话，支持RAG检索增强 + 大模型回答
3. 推荐结果需要持久化到数据库，便于后续分析推荐效果
4. 需要处理JWT认证，从请求头提取用户ID

#### 2.4.2 给AI的Prompt

```
请帮我设计一个Spring Boot REST控制器 AIController.java，整合以下AI服务能力：

已有依赖：
- ZhipuAiService：大模型对话和推荐理由生成
- CollaborativeFilterService：协同过滤推荐 + 内容推荐兜底
- RagService：知识库检索
- RoomSeatMapper：查询房间和座位信息
- AiRecommendationMapper：持久化推荐记录
- JwtUtils：JWT解析

API设计：
1. POST /api/ai/recommendations
   - 请求体：date, building, preferWindow, preferPower
   - 流程：
     a. 从JWT获取userId
     b. 调用协同过滤推荐房间（top 10）
     c. 如果结果不足3个，用内容推荐补充
     d. 为每个推荐房间找最佳可用座位（优先匹配用户偏好类型）
     e. 调用智谱服务生成推荐理由（或本地兜底）
     f. 构造RecommendationVO，设置score范围0.60-0.99
     g. 持久化到ai_recommendation表
     h. 如果全部为空，返回热门推荐兜底
   - 返回：List<RecommendationVO>

2. POST /api/ai/chat
   - 请求体：message, history
   - 流程：
     a. 调用RagService检索相关文档（top 3）
     b. 如果有文档，构造增强system prompt（包含知识库内容）
     c. 如果没有文档，使用默认知识prompt
     d. 调用ZhipuAiService.chat生成回答
     d. 返回reply和relatedDocs列表

请使用Spring Boot 3.x, Swagger注解(@Tag, @Operation), lombok。
```

#### 2.4.3 AI的输出摘要

Claude生成的完整控制器包含：

1. **推荐流程编排**：清晰的5步流水线 — 协同过滤 -> 冷启动兜底 -> 座位匹配 -> 理由生成 -> 持久化
2. **座位匹配策略**：`findBestSeat()` 优先按用户偏好类型（window/power）查找，否则返回任意可用座位
3. **RAG增强对话**：检索到文档时，将知识库内容注入system prompt；未检索到时使用默认规则prompt
4. **VO设计**：内联定义 `RecommendRequest`、`RecommendationVO`、`ChatRequest`、`ChatResponse`，减少文件数量
5. **策略标签**：使用 `STRATEGIES` 数组循环分配推荐策略名称，增加可解释性

#### 2.4.4 人工修改与调整

| 修改项 | 说明 |
|--------|------|
| 去重逻辑 | AI未处理协同过滤和内容推荐结果可能重复的问题，人工添加 `existingRoomIds` Set去重 |
| Score范围限制 | 人工添加 `Math.min(0.99, Math.max(0.60, rec.score()))` 确保分数在合理展示区间 |
| 空值处理 | 对 `request.getPreferWindow()` 等Boolean添加null判断，避免NPE |
| 异常边界 | 当room或seat查询为null时添加continue跳过，防止NullPointerException |
| 热门兜底 | 补充 `fallbackRecommendations()` 方法，当所有推荐都为空时返回热门房间 |

#### 2.4.5 效果评价

- **节省时间**：控制器骨架和流程编排约30分钟由AI生成，人工边界处理和联调约2小时；相比从零设计API节省约3小时
- **质量**：流程设计清晰，职责分离合理，Swagger注解完整
- **踩坑**：AI生成的 `findBestSeat` 逻辑中 `preferredType` 为null时直接查询所有座位，但AI未处理 `selectAvailableSeatsByType` 返回空的情况，人工添加回退逻辑；另外AI生成的score未限制范围，前端展示时出现1.23等异常值，已修复

---

### 记录五：Docker Compose健康检查与启动顺序排错

| 项目 | 内容 |
|------|------|
| **编号** | AI-005 |
| **日期** | 2026年5月25日 |
| **开发模块** | 基础设施 — Docker Compose编排 |
| **开发人员** | 骆家武 |

#### 2.5.1 遇到的问题/需求

项目采用微服务架构，包含MySQL、Redis、Nacos、6个业务微服务和1个前端服务，共10个容器。初次编写docker-compose.yml时遇到严重问题：

1. 微服务启动时MySQL尚未就绪，导致连接失败和无限重启
2. Nacos启动较慢（需要60秒以上），其他服务先启动后无法注册
3. Gateway依赖所有业务服务，但业务服务启动有快有慢
4. 没有健康检查机制，无法判断服务真实可用状态

#### 2.5.2 给AI的Prompt

```
我的Spring Cloud微服务项目有10个Docker服务需要编排，遇到启动顺序问题。
服务列表：mysql、redis、nacos、campus-auth、campus-user、campus-room、campus-reservation、campus-attendance、campus-ai、campus-gateway、campus-frontend

依赖关系：
- 所有业务服务（auth, user, room, reservation, attendance, ai）依赖 mysql + redis + nacos
- gateway 依赖所有业务服务
- frontend 依赖 gateway

问题：
1. mysql启动需要时间，业务服务启动时连接失败
2. nacos启动很慢（需要60秒以上）
3. 需要健康检查确保服务真正可用后才启动下游

请帮我设计一个docker-compose.yml，要求：
1. 为 mysql、redis、nacos 配置健康检查（healthcheck）
2. 业务服务使用 depends_on + condition: service_healthy 等待基础服务就绪
3. gateway 使用 condition: service_started 等待业务服务（不需要等健康，只要启动即可）
4. 使用统一网络 campus-network
5. mysql数据持久化到命名卷
6. 暴露必要端口
```

#### 2.5.3 AI的输出摘要

Claude生成的docker-compose.yml核心设计：

1. **健康检查配置**：
   - MySQL：`mysqladmin ping -h localhost -u root -p123456`，间隔10秒，重试10次，启动宽限期30秒
   - Redis：`redis-cli ping`，间隔10秒，重试5次，启动宽限期10秒
   - Nacos：`curl -f http://localhost:8848/nacos/actuator/health`，间隔10秒，重试10次，启动宽限期60秒

2. **依赖编排**：
   - 所有业务服务 `depends_on` MySQL/Redis/Nacos，condition均为 `service_healthy`
   - Gateway `depends_on` 所有业务服务，condition为 `service_started`（不需要健康检查，容器启动即可）
   - Frontend `depends_on` Gateway

3. **网络与存储**：统一使用 `campus-network` bridge网络，MySQL数据卷 `mysql-data` 持久化

#### 2.5.4 人工修改与调整

| 修改项 | 说明 |
|--------|------|
| Nacos数据库 | AI默认使用外部MySQL，人工修改为内置Derby（`SPRING_DATASOURCE_PLATFORM: ""`），减少依赖 |
| 端口映射 | 补充Nacos gRPC端口 `9848:9848`，支持Nacos 2.x客户端长连接 |
| 环境变量 | 为campus-ai添加 `ZHIPU_API_KEY: ${ZHIPU_API_KEY:-}`，支持从宿主机环境变量注入API Key |
| 启动顺序微调 | 实际测试发现campus-gateway偶发NPE（业务服务注册有延迟），人工在gateway中添加 @Retryable 重试机制 |
| 前端依赖 | 确认frontend使用 `depends_on: campus-gateway` 即可，nginx配置中已包含proxy_pass到gateway |

#### 2.5.5 效果评价

- **节省时间**：docker-compose.yml骨架约15分钟由AI生成，人工调优健康检查参数和测试约2小时；相比从零研究Docker Compose条件依赖节省约2小时
- **质量**：健康检查命令选择准确，启动宽限期设置合理，依赖关系清晰
- **踩坑**：
  - Nacos 2.x需要额外暴露9848端口用于gRPC，AI未提及，人工补充
  - 初期 `start_period` 设置过短（30秒），Nacos在慢速机器上启动超时，调整为60秒后解决
  - `condition: service_healthy` 需要Docker Compose 2.20+版本支持，团队开发环境版本过低，升级后解决

---

### 记录六：数据库索引优化（reservation/seat复合索引）

| 项目 | 内容 |
|------|------|
| **编号** | AI-006 |
| **日期** | 2026年5月28日 |
| **开发模块** | 数据库层 — SQL性能优化 |
| **开发人员** | 骆家武 |

#### 2.6.1 遇到的问题/需求

在性能测试中发现以下慢查询：

1. 查询某用户在特定日期的预约：`SELECT * FROM reservation WHERE user_id = ? AND reserve_date = ?`，全表扫描
2. 查询某座位的可用状态：`SELECT * FROM seat WHERE room_id = ? AND status = 'AVAILABLE'`，全表扫描
3. 查询某房间某日期下的预约冲突：`SELECT * FROM reservation WHERE room_id = ? AND seat_id = ? AND reserve_date = ?`，全表扫描

需要为 `reservation` 和 `seat` 表设计合理的复合索引。

#### 2.6.2 给AI的Prompt

```
我正在优化校园自习室系统的MySQL数据库性能。请帮我分析以下表的索引设计：

表1：reservation（预约表）
- reservation_id BIGINT PK
- user_id BIGINT NOT NULL
- room_id BIGINT NOT NULL
- seat_id BIGINT NOT NULL
- slot_id BIGINT NOT NULL
- reserve_date DATE NOT NULL
- status ENUM('PENDING','CONFIRMED','CANCELLED','CHECKED_IN','COMPLETED','EXPIRED','VIOLATED')
- create_time DATETIME

常见查询：
1. 查询某用户的所有预约：WHERE user_id = ?
2. 查询某房间某座位某日期的预约：WHERE room_id = ? AND seat_id = ? AND reserve_date = ?
3. 查询某日期范围内的预约：WHERE reserve_date BETWEEN ? AND ?
4. 查询某状态的预约：WHERE status = ?

表2：seat（座位表）
- seat_id BIGINT PK
- room_id BIGINT NOT NULL
- seat_no VARCHAR(20)
- seat_type ENUM('NORMAL','WINDOW','CORNER','DISABLED')
- has_power BOOLEAN
- status ENUM('AVAILABLE','RESERVED','IN_USE','MAINTAINING')

常见查询：
1. 查询某房间的所有座位：WHERE room_id = ?
2. 查询某房间某类型的可用座位：WHERE room_id = ? AND seat_type = ? AND status = 'AVAILABLE'

请给出索引设计建议，并解释为什么这样设计（最左前缀原理）。
```

#### 2.6.3 AI的输出摘要

Claude给出的索引设计建议：

**reservation表：**
1. `INDEX idx_user_id (user_id)` — 单用户查询
2. `INDEX idx_room_seat_date (room_id, seat_id, reserve_date)` — 复合索引，覆盖房间+座位+日期查询，符合最左前缀
3. `INDEX idx_reserve_date (reserve_date)` — 日期范围查询
4. `INDEX idx_status (status)` — 状态筛选

**seat表：**
1. `INDEX idx_room_id (room_id)` — 房间座位查询
2. `INDEX idx_seat_no (seat_no)` — 座位编号查询
3. `INDEX idx_status (status)` — 状态筛选

AI解释：
- `(room_id, seat_id, reserve_date)` 复合索引可以覆盖 `room_id` 前缀查询、`room_id + seat_id` 查询和完整的三列查询
- 单列索引用于独立查询条件，避免复合索引失效

#### 2.6.4 人工修改与调整

| 修改项 | 说明 |
|--------|------|
| 索引合并 | 将AI建议的 `idx_room_id` 和 `idx_status` 合并考虑，实际保留单列索引（seat表数据量不大，复合索引收益有限） |
| 覆盖索引 | 在reservation表中，考虑添加 `idx_user_status (user_id, status)` 覆盖"某用户某状态"查询，但当前数据量下暂不添加 |
| 索引顺序 | AI建议的 `(room_id, seat_id, reserve_date)` 顺序合理，但人工调整为 `(room_id, seat_id, reserve_date)` 而非 `(room_id, reserve_date, seat_id)`，因为seat_id区分度更高 |
| 外键索引 | MySQL InnoDB外键自动创建索引，但AI建议中显式声明了外键，人工确认外键索引已存在 |
| 执行计划验证 | 使用 `EXPLAIN` 验证索引命中情况，确认 `idx_room_seat_date` 被正确使用 |

#### 2.6.5 效果评价

- **节省时间**：索引设计建议约10分钟由AI生成，人工验证和调整约30分钟；相比从零学习索引优化理论节省约2小时
- **质量**：最左前缀原理解释清晰，索引设计符合实际查询模式
- **踩坑**：初期AI建议为seat表的 `(room_id, seat_type, status)` 添加复合索引，但实际seat表数据量很小（每个房间几十条），复合索引维护成本高而收益低，最终保留单列索引；另外发现MySQL 8.0中ENUM字段的索引效率略低于TINYINT，但当前设计已满足性能需求

---

### 记录七：前端Vue3 + Element Plus组件与接口联调优化

| 项目 | 内容 |
|------|------|
| **编号** | AI-007 |
| **日期** | 2026年6月1日 |
| **开发模块** | 前端 — AI推荐页面与智能客服页面 |
| **开发人员** | 何展霖 |

#### 2.7.1 遇到的问题/需求

需要开发两个前端页面：

1. **AI智能推荐页面（AIRecommend.vue）**：用户选择日期、教学楼、偏好（靠窗/有电源），获取AI推荐的座位，展示推荐理由和匹配度
2. **AI智能客服页面（AIChat.vue）**：聊天界面，支持用户输入问题、FAQ快捷标签、AI回复展示、加载动画

技术栈：Vue3 + Composition API + TypeScript + Element Plus + Axios

#### 2.7.2 给AI的Prompt

```
请帮我用Vue3 + TypeScript + Element Plus开发两个页面：

页面1：AI智能推荐（AIRecommend.vue）
- 顶部筛选栏：日期选择器、教学楼下拉框、靠窗/有电源复选框、获取推荐按钮
- 推荐结果卡片：展示推荐理由、匹配度百分比、推荐策略、推荐时间、去预约按钮
- 使用el-card、el-row/el-col布局
- 空状态时显示el-empty
- 调用API：POST /api/ai/recommendations，参数 {date, building, preferWindow, preferPower}
- 点击"去预约"跳转到 /rooms/:roomId?seatId=:seatId

页面2：AI智能客服（AIChat.vue）
- 聊天消息区域：用户消息（蓝色右对齐）、AI消息（白色左对齐）、头像区分
- 输入区域：el-textarea + 发送按钮，支持Enter发送
- 加载状态：AI思考时显示加载动画（三个跳动的小圆点 + 随机提示文字）
- FAQ快捷标签：el-tag展示常见问题，点击自动发送
- 相关文档展示：AI回复下方显示引用的知识库文档标题
- 调用API：POST /api/ai/chat，参数 {message, history}

请生成完整的Vue单文件组件代码，包含template、script setup、style scoped。
```

#### 2.7.3 AI的输出摘要

Claude生成的两个完整Vue组件：

**AIRecommend.vue**：
- 使用 `reactive` 管理筛选参数，`ref` 管理推荐列表和加载状态
- 日期默认值为当天（`new Date().toISOString().split('T')[0]`）
- 推荐卡片使用 `el-row :gutter="16"` 和 `el-col :xs="24" :sm="12" :md="8"` 实现响应式三列布局
- 匹配度展示为百分比 `(rec.score * 100).toFixed(0) %`
- 使用 `useRouter` 实现点击跳转

**AIChat.vue**：
- 消息列表使用 `v-for` 渲染，通过 `:class="msg.role"` 切换左右布局
- 加载动画使用CSS `@keyframes bounce` 实现三个小圆点跳动效果
- 随机提示文字从数组中随机选取，增加趣味性
- FAQ标签使用 `el-tag` 展示，点击触发 `sendMessage`
- 使用 `nextTick` + `scrollTop = scrollHeight` 实现自动滚动到底部

#### 2.7.4 人工修改与调整

| 修改项 | 说明 |
|--------|------|
| 类型导入 | AI使用内联类型定义，人工提取到 `src/api/ai.ts` 和 `src/types/index.ts`，统一管理接口类型 |
| 错误处理 | AI的catch块较简单，人工添加 `ElMessage.error()` 统一提示 |
| 消息历史 | AI的chat API未正确传递history格式，人工调整为过滤后只传递role和content字段 |
| 响应式边界 | AI的 `el-col` 响应式在移动端测试时发现问题，人工调整 `:xs="24" :sm="12" :md="8"` 确保小屏单列展示 |
| 样式微调 | AI生成的气泡样式基础上，人工添加 `box-shadow`、圆角、hover效果，提升视觉体验 |
| 加载动画 | AI的CSS动画基础上，人工添加 `loading-text` 样式和更多随机提示语 |
| 路由跳转 | AI使用 `router.push`，人工补充 `query: { seatId: String(seatId) }` 确保seatId正确传递 |

#### 2.7.5 效果评价

- **节省时间**：两个页面骨架约40分钟由AI生成，人工类型提取、样式调优和联调约3小时；相比从零手写节省约4小时
- **质量**：组件结构清晰，响应式布局合理，加载动画效果流畅，代码符合Vue3 Composition API规范
- **踩坑**：
  - AI生成的 `value-format="YYYY-MM-DD"` 在Element Plus 2.4+版本中使用正确，但旧版本需调整为 `"yyyy-MM-dd"`，已确认版本兼容
  - AI的 `renderMarkdown` 函数仅处理换行，实际场景中用户可能输入特殊字符，人工添加基础的HTML转义防止XSS
  - 联调时发现后端返回的 `createTime` 是字符串格式，前端直接展示，无需额外处理

---

## 3. 总结

### 3.1 AI辅助开发的收益

| 维度 | 具体收益 |
|------|----------|
| **效率提升** | 7条开发记录累计节省约20+小时开发时间，算法类任务效率提升最为显著（协同过滤、RAG检索从零手写需4-5小时，AI辅助后2小时内完成） |
| **代码质量** | AI生成的代码结构规范、命名合理、符合Java/Vue最佳实践，减少了初级错误（如空指针、资源泄漏） |
| **知识拓展** | 通过AI快速了解智谱API调用方式、Docker Compose条件依赖、MySQL索引最左前缀等知识点，降低学习成本 |
| **思路启发** | AI提供的多种实现方案（如RAG的2-gram分词、协同过滤的加权聚合）拓宽了设计思路 |
| **文档一致性** | AI生成的代码风格统一，便于团队维护 |

### 3.2 AI辅助开发的局限

| 局限 | 说明 |
|------|------|
| **边界处理不足** | AI生成的代码往往缺少空值判断、异常边界处理，需要人工补充（如AI-001的targetVector空判断、AI-003的API Key空值处理） |
| **业务上下文缺失** | AI不了解项目的具体业务规则（如分数范围0.60-0.99、策略标签循环分配），需要人工调整 |
| **安全考虑不足** | AI未主动考虑XSS防护、SQL注入等安全问题，需要人工审查 |
| **性能盲点** | AI建议的索引方案需要结合实际数据量和查询模式验证，不能直接使用 |
| **版本兼容性** | AI训练数据有截止日期，对最新框架版本（如Element Plus 2.4+的value-format变化）可能不了解 |

### 3.3 最佳实践

1. **Prompt工程**：提供足够的上下文（技术栈、数据模型、约束条件），使用结构化Prompt（分点列出需求），AI输出质量显著提升
2. **分层验证**：AI生成代码后，按"语法正确性 -> 边界条件 -> 业务逻辑 -> 性能表现"四层验证
3. **人工兜底**：AI适合生成80%的标准代码，剩余20%的边界处理、安全加固、业务定制必须人工完成
4. **版本管理**：AI辅助生成的代码同样需要走Code Review，纳入Git版本控制
5. **工具组合**：复杂算法用Claude（上下文长、推理强），API调用用官方文档 + AI辅助，前端组件用AI生成骨架后人工精调样式
6. **持续迭代**：将AI输出中的问题反馈到Prompt中，形成"生成 -> 测试 -> 修正Prompt -> 再生成"的迭代循环

---

## 附录：相关代码文件清单

| 文件路径 | 说明 |
|----------|------|
| `backend/campus-ai/src/main/java/com/campus/ai/service/CollaborativeFilterService.java` | 协同过滤推荐服务 |
| `backend/campus-ai/src/main/java/com/campus/ai/service/RagService.java` | RAG知识库检索服务 |
| `backend/campus-ai/src/main/java/com/campus/ai/service/ZhipuAiService.java` | 智谱大模型调用与本地兜底 |
| `backend/campus-ai/src/main/java/com/campus/ai/controller/AIController.java` | AI服务REST控制器 |
| `frontend/src/views/ai/AIRecommend.vue` | AI智能推荐前端页面 |
| `frontend/src/views/ai/AIChat.vue` | AI智能客服前端页面 |
| `frontend/src/api/ai.ts` | 前端AI接口封装 |
| `docker-compose.yml` | Docker Compose编排文件 |
| `test/sql/mysql/campus_studyroom.sql` | 数据库初始化脚本（含索引设计） |

---

*本文档由校园自习室开发小组编制，记录AI辅助开发的真实过程，供课程交付与团队复盘使用。*
