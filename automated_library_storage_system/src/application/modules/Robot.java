package application.modules;

public class Robot extends Resource {


    public Robot(String id, float executionDuration) {
        super(id, executionDuration);
    }

    @Override
    public void execute(Task task) {
        // Implementation for robot execution
    }
}
