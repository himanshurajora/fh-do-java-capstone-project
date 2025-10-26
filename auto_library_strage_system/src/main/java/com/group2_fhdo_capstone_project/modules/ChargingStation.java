package com.group2_fhdo_capstone_project.modules;

import java.util.List;

public class ChargingStation {
    // slots for charging
    List<Slot> slots;


    public void plugInRobot(Robot robot) {}

    public void plugOutRobot(Robot robot) {}

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
