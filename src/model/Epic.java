package model;

import util.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private List<Integer> subtaskIds;
    private transient LocalDateTime startTime;
    private transient Duration duration;
    private transient LocalDateTime endTime;
    private String endTimeStr;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Epic(String name, String description) {
        super(name, description, Status.NEW, null, null);
        this.subtaskIds = new ArrayList<>();
    }

    public String getEndTimeStr() {
        return endTime != null ? endTime.format(FORMATTER) : null;
    }

    public void setEndTimeStr(String endTimeStr) {
        this.endTimeStr = endTimeStr;
        this.endTime = (endTimeStr != null && !endTimeStr.isEmpty())
                ? LocalDateTime.parse(endTimeStr, FORMATTER)
                : null;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        syncTransientFields();
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public void setDuration(Duration duration) {
        this.duration = duration;
        syncTransientFields();
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        if (endTime != null) {
            this.endTimeStr = endTime.format(FORMATTER);
        }
    }

    /**
     * Проверка на пустоту списка подзадач
     * Если у epic нет подзадач, сброс всех временных параметров в null
     * Поиск самого раннего startTime
     */
    public void updateTimeParameters(List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            this.setStartTime(null);
            this.setDuration(null);
            this.setEndTime(null);
            return;
        }

        LocalDateTime earliestStart = subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime latestEnd = subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        this.setStartTime(earliestStart);
        this.endTime = latestEnd;
        if (earliestStart != null && latestEnd != null) {
            this.setDuration(Duration.between(earliestStart, latestEnd));
        } else {
            this.setDuration(null);
        }
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(List<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public void addSubtask(int subtaskId) {
        if (subtaskId == this.getId()) return;

        if (subtaskIds == null) {
            subtaskIds = new ArrayList<>();
        }

        if (!subtaskIds.contains(subtaskId)) {
            subtaskIds.add(subtaskId);
        }
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Epic{" +
                ", " + super.toString() +
                "subtasksIds=" + subtaskIds +
                '}';
    }

    protected void syncTransientFields() {
        super.syncTransientFields();

        if (endTime != null) {
            this.endTimeStr = endTime.format(FORMATTER);
        } else {
            this.endTimeStr = null;
        }
    }
}