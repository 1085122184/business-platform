package com.cjx.uibot.service.manager;

import com.cjx.uibot.api.dto.task.QueueTask;
import com.cjx.uibot.api.enums.QueueType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;


/**
 * 队列管理器
 * 统一管理优先级队列和普通队列
 * 提供队列操作的统一接口
 *
 * @author Task Queue System
 * @version 1.0
 * @since 2025-11-12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueueManager {
    @Qualifier("priorityTaskQueue")
    private final PriorityBlockingQueue<QueueTask> priorityQueue;

    @Qualifier("normalTaskQueue")
    private final LinkedBlockingQueue<QueueTask> normalQueue;

    /**
     * 添加任务到队列
     * 根据任务的队列类型自动选择对应的队列
     *
     * @param task 队列任务
     * @return 是否添加成功
     */
    public boolean offer(QueueTask task) {
        if (task == null || task.getQueueType() == null) {
            log.error("任务或队列类型为空，无法添加到队列");
            return false;
        }

        boolean success;
        if (task.getQueueType() == QueueType.PRIORITY) {
            success = priorityQueue.offer(task);
            log.debug("任务已添加到优先级队列: taskId={}, priority={}, queueSize={}",
                    task.getTaskId(), task.getPriority(), priorityQueue.size());
        } else {
            success = normalQueue.offer(task);
            log.debug("任务已添加到普通队列: taskId={}, queueSize={}",
                    task.getTaskId(), normalQueue.size());
        }

        return success;
    }

    /**
     * 从队列中取出下一个任务
     * 优先从优先级队列取，如果为空则从普通队列取
     *
     * 策略：
     * 1. 优先处理优先级队列中的任务
     * 2. 优先级队列为空时，处理普通队列任务
     * 3. 两个队列都为空时，返回null
     *
     * @return 下一个要执行的任务，如果队列为空则返回null
     */
    public QueueTask poll() {
        // 优先从优先级队列取任务
        QueueTask task = priorityQueue.poll();

        if (task != null) {
            log.debug("从优先级队列取出任务: taskId={}, remainingSize={}",
                    task.getTaskId(), priorityQueue.size());
            return task;
        }

        // 优先级队列为空，从普通队列取任务
        task = normalQueue.poll();

        if (task != null) {
            log.debug("从普通队列取出任务: taskId={}, remainingSize={}",
                    task.getTaskId(), normalQueue.size());
            return task;
        }

        log.debug("所有队列都为空");
        return null;
    }

    /**
     * 查看下一个任务但不移除
     *
     * @return 下一个任务，如果队列为空则返回null
     */
    public QueueTask peek() {
        QueueTask task = priorityQueue.peek();
        if (task != null) {
            return task;
        }
        return normalQueue.peek();
    }

    /**
     * 获取优先级队列大小
     *
     * @return 优先级队列中的任务数量
     */
    public int getPriorityQueueSize() {
        return priorityQueue.size();
    }

    /**
     * 获取普通队列大小
     *
     * @return 普通队列中的任务数量
     */
    public int getNormalQueueSize() {
        return normalQueue.size();
    }

    /**
     * 获取总队列大小
     *
     * @return 所有队列中的任务总数
     */
    public int getTotalQueueSize() {
        return priorityQueue.size() + normalQueue.size();
    }

    /**
     * 检查所有队列是否都为空
     *
     * @return 所有队列都为空返回true，否则返回false
     */
    public boolean isEmpty() {
        return priorityQueue.isEmpty() && normalQueue.isEmpty();
    }

    /**
     * 清空指定类型的队列
     *
     * @param queueType 队列类型
     */
    public void clear(QueueType queueType) {
        if (queueType == QueueType.PRIORITY) {
            int size = priorityQueue.size();
            priorityQueue.clear();
            log.info("已清空优先级队列，清除 {} 个任务", size);
        } else {
            int size = normalQueue.size();
            normalQueue.clear();
            log.info("已清空普通队列，清除 {} 个任务", size);
        }
    }

    /**
     * 清空所有队列
     */
    public void clearAll() {
        int prioritySize = priorityQueue.size();
        int normalSize = normalQueue.size();

        priorityQueue.clear();
        normalQueue.clear();

        log.info("已清空所有队列，优先级队列: {} 个，普通队列: {} 个",
                prioritySize, normalSize);
    }

    /**
     * 获取指定队列
     *
     * @param queueType 队列类型
     * @return 对应的队列
     */
    public BlockingQueue<QueueTask> getQueue(QueueType queueType) {
        return queueType == QueueType.PRIORITY ? priorityQueue : normalQueue;
    }
}
