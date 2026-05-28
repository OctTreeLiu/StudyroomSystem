# 逐光自习室管理系统

基于 **Spring Boot + Vue 3** 的前后端分离自习室管理平台，支持在线预约、座位管理、支付宝沙箱支付、会员与积分、学习统计、留言互动、通知推送及 AI 助手（通义千问）等功能。

## 技术栈

| 后端 | 前端 |
|------|------|
| Spring Boot 2.7.14 | Vue 3.3.4 |
| MyBatis 2.3.1 | Element Plus 2.4.2 |
| MySQL 8.0 | Vite 4.5.0 |
| JWT / Maven | Pinia / Vue Router / Axios |

## 环境要求

- **JDK** 11+
- **Maven** 3.6+
- **MySQL** 8.0+
- **Node.js** 16+（推荐 18 LTS）

## 快速启动

### 1. 克隆项目

```bash
git clone <你的仓库地址>
cd code
```

### 2. 导入数据库表结构

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS studyroom_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入表结构（仅建表，不含业务数据）
mysql -u root -p studyroom_db < db_sql/studyroom_db_onlytable.sql
```

也可使用 Navicat / MySQL Workbench 执行 `db_sql/studyroom_db_onlytable.sql`。

### 3. 插入管理员账号

系统**无管理员注册功能**，须手动插入管理员：

| 项 | 值 |
|----|-----|
| 用户名 | `admin` |
| 密码 | `123456` |
| 密码（MD5） | `e10adc3949ba59abbe56e057f20f883e` |
| 角色 | `role = 1`（管理员） |

```sql
USE studyroom_db;

INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`)
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 1, 1);
```

### 4. 插入默认业务配置

执行五区自习室（138 个座位）及价格配置的初始化 SQL，详见 **[docs/部署说明.md — 3.1.4 节](docs/部署说明.md#314-插入默认业务配置必做)**。

简要说明：依次插入 `study_room` → `seat` → `price_config`，默认预约价 3 元/小时、租赁 30 元/天。

### 5. 配置并启动后端

编辑 `backend/src/main/resources/application.yml`，至少修改：

- `spring.datasource` — MySQL 连接（url / username / password）
- `alipay.*` — 支付宝沙箱（支付功能需要，见 [支付宝配置说明](docs/支付宝沙箱支付配置说明.md)）
- `ai.*` — 通义千问 API Key（AI 助手需要）

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端地址：`http://localhost:8080/api`

### 6. 安装依赖并启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：`http://localhost:3000`

使用 **admin / 123456** 登录管理端。

---

## 默认访问地址

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:3000 |
| 后端 API | http://localhost:8080/api |
| 管理员账号 | admin / 123456 |

如果选择导入包含数据的sql文件，管理员密码为admin123

## 项目结构

```
code/
├── backend/          # Spring Boot 后端
├── frontend/         # Vue 3 前端
├── db_sql/           # 数据库 SQL（推荐 studyroom_db_onlytable.sql）
└── docs/             # 项目文档
```

## 文档

| 文档 | 说明 |
|------|------|
| [docs/部署说明.md](docs/部署说明.md) | 完整部署指南（数据库、后端、前端、GitHub） |
| [docs/项目总结.md](docs/项目总结.md) | 功能模块与技术说明 |
| [docs/项目结构说明.md](docs/项目结构说明.md) | 目录与代码结构 |
| [docs/支付宝沙箱支付配置说明.md](docs/支付宝沙箱支付配置说明.md) | 支付沙箱配置 |

## License

本项目为毕业设计作品，仅供学习交流使用。
