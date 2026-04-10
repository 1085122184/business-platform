package com.cjx.uibot.repository.uibot;

import com.cjx.common.jpa.repository.BaseRepository;
import com.cjx.uibot.entity.uibot.Task;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 任务数据访问接口
 *
 * @author Task Queue System
 * @version 1.0
 * @since 2025-11-12
 */
@Repository
public interface TaskRepository extends BaseRepository<Task> {
    /**
     * 根据状态查询任务列表
     *
     * @param status 任务状态
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    Page<Task> findByStatus(Task.TaskStatus status, Pageable pageable);

    /**
     * 根据状态查询任务数量
     *
     * @param status 任务状态
     * @return 任务数量
     */
    long countByStatus(Task.TaskStatus status);

    /**
     * 查询所有未完成的任务（用于系统重启时恢复）
     *
     * @return 未完成任务列表
     */
    @Query("SELECT t FROM Task t WHERE t.status IN :statuses ORDER BY t.priority ASC, t.createTime ASC")
    List<Task> findIncompleteTasksOrderByPriority(@Param("statuses") List<Task.TaskStatus> statuses);

    /**
     * 查询正在执行的任务（带悲观锁）
     *
     * @return 正在执行的任务
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Task t WHERE t.status = 'RUNNING'")
    Optional<Task> findRunningTaskWithLock();

    /**
     * 查询超时的任务
     *
     * @param status 任务状态
     * @param timeoutThreshold 超时阈值时间
     * @return 超时任务列表
     */
    @Query("SELECT t FROM Task t WHERE t.status = :status " +
            "AND t.startedAt IS NOT NULL " +
            "AND t.startedAt < :timeoutThreshold")
    List<Task> findTimeoutTasks(@Param("status") Task.TaskStatus status,
                                @Param("timeoutThreshold") LocalDateTime timeoutThreshold);

    /**
     * 批量更新超时任务状态
     *
     * @param taskIds 任务ID列表
     * @param newStatus 新状态
     * @param errorMessage 错误信息
     * @param completedAt 完成时间
     * @return 更新数量
     */
    @Modifying
    @Query("UPDATE Task t SET t.status = :newStatus, t.errorMessage = :errorMessage, " +
            "t.completedAt = :completedAt WHERE t.id IN :taskIds")
    int batchUpdateStatus(@Param("taskIds") List<String> taskIds,
                          @Param("newStatus") Task.TaskStatus newStatus,
                          @Param("errorMessage") String errorMessage,
                          @Param("completedAt") LocalDateTime completedAt);

    /**
     * 查询指定时间范围内的任务统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Query("SELECT t.status as status, COUNT(t) as count FROM Task t " +
            "WHERE t.createTime BETWEEN :startTime AND :endTime " +
            "GROUP BY t.status")
    List<Object[]> getTaskStatistics(@Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 删除指定天数之前的已完成任务
     *
     * @param beforeDate 截止日期
     * @param statuses 要删除的状态列表
     * @return 删除数量
     */
    @Modifying
    @Query("DELETE FROM Task t WHERE t.completedAt < :beforeDate AND t.status IN :statuses")
    int deleteOldCompletedTasks(@Param("beforeDate") LocalDateTime beforeDate,
                                @Param("statuses") List<Task.TaskStatus> statuses);
}
