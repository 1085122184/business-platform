package com.cjx.uibot.service.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 任务执行管理器
 * 负责管理当前执行任务的状态，确保同一时间只有一个任务在执行
 * 使用读写锁提高并发性能
 *
 * @author Task Queue System
 * @version 1.0
 * @since 2025-11-12
 */
@Slf4j
@Component
public class TaskExecutionManager {
    /**
     * 当前执行任务ID的原子引用
     */
    private final AtomicReference<String> currentTaskId = new AtomicReference<>(null);

    /**
     * 读写锁，提高并发查询性能
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    /**
     * 检查是否有任务正在执行
     * 使用读锁，支持高并发查询
     *
     * @return true-有任务执行，false-无任务执行
     */
    public boolean isTaskExecuting() {
        readLock.lock();
        try {
            return currentTaskId.get() != null;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 获取当前执行任务ID
     *
     * @return 当前执行任务ID，如果没有则返回null
     */
    public String getCurrentTaskId() {
        readLock.lock();
        try {
            return currentTaskId.get();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 尝试开始执行任务
     * 使用CAS操作确保原子性
     *
     * @param taskId 任务ID
     * @return true-成功开始，false-已有任务在执行
     */
    public boolean tryStartExecution(String taskId) {
        writeLock.lock();
        try {
            if (currentTaskId.compareAndSet(null, taskId)) {
                log.info("任务开始执行: taskId={}", taskId);
                return true;
            }
            log.warn("尝试开始执行任务失败，已有任务正在执行: currentTaskId={}, newTaskId={}",
                    currentTaskId.get(), taskId);
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 完成任务执行
     *
     * @param taskId 任务ID
     * @return true-成功完成，false-任务ID不匹配
     */
    public boolean finishExecution(String taskId) {
        writeLock.lock();
        try {
            String current = currentTaskId.get();
            if (taskId.equals(current)) {
                currentTaskId.set(null);
                log.info("任务执行完成: taskId={}", taskId);
                return true;
            }
            log.warn("完成任务失败，任务ID不匹配: currentTaskId={}, finishTaskId={}",
                    current, taskId);
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 强制清除当前执行任务
     * 用于异常情况处理，如任务超时
     *
     * @param reason 清除原因
     */
    public void forceClear(String reason) {
        writeLock.lock();
        try {
            String taskId = currentTaskId.getAndSet(null);
            if (taskId != null) {
                log.warn("强制清除执行任务: taskId={}, reason={}", taskId, reason);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 重置执行状态（用于系统初始化）
     */
    public void reset() {
        writeLock.lock();
        try {
            currentTaskId.set(null);
            log.info("任务执行管理器已重置");
        } finally {
            writeLock.unlock();
        }
    }
}
