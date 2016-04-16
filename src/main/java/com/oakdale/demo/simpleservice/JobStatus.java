package com.oakdale.demo.simpleservice;

/**
 * Created by Alex on 15/04/2016.
 */
public class JobStatus {

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Object getResults() {
        return results;
    }

    public void setResults(Object results) {
        this.results = results;
    }

    private long id;

    private Status status;

    private Object results;

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    private String originator;

    public static enum Status { Requested, Running, Completed, Failed, TimedOut };

    public JobStatus(long id, Status status, String originator) {
        this.id = id;
        this.status = status;
        this.originator = originator;
    }

}
