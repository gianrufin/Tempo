package com.tempo.app.data.repository

import com.tempo.app.data.local.dao.TaskCompletionDao
import com.tempo.app.data.local.dao.TaskDao
import com.tempo.app.data.local.entity.TaskCompletionEntity
import com.tempo.app.domain.RecurrenceEngine
import com.tempo.app.domain.model.Task
import com.tempo.app.domain.model.TaskWithTodayStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val completionDao: TaskCompletionDao,
) : TaskRepository {

    override fun observeTasksForDate(date: LocalDate): Flow<List<TaskWithTodayStatus>> =
        combine(taskDao.observeActive(), completionDao.observeAll()) { entities, completions ->
            val completionsByTask = completions.groupBy { it.taskId }
            entities.mapNotNull { entity ->
                val task = entity.toDomain()
                if (date.isBefore(task.createdAt)) return@mapNotNull null

                if (task.isRecurring) {
                    val rule = task.recurrenceRule ?: return@mapNotNull null
                    if (!RecurrenceEngine.isScheduledOn(rule, date)) return@mapNotNull null
                    val done = completionsByTask[entity.id].orEmpty().any { it.date == date }
                    TaskWithTodayStatus(task, done)
                } else {
                    if (task.completedAt != null) return@mapNotNull null
                    val due = task.dueDate
                    if (due != null && due.isAfter(date)) return@mapNotNull null
                    TaskWithTodayStatus(task, false)
                }
            }
        }

    override suspend fun getAllActiveTasks(): List<Task> = taskDao.getAllActive().map { it.toDomain() }

    override suspend fun isDoneForDate(taskId: Long, date: LocalDate): Boolean {
        val entity = taskDao.getById(taskId) ?: return false
        return if (entity.isRecurring) {
            completionDao.getForTaskAndDate(taskId, date) != null
        } else {
            entity.completedAt != null
        }
    }

    override suspend fun getTask(id: Long): Task? = taskDao.getById(id)?.toDomain()

    override suspend fun addTask(task: Task): Long = taskDao.insert(task.toEntity())

    override suspend fun updateTask(task: Task) = taskDao.update(task.toEntity())

    override suspend fun archiveTask(id: Long) = taskDao.archive(id)

    override suspend fun toggleTaskDone(taskId: Long, date: LocalDate) {
        val entity = taskDao.getById(taskId) ?: return
        if (entity.isRecurring) {
            val existing = completionDao.getForTaskAndDate(taskId, date)
            if (existing != null) {
                completionDao.delete(taskId, date)
            } else {
                completionDao.upsert(TaskCompletionEntity(taskId = taskId, date = date, completedAt = Instant.now()))
            }
        } else {
            taskDao.update(entity.copy(completedAt = if (entity.completedAt == null) Instant.now() else null))
        }
    }
}
