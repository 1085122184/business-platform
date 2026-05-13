package com.cjx.common.core.utils;
/**
 * 雪花算法ID生成器
 * <p>
 * 与Hutool区别：
 * - Hutool的IdUtil.getSnowflake()功能类似
 * - 但本类提供了更详细的配置和文档说明
 * - 适合需要深度定制的场景
 * <p>
 * ID结构(64bit):
 * 0 - 41位时间戳 - 10位机器ID - 12位序列号
 *
 * @author system
 */
public class SnowflakeIdWorker {
    /** 开始时间戳 (2024-01-01 00:00:00) */
    private static final long START_TIMESTAMP = 1704038400000L;

    /** 机器ID所占位数 */
    private static final long WORKER_ID_BITS = 10L;

    /** 序列号所占位数 */
    private static final long SEQUENCE_BITS = 12L;

    /** 机器ID最大值 */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /** 序列号最大值 */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /** 机器ID左移位数 */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /** 时间戳左移位数 */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /** 机器ID */
    private final long workerId;

    /** 序列号 */
    private long sequence = 0L;

    /** 上次生成ID的时间戳 */
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     * @param workerId 机器ID (0-1023)
     */
    public SnowflakeIdWorker(long workerId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("Worker ID 必须在 0 到 %d 之间", MAX_WORKER_ID));
        }
        this.workerId = workerId;
    }

    /**
     * 生成下一个ID
     */
    public synchronized long nextId() {
        long timestamp = currentTimeMillis();

        // 时钟回拨检测
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("时钟回拨，拒绝生成ID，时间差: %d ms",
                            lastTimestamp - timestamp));
        }

        // 同一毫秒内
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 序列号溢出
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 组装ID
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 等待下一毫秒
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳
     */
    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    // ========== 静态工厂方法 ==========

    private static volatile SnowflakeIdWorker instance;

    /**
     * 获取单例实例（默认workerId=1）
     */
    public static SnowflakeIdWorker getInstance() {
        if (instance == null) {
            synchronized (SnowflakeIdWorker.class) {
                if (instance == null) {
                    // 实际项目中应该从配置文件或环境变量读取workerId
                    instance = new SnowflakeIdWorker(1);
                }
            }
        }
        return instance;
    }

    /**
     * 生成ID（静态方法）
     */
    public static long generateId() {
        return getInstance().nextId();
    }
}
