package application.tests;

import application.modules.*;

import java.util.ArrayList;

public class ChargingStationTests {
    private static int run=0, pass=0; private static java.util.List<String> fails=new java.util.ArrayList<>();

    public static void run(){
        testPlugInNoSlots();
        testPlugInWithSlot();
        testPlugOutRobot();
        testPlugInNull();
        testFindAvailableSlot();
        System.out.println("ChargingStationTests: "+pass+"/"+run+" passed");
        for(String f:fails) System.out.println("FAIL: "+f);
    }
    private static void ok(boolean c,String m){run++; if(c)pass++; else fails.add(m);}    

    private static ChargingStation stationWith(int available){
        ChargingStation cs=new ChargingStation("Test", "Test", 2);
        try {
            java.lang.reflect.Field f = ChargingStation.class.getDeclaredField("slots");
            f.setAccessible(true);
            java.util.List<Slot> list=new ArrayList<>();
            for(int i=0;i<available;i++) list.add(new Slot());
            f.set(cs, list);
        } catch (Exception e) { throw new RuntimeException(e); }
        return cs;
    }

    private static void testPlugInNoSlots(){
        ChargingStation cs=stationWith(0); Robot r=new Robot("R",1);
        try{ cs.plugInRobot(r); ok(false,"should throw when no slots"); }
        catch(RobotExceptions.ResourceUnavailableException e){ ok(true,""); }
    }
    private static void testPlugInWithSlot(){
        ChargingStation cs=stationWith(1); Robot r=new Robot("R",1);
        try{ cs.plugInRobot(r); ok(true,""); } catch(Exception e){ ok(false,"plugIn should work"); }
    }
    private static void testPlugOutRobot(){
        ChargingStation cs=stationWith(1); Robot r=new Robot("R",1);
        try{ cs.plugInRobot(r); cs.plugOutRobot(r); ok(true,""); } catch(Exception e){ ok(false,"plugOut should work"); }
    }
    private static void testPlugInNull(){
        ChargingStation cs=stationWith(1);
        try{ cs.plugInRobot(null); ok(false,"null should throw"); } catch(IllegalArgumentException e){ ok(true,""); } catch(Exception e){ ok(false,"wrong exception"); }
    }
    private static void testFindAvailableSlot(){
        ChargingStation cs=stationWith(2);
        ok(cs.findAvailableSlot()!=null, "should find slot");
    }
}


