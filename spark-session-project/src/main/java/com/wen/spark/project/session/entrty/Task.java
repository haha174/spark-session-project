package com.wen.spark.project.session.entrty;

import com.wen.spark.project.session.annotation.FieldName;

/**
 * @author : WChen129
 * @date : 2018-05-24
 */
public class Task {
    @FieldName(value = "task_id")
    private long taskid;
    @FieldName(value = "task_name")
    private String taskName;
    @FieldName(value = "create_time")
    private String createTime;
    @FieldName(value = "start_time")
    private String startTime;
    @FieldName(value = "finish_time")
    private String finishTime;
    @FieldName(value = "task_type")
    private String taskType;
    @FieldName(value = "task_status")
    private String taskStatus;
    @FieldName(value = "task_param")
    private String taskParam;

    public long getTaskid() {
        return taskid;
    }

    public void setTaskid(long taskid) {
        this.taskid = taskid;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getTaskParam() {
        return taskParam;
    }

    public void setTaskParam(String taskParam) {
        this.taskParam = taskParam;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskid=" + taskid +
                ", taskName='" + taskName + '\'' +
                ", createTime='" + createTime + '\'' +
                ", startTime='" + startTime + '\'' +
                ", finishTime='" + finishTime + '\'' +
                ", taskType='" + taskType + '\'' +
                ", taskStatus='" + taskStatus + '\'' +
                ", taskParam='" + taskParam + '\'' +
                '}';
    }
}
