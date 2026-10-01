# 🐶 简历狗 Resume Dog

> AI 驱动的简历生成工具。输入你的经历碎片，自动生成排版专业、措辞精准的简历，支持多模板导出 PDF。

用户写简历最大的痛点不是「写不出来」，而是**知道自己做过什么，但写不成招聘方想看的样子**。
简历狗做的就是中间那层改写：把口语化、零散、缺数字的经历碎片，变成一份结构完整、每条都有量化结果的中文简历。

---

## 目录

- [技术栈](#技术栈)
- [架构](#架构)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [API](#api)
- [核心设计决策](#核心设计决策)
- [成本控制](#成本控制)
- [当前状态与下一步](#当前状态与下一步)

---

## 技术栈

### 前端

| 选型 | 版本 | 为什么是它 |
|---|---|---|
| **React** | 18.3 | 生态最大，AI 相关的现成组件（流式渲染、Markdown 编辑）后续好接 |
| **TypeScript** | 5.7 | 简历结构是和后端共享的契约，`ResumeContent` 两端同名同构，类型能挡住大部分低级错误 |
| **Vite** | 5.4 | 冷启动秒级，改配置不用重启服务 |
| **Ant Design** | 5.22 | 简历编辑器的本质是**动态表单**（经历条数不定、每条的字段还不一样），antd 的 `Form.List` 正好干这个，不用自己造轮子 |
| **TanStack Query** | 5.62 | 服务端状态管理：缓存、失效、加载态一套齐活 |
| **Zustand** | 5.0 | 只存「当前用户 + JWT」这一个状态，不值得上 Redux |
| **Axios** | 1.7 | 拦截器统一处理 token 注入和 401 跳转 |

**关于 React 18 而不是 19**：antd v5 对 React 19 需要额外的兼容包，18 是当前最省心的组合。等 antd v6 稳定后再升。

### 后端

| 选型 | 版本 | 为什么是它 |
|---|---|---|
| **Java** | 17 | LTS |
| **Spring Boot** | 3.5.16 | 当前稳定线。Spring Boot 4 已发布但生态跟进中，先不冒险 |
| **Spring Security + JJWT** | 6.x / 0.12.6 | 无状态 JWT。简历内容是个人敏感信息，鉴权不能省 |
| **Spring Data JPA + Flyway** | 3.5 / 11 | schema 变更只走 Flyway 迁移文件，`ddl-auto=validate` |
| **MySQL** | 8.4 | `utf8mb4_0900_ai_ci`，中文字段和 emoji 都不会截断 |
| **Anthropic Java SDK** | 2.68.0 | 官方 SDK，自带结构化输出和 prompt caching |
| **Thymeleaf + OpenHTMLtoPDF** | 3.1 / 1.0.10 | 服务端渲染 PDF，见下方「为什么 PDF 在后端出」 |
| **springdoc-openapi** | 2.8.13 | 自动生成 `/swagger-ui.html` |

---

## 架构

```
┌─────────────────────────────────────────────────────────┐
│  React 18 + Ant Design (Vite)                            │
│  ┌───────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ 经历碎片  │→ │ AI 生成中    │→ │ 结构化预览       │  │
│  │ 输入 +    │  │ 20-60s       │  │ + 选模板         │  │
│  │ 目标岗位  │  │              │  │ + 导出 PDF       │  │
│  └───────────┘  └──────────────┘  └──────────────────┘  │
└────────────────────────┬────────────────────────────────┘
                         │ REST (JWT) / SSE 预留
┌────────────────────────▼────────────────────────────────┐
│  Spring Boot                                            │
│                                                         │
│  Controller ──► Service ──┬──► ClaudeResumeService       │
│                           │      │ 结构化输出              │
│                           │      │ prompt caching        │
│                           │      ▼                      │
│                           │    Claude API               │
│                           │                             │
│                           └──► PdfService                │
│                                  │ Thymeleaf             │
│                                  ▼                       │
│                              OpenHTMLtoPDF → PDF         │
│                                                         │
│  JPA ──► MySQL（简历内容存 JSON 列）                     │
└─────────────────────────────────────────────────────────┘
```

核心数据流只有一条：**经历碎片 → Claude 结构化输出 → `ResumeContent` → 存 JSON → 渲染成 PDF**。
这条链路上的每个环节下面都单独解释。

---

## 快速开始

### 前置

- JDK 17+
- Maven 3.9+
- Node.js 20+
- Docker（只用来起 MySQL）

### 1. 配置

```bash
cp .env.example .env
# 编辑 .env，至少填 ANTHROPIC_API_KEY 和 JWT_SECRET
# JWT_SECRET 生成：node -e "console.log(require('crypto').randomBytes(48).toString('base64'))"
```

> **没有 API Key 也能先跑通全流程**：把 `.env` 里的 `CLAUDE_MOCK=true` 打开，
> 后端会跳过 Claude 调用直接返回示例数据，前端链路完全一致。

### 2. 起数据库

```bash
docker compose up -d
```

### 3. 起后端

```bash
cd backend
mvn spring-boot:run
```

- 服务：http://localhost:8080
- 接口文档：http://localhost:8080/swagger-ui.html
- 健康检查：http://localhost:8080/actuator/health

首次启动时 Flyway 会自动建表。

### 4. 起前端

```bash
cd frontend
npm install
npm run dev
```

打开 http://localhost:5173 ，注册一个账号就能用。

> Vite 已经把 `/api` 代理到 `localhost:8080`，开发时前后端同源，不会遇到跨域。

---

## 项目结构

```
resume-dog/
├── README.md
├── .env.example                    # 环境变量模板（.env 不进仓库）
├── docker-compose.yml              # 本地 MySQL
│
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/resumedog/
│       │   │   ├── ResumeDogApplication.java
│       │   │   ├── common/         # 统一响应、全局异常
│       │   │   ├── config/         # Claude 客户端、Security、PDF 引擎、OpenAPI
│       │   │   ├── domain/         # JPA 实体 + ResumeContent（领域模型）
│       │   │   ├── repository/     # Spring Data JPA
│       │   │   ├── service/        # 业务逻辑 + Claude 调用 + PDF 渲染
│       │   │   ├── security/       # JWT 签发与校验
│       │   │   └── controller/     # REST 接口
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── prompts/
│       │       │   └── resume-writer-system.md   # ← 简历写作规范，改效果先改这里
│       │       ├── db/migration/V1__init.sql
│       │       └── resume-templates/              # ← PDF 模板，加模板改这里
│       │           ├── resume-fragments.html       #   公共样式和片段
│       │           ├── resume-classic.html
│       │           └── resume-modern.html
│       └── test/
│           └── PdfRenderSmokeTest.java
│
└── frontend/
    ├── package.json / vite.config.ts / tsconfig.json
    └── src/
        ├── main.tsx / App.tsx
        ├── api/          # client.ts（axios 拦截器）+ index.ts（接口定义）
        ├── components/   # ResumePreview
        ├── pages/        # LoginPage / HomePage / EditorPage
        ├── store/        # auth（zustand）
        ├── styles/
        └── types/        # 与后端 ResumeContent 同构
```

---

## API

统一响应包装，`code === 0` 为成功：

```jsonc
{ "code": 0, "message": "ok", "data": { }, "timestamp": "2026-10-01T12:00:00Z" }
```

除 `/api/auth/**` 外都需要 `Authorization: Bearer <token>`。

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/api/auth/register` | 注册，返回 JWT |
| `POST` | `/api/auth/login` | 登录，返回 JWT |
| `GET` | `/api/resumes` | 简历列表 |
| `GET` | `/api/resumes/{id}` | 简历详情 |
| `POST` | `/api/resumes` | 新建草稿 |
| `PUT` | `/api/resumes/{id}` | 保存（用户手工改过内容后回写） |
| `POST` | `/api/resumes/{id}/generate` | **AI 生成**：碎片 → 结构化简历 |
| `GET` | `/api/resumes/{id}/export` | 导出 PDF |
| `DELETE` | `/api/resumes/{id}` | 删除 |
| `GET` | `/api/templates` | 模板列表 |

### 生成接口示例

```bash
curl -X POST http://localhost:8080/api/resumes/1/generate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "targetPosition": "Java 后端工程师（高并发方向）",
    "fragments": "2021 校招进的某某科技，做订单系统，流量大的时候数据库老挂，后来做了分库分表加缓存"
  }'
```

耗时通常 20–60 秒。前端 `client.ts` 里的 timeout 给到了 120s。

---

## 核心设计决策

这部分是踩过坑之后才有的结论，改代码前建议先读一遍。

### 1. 用结构化输出，不要让模型「输出 JSON 字符串」

`ClaudeResumeService` 用的是 SDK 的结构化输出：把 `ResumeContent.class` 交给 SDK，
它自动推导 JSON Schema，返回值直接就是强类型的 `ResumeContent`：

```java
StructuredOutputConfig<ResumeContent> outputConfig = StructuredOutputConfig
        .<ResumeContent>builder()
        .format(ResumeContent.class)
        .effort(...)          // effort 和 format 必须在同一个对象上设置
        .build();
```

对比让模型「请输出 JSON」然后自己解析：后者会冒出半截 JSON、```json 围栏、多余的「好的，以下是您的简历」，
每一种都要写容错分支。前者在 API 层就把形状保证了。

**踩过的坑**：`effort` 和 `format` 分两次调用 `outputConfig(...)` 会互相覆盖，
后调的抹掉先调的，schema 直接失效。两个参数必须打包进同一个 `StructuredOutputConfig`。

### 2. 字段注释就是给模型的指令

`ResumeContent` 上每个字段的 `@JsonPropertyDescription` 会**直接进入 JSON Schema**。
比如「每条以动词开头，包含具体动作、技术手段与可量化结果」这类约束写在这里，
比在 prompt 里反复强调有效得多——模型是在照着 schema 生成，不是在读你的散文。

同理，`prompts/resume-writer-system.md` 里的第 1 条「不要编造数字」很重要：
用户给的碎片里通常没有精确数字，必须明确告诉模型宁可写保守描述也不要编一个看起来很精确的假数字。

### 3. 简历内容用 JSON 列，不拆子表

`resumes.content` 是一列 `json`，存整个 `ResumeContent`。理由：

- 简历是「读多写多、几乎不按字段查询」的文档型数据，拆成 education/work/project 几张表只会带来无谓的 JOIN 和级联维护；
- AI 生成的字段会随模型能力演进而增删，JSON 列不需要迁移；
- 整份简历天然是一个编辑单元，读改写一次即可。

**代价**：失去了「按技能检索所有简历」这类查询。要做人才库检索时，
加一张倒排表抽取 `skills` 字段即可，主表不用动。

### 4. PDF 在后端渲染

简历是最终要投递出去的正式文件，需要跨系统、跨浏览器输出一致，还要可靠嵌入中文字体。
浏览器端导出（html2pdf / jsPDF）在这三点上都会翻车，尤其中文和分页。

用 **Thymeleaf + OpenHTMLtoPDF**：

- 模板在 `backend/src/main/resources/resume-templates/`，加模板 = 放一个 HTML + 在 `TemplateService` 加一行；
- 公共样式和各个 section 抽在 `resume-fragments.html` 里，两套模板共用，改一处两边生效。

**⚠️ 硬约束：OpenHTMLtoPDF 只实现 CSS 2.1 子集，不支持 flex 和 grid。**
模板里所有「左右分栏」都用 `display: table` + `table-cell` 实现。
在模板里写 `display:flex` 不会报错，只会静默排版错乱。

### 5. 前端预览只是近似效果

`ResumePreview.tsx` 让用户生成完能立刻看到内容，但它**不是最终排版**。
真正的 PDF 由后端渲染，两边共用同一份 `ResumeContent` 数据。
想快速确认最终效果，跑一次冒烟测试：

```bash
cd backend && mvn test -Dtest=PdfRenderSmokeTest
# 会在 target/sample-resume/ 下生成 classic.pdf 和 modern.pdf
```

这个测试不碰数据库、不需要 API Key。它守的是「编译能过但运行炸掉」的那类问题——
比如 Thymeleaf 3 把 OGNL 声明成了 optional 依赖，缺了编译完全不报错，
只有真正渲染模板时才会 `NoClassDefFoundError`。

### 6. 鉴权用 Spring Security 而不是裸 filter

简历内容是个人敏感信息（姓名、电话、邮箱、求职意向），不能裸奔。
无状态 JWT + `OncePerRequestFilter` + 关闭 CSRF（token 鉴权天然免疫 CSRF）。
`CurrentUser.id()` 从 `SecurityContext` 里取 userId，Controller 里不出现任何和鉴权相关的代码。

---

## 成本控制

这个产品是典型的「高频调用大模型」，成本结构决定了它能不能活下去。按性价比排序：

**1. Prompt caching（已实现）**
简历写作规范是一段固定的长文本，放在 system 最前面并打上 `cache_control` 断点，
命中缓存后按约 1/10 价格计费。日志里能看到 `cacheReadInputTokens`——**如果一直是 0，说明缓存没生效**。

**2. 选模型（配置项，不用改代码）**
默认 `claude-opus-5-5`（`$4 / $20` per MTok），质量最好。
这是 C 端产品要压成本时最直接的一刀——换成 `claude-sonnet-5-5`（`$2 / $10`），
改 `.env` 里的 `CLAUDE_MODEL` 即可。**建议先用 Opus 跑通效果、攒够真实样本，再评估换 Sonnet 的质量损失。**

**3. effort 档位（已暴露为配置）**
`claude.effort` 默认为 `HIGH`。Claude Opus 5.5 的模型默认是 `MEDIUM`，
所以这里必须显式设置。简历措辞质量对 effort 敏感，但可以按接口分别调：
生成接口用 `HIGH`，未来加的「润色单条经历」接口可以降到 `LOW`。

**4. 生成结果复用（未实现）**
同一个人重复点「AI 生成」会反复调用。可以在生成前对 `(fragments, targetPosition)`
做哈希，命中则直接返回上次结果。**建议先加上这个，再考虑其他优化。**

**5. 免费额度机制（未实现）**
注册送 N 次生成，用完引导付费。这是 C 端产品的标准形态，
但需要先有用户才能设计，先不做。

---

## 当前状态与下一步

### 已完成

- [x] 前后端工程骨架，Maven / Vite 配置，Docker Compose 一键起 MySQL
- [x] JWT 注册登录，Spring Security 无状态鉴权
- [x] 简历 CRUD，MySQL JSON 列 + Flyway 迁移
- [x] Claude 结构化输出：经历碎片 → `ResumeContent`
- [x] Prompt caching（系统提示词缓存断点）
- [x] PDF 服务端渲染，两套模板 + 公共片段
- [x] PDF 冒烟测试（不依赖 DB 和 API Key）
- [x] 前端登录 / 列表 / 编辑页，编辑页左右分栏（输入 + 预览）
- [x] `claude.mock` 模式，无 API Key 也能跑通全流程

### 下一步（按优先级）

1. **跑通 PDF 冒烟测试** —— `pom.xml` 里新增了 `ognl` 和 `javassist` 两个 Thymeleaf 运行期必需依赖，
   首次拉取需要网络，跑一次确认两个模板都能正常出 PDF。
2. **接真实 API 验证生成质量** —— 结构化输出在真实调用下的 schema 推导和措辞效果需要实测。
   建议准备 5-10 份真实经历碎片，覆盖应届 / 社招 / 转行三类，逐条评估改写质量。
3. **流式输出（SSE）** —— 生成要 20-60 秒，目前是干等。加 SSE 后可以让用户看到「正在分析经历…」这类进度。
4. **结构化内容的前端编辑** —— 现在生成后只能看不能改。这是最大的功能缺口。
   用 antd `Form.List` 实现「工作经历」的动态增删改，其余 section 复用同一套模式。
5. **生成结果复用** —— 见「成本控制」第 4 条。
6. **针对 JD 优化** —— 传入目标 JD，输出「匹配度分析 + 差距项 + 改写建议」。这是差异化功能。

---

## License

MIT
#   r e s u m e - d o g  
 