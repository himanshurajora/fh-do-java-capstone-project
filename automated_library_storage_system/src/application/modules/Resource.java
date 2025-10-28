package application.modules;

public abstract class Resource {

   
    protected String id;

    
    protected float executionDuration; 

    public Resource(String id, float executionDuration) {
        this.id = id;
        this.executionDuration = executionDuration;
    }

    // abstract method to execute a task
    public abstract void execute(Task task);

 
    public String getId() {
        return id;
    }

   
    public float getExecutionDuration() {
        return executionDuration;
    }
}
