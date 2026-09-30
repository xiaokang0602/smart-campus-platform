-- =============================================================
-- 智慧校园综合管理系统 初始化建表脚本（MySQL 8.0）
-- 说明：仅建表结构；演示种子数据由后端 DataInitializer 在首次启动时写入
-- =============================================================

CREATE DATABASE IF NOT EXISTS smart_campus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE smart_campus;

-- 学校表
CREATE TABLE IF NOT EXISTS school (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  school_name VARCHAR(100) NOT NULL COMMENT '学校名称',
  address VARCHAR(255) COMMENT '地址',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学校表';

-- 用户表
CREATE TABLE IF NOT EXISTS user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL COMMENT '登录账号',
  password VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密密码',
  real_name VARCHAR(50) COMMENT '真实姓名',
  role TINYINT NOT NULL COMMENT '1学生 2任课教师 3班主任 4后台账号',
  account_level TINYINT DEFAULT 0 COMMENT '后台层级:1超级管理员 2学校管理员 3普通后台;其余为0',
  school_id BIGINT DEFAULT 0 COMMENT '所属学校ID;超管为0',
  phone VARCHAR(20) COMMENT '手机号',
  status TINYINT DEFAULT 1 COMMENT '1启用 0禁用',
  first_login TINYINT DEFAULT 0 COMMENT '0已完成 1待完善密保与设置新密码',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 密保问题表
CREATE TABLE IF NOT EXISTS security_question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  question VARCHAR(200) NOT NULL,
  answer_hash VARCHAR(100) NOT NULL,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='密保问题表';

-- 好友担保人表
CREATE TABLE IF NOT EXISTS friend_guardian (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '账号所属学生ID',
  guardian_user_id BIGINT NOT NULL COMMENT '被指定的认证同学ID',
  is_primary TINYINT DEFAULT 1 COMMENT '1第一担保人 0第二担保人',
  bind_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友担保人表';

-- 后台账号申请表
CREATE TABLE IF NOT EXISTS admin_account_apply (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  apply_school_id BIGINT NOT NULL,
  applicant_id BIGINT NOT NULL,
  new_username VARCHAR(50) NOT NULL,
  new_real_name VARCHAR(50),
  menu_perms TEXT COMMENT '申请的菜单权限 json',
  apply_reason TEXT,
  audit_status TINYINT DEFAULT 0 COMMENT '0待审批 1通过 2驳回',
  audit_user BIGINT,
  audit_comment TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台账号申请表';

-- 班级表
CREATE TABLE IF NOT EXISTS class (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_name VARCHAR(50) NOT NULL,
  grade VARCHAR(20) COMMENT '年级',
  head_teacher_id BIGINT COMMENT '班主任ID',
  school_id BIGINT NOT NULL,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 学生信息表
CREATE TABLE IF NOT EXISTS student (
  id BIGINT PRIMARY KEY COMMENT '学生ID,关联user.id',
  class_id BIGINT,
  student_no VARCHAR(30) COMMENT '学号',
  duty VARCHAR(50) DEFAULT '无' COMMENT '任职:班长/课代表/无',
  gender VARCHAR(10) COMMENT '性别',
  birthday DATE,
  archive_note TEXT COMMENT '档案备注',
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表';

-- 评奖评优表
CREATE TABLE IF NOT EXISTS student_award (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  award_name VARCHAR(100) NOT NULL,
  award_time DATE,
  award_level VARCHAR(50),
  create_by BIGINT COMMENT '操作人(班主任)',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评奖评优表';

-- 教师任职表
CREATE TABLE IF NOT EXISTS teacher_job (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  class_id BIGINT NOT NULL,
  subject VARCHAR(30) NOT NULL,
  is_head_teacher TINYINT DEFAULT 0,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师任职表';

-- 课代表表
CREATE TABLE IF NOT EXISTS class_rep (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  subject VARCHAR(30),
  class_id BIGINT,
  audit_status TINYINT DEFAULT 0 COMMENT '0待审批 1通过 2驳回',
  audit_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课代表表';

-- 课表表
CREATE TABLE IF NOT EXISTS timetable (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_id BIGINT NOT NULL,
  week TINYINT NOT NULL COMMENT '星期1~7',
  period TINYINT NOT NULL COMMENT '节次',
  subject VARCHAR(30),
  teacher_id BIGINT,
  room VARCHAR(50),
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课表表';

-- 题库表
CREATE TABLE IF NOT EXISTS question_bank (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  school_id BIGINT NOT NULL COMMENT '校际隔离关键字段',
  subject VARCHAR(30) NOT NULL,
  question_type TINYINT NOT NULL COMMENT '1单选 2多选 3判断 4填空 5简答',
  grade VARCHAR(20) COMMENT '初一/初二/初三',
  title TEXT NOT NULL,
  options TEXT COMMENT '选项 json',
  answer TEXT COMMENT '参考答案',
  analysis TEXT COMMENT '解析',
  difficulty TINYINT DEFAULT 3 COMMENT '难度1-5',
  knowledge_point VARCHAR(100),
  create_teacher BIGINT,
  source TINYINT DEFAULT 1 COMMENT '1手动 2AI生成',
  ai_batch_no VARCHAR(50),
  review_status TINYINT DEFAULT 1 COMMENT '0待挑选 1已入库 2已丢弃',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库表';

-- 试卷表
CREATE TABLE IF NOT EXISTS exam_paper (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  school_id BIGINT NOT NULL,
  paper_name VARCHAR(100) NOT NULL,
  subject VARCHAR(30) NOT NULL,
  grade VARCHAR(20),
  total_score INT DEFAULT 0,
  exam_time INT DEFAULT 60 COMMENT '时长(分钟)',
  create_teacher BIGINT,
  source TINYINT DEFAULT 1,
  status TINYINT DEFAULT 0 COMMENT '0未发布 1已发布 2已结束',
  class_ids TEXT COMMENT '允许参加班级 json',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- 试卷-试题关联表
CREATE TABLE IF NOT EXISTS paper_question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  paper_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  score INT DEFAULT 0,
  sort INT DEFAULT 0,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷-试题关联表';

-- 考试记录表
CREATE TABLE IF NOT EXISTS exam_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  paper_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  start_time DATETIME,
  submit_time DATETIME,
  total_score DECIMAL(10,1) DEFAULT 0,
  status TINYINT DEFAULT 0 COMMENT '0未开始 1进行中 2已交卷 3切屏超限强制交卷',
  cheat_switch_count INT DEFAULT 0,
  max_allowed_switch INT DEFAULT 3,
  reopen_count INT DEFAULT 0,
  last_reopen_by BIGINT,
  last_reopen_time DATETIME,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';

-- 学生答题详情表
CREATE TABLE IF NOT EXISTS exam_answer (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  exam_record_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  student_answer TEXT,
  is_correct TINYINT DEFAULT 0,
  score DECIMAL(10,1) DEFAULT 0,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生答题详情表';

-- AI 学情分析表
CREATE TABLE IF NOT EXISTS ai_score_analysis (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  exam_record_id BIGINT COMMENT '单次考试记录ID;画像分析可空',
  student_id BIGINT NOT NULL,
  analysis_scope TINYINT DEFAULT 1 COMMENT '1单次考后分析 2画像实时分析',
  subject VARCHAR(30),
  analyst_id BIGINT,
  analysis_content TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI学情分析表';

-- 任课教师更换申请表
CREATE TABLE IF NOT EXISTS teacher_change_apply (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  apply_class_id BIGINT NOT NULL,
  subject VARCHAR(30),
  old_teacher_id BIGINT,
  new_teacher_id BIGINT,
  apply_user BIGINT,
  apply_reason TEXT,
  audit_status TINYINT DEFAULT 0 COMMENT '0待审核 1通过 2驳回',
  audit_comment TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任课教师更换申请表';

-- 消息主表
CREATE TABLE IF NOT EXISTS message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  school_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT,
  notice_type TINYINT DEFAULT 1 COMMENT '1系统公告 2考试通知 3审批结果 4班级通知 5个人提醒',
  publisher_id BIGINT,
  publisher_role TINYINT,
  target_scope TINYINT DEFAULT 1 COMMENT '1全校 2指定班级 3指定个人',
  target_class_ids TEXT,
  target_user_ids TEXT,
  is_top TINYINT DEFAULT 0,
  attachment VARCHAR(255),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息主表';

-- 消息接收表
CREATE TABLE IF NOT EXISTS message_receiver (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  message_id BIGINT NOT NULL,
  receiver_id BIGINT NOT NULL,
  is_read TINYINT DEFAULT 0,
  read_time DATETIME,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息接收表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  school_id BIGINT DEFAULT 0,
  operator_id BIGINT,
  operator_role TINYINT,
  module VARCHAR(50),
  action VARCHAR(50),
  target_type VARCHAR(50),
  target_id BIGINT,
  change_diff TEXT,
  ip VARCHAR(50),
  user_agent VARCHAR(500),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
