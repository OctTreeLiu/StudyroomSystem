-- 为 reservation 表添加退款相关字段
ALTER TABLE reservation
  ADD COLUMN refund_no VARCHAR(64) NULL COMMENT '退款单号' AFTER trade_no,
  ADD COLUMN refund_amount DECIMAL(10,2) NULL COMMENT '退款金额' AFTER refund_no,
  ADD COLUMN refund_time DATETIME NULL COMMENT '退款时间' AFTER refund_amount,
  ADD COLUMN refund_status TINYINT(1) DEFAULT 0 COMMENT '退款状态：0-未退款，1-退款成功，2-退款失败' AFTER refund_time,
  ADD COLUMN refund_reason VARCHAR(255) NULL COMMENT '退款原因' AFTER refund_status;

