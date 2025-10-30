package application.modules;

import java.util.List;

public class ChargingStation {
    // slots for charging
    List<Slot> slots;


    public void plugInRobot(Robot robot) throws RobotExceptions.ResourceUnavailableException {
        if (robot == null) throw new IllegalArgumentException("robot is null");
        Slot slot = findAvailableSlot();
        if (slot == null) {
            application.Logger.logResources("charging", "ERROR", "No available slot for robot " + robot.getId());
            throw new RobotExceptions.ResourceUnavailableException("No available slot");
        }
        slot.setAvailable(false);
        application.Logger.logResources("charging", "INFO", "Robot plugged in: " + robot.getId());
    }

    public void plugOutRobot(Robot robot) {
        if (robot == null) throw new IllegalArgumentException("robot is null");
        // naive: mark first occupied as available
        for (Slot s : slots) {
            if (!s.isAvailable()) { s.setAvailable(true); break; }
        }
        application.Logger.logResources("charging", "INFO", "Robot unplugged: " + robot.getId());
    }

    public Slot findAvailableSlot() {
        for (Slot slot : slots) {
            if (slot.isAvailable()) {   
                return slot;
            }
        }
        return null;
    }

    // charging process loop
    public void executeChargingProcess() {}
}
