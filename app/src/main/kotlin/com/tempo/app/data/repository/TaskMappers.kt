package com.tempo.app.data.repository

import com.tempo.app.data.local.entity.TaskEntity
import com.tempo.app.domain.model.Task

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    notes = notes,
    isRecurring = isRecurring,
    recurrenceRule = recurrenceRule,
    dueDate = dueDate,
    reminderTime = reminderTime,
    priority = priority,
    createdAt = createdAt,
    archived = archived,
    completedAt = completedAt,
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    notes = notes,
    isRecurring = isRecurring,
    recurrenceRule = recurrenceRule,
    dueDate = dueDate,
    reminderTime = reminderTime,
    priority = priority,
    createdAt = createdAt,
    archived = archived,
    completedAt = completedAt,
)
