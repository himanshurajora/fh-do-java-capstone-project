package com.group2_fhdo_capstone_project.modules;

public abstract class Resource {
    
    // unique id
    protected String id;
    // fixed execution duration for each task
    // per resource
    public float  executionDuration; 

    Resource(String id, float executionDuration) {
        this.id = id;
        this.executionDuration = executionDuration;
    }

    // the execution method
    public void execute(Task task) {}
}
