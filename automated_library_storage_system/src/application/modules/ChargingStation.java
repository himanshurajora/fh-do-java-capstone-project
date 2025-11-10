package application.modules;

import java.util.ArrayList;
import java.util.List;

public class ChargingStation {
    private String id;
    private String name;
    private List<Slot> slots;

    public ChargingStation(String id, String name, int numSlots) {
        this.id = id;
        this.name = name;
        this.slots = new ArrayList<>();
        for (int i = 0; i < numSlots; i++) {
            slots.add(new Slot());
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public List<Slot> getSlots() { return new ArrayList<>(slots); }
    
    public int getTotalSlots() { return slots.size(); }
    
    public int getOccupiedSlots() {
        int count = 0;
        for (Slot slot : slots) {
            if (!slot.isAvailable()) count++;
        }
        return count;
    }
    
    public int getAvailableSlots() {
        return getTotalSlots() - getOccupiedSlots();
    }

    public void plugInRobot(Robot robot) throws RobotExceptions.ResourceUnavailableException {
        if (robot == null) throw new IllegalArgumentException("robot is null");
        Slot slot = findAvailableSlot();
        if (slot == null) {
            application.Logger.logResources(id, "ERROR", "No available slot for robot " + robot.getId());
            throw new RobotExceptions.ResourceUnavailableException("No available slot");
        }
        slot.plugInRobot(robot);
        application.Logger.logResources(id, "INFO", "Robot plugged in: " + robot.getId() + " (" + getOccupiedSlots() + "/" + getTotalSlots() + ")");
    }

    public void plugOutRobot(Robot robot) {
        if (robot == null) throw new IllegalArgumentException("robot is null");
        for (Slot slot : slots) {
            if (!slot.isAvailable() && slot.robot != null && slot.robot.getId().equals(robot.getId())) {
                slot.plugOutRobot();
                application.Logger.logResources(id, "INFO", "Robot unplugged: " + robot.getId() + " (" + getOccupiedSlots() + "/" + getTotalSlots() + ")");
                return;
            }
        }
        application.Logger.logResources(id, "WARN", "Robot not found in station: " + robot.getId());
    }

    public Slot findAvailableSlot() {
        for (Slot slot : slots) {
            if (slot.isAvailable()) {   
                return slot;
            }
        }
        return null;
    }

    public void executeChargingProcess() {}
}
