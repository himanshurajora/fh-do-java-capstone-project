package application.modules;

public class Slot {
    Robot robot;

    public void plugInRobot(Robot robot) {
        this.robot = robot;
    }

    public void plugOutRobot() {
        this.robot = null;
    }

    public Robot getRobot() {
        return robot;
    }

    public boolean isAvailable() {
        return this.robot == null;
    }
}
