package model;

import util.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {
    private int id;
    private String name;
    private String description;
    private Status status;
    private long durationInSeconds;
    private String startTimeStr;
    private transient LocalDateTime startTime;
    private transient Duration duration;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public Task(String title, String description, Status status, Duration duration, LocalDateTime startTime) {
        this.name = title;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
        this.id = 0;
        syncTransientFields();
    }

    public Task(String title, String description) {
        this.name = title;
        this.description = description;
        this.status = Status.NEW;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public long getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(long durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
        this.duration = Duration.ofSeconds(durationInSeconds);
    }

    public String getStartTimeStr() {
        return startTimeStr;
    }

    public void setStartTimeStr(String startTimeStr) {
        this.startTimeStr = startTimeStr;
        this.startTime = parseDateTime(startTimeStr);
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
        syncTransientFields();
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        syncTransientFields();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isOverlapping(Task task) {
        if (this.startTime == null || task.startTime == null) {
            return false;
        }
        return !this.getEndTime().isBefore(task.startTime) &&
                !task.getEndTime().isBefore(this.startTime);
    }

    protected void syncTransientFields() {
        if (startTime != null) {
            this.startTimeStr = formatDateTime(startTime);
        }
        if (duration != null) {
            this.durationInSeconds = duration.getSeconds();
        }
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        return (dateTime != null) ? dateTime.format(FORMATTER) : null;
    }

    private static LocalDateTime parseDateTime(String str) {
        return (str != null && !str.isEmpty()) ? LocalDateTime.parse(str, FORMATTER) : null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Task)) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "title='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }
}