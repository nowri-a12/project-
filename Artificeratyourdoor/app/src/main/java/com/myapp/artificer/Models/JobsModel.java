package com.myapp.artificer.Models;

import java.io.Serializable;

public class JobsModel implements Serializable {
    String jobId, userId,workerId, workerName, ratings;

    public JobsModel() {
    }

    public JobsModel(String jobId, String userId, String workerId, String workerName) {
        this.jobId = jobId;
        this.userId = userId;
        this.workerId = workerId;
        this.workerName = workerName;
    }

    public JobsModel(String jobId, String userId, String workerId, String workerName, String ratings) {
        this.jobId = jobId;
        this.userId = userId;
        this.workerId = workerId;
        this.workerName = workerName;
        this.ratings = ratings;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getRatings() {
        return ratings;
    }

    public void setRatings(String ratings) {
        this.ratings = ratings;
    }
}
