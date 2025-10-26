package com.group2_fhdo_capstone_project.modules;

public class Slot {
     Robot robot;

     public void plugInRobot(Robot robot) {
         this.robot = robot;
     }

     public void plugOutRobot() {
         this.robot = null;
     }

     public boolean isAvailable() {
         return this.robot == null;
     }

     public void chargeRobot() {
        // we will charge by 1 percent per unit time.
        // so there will be a call from the charging station charging execution process function
        // as per it's process and we'll increase the charging by one percent per unit time/call
     }
}
