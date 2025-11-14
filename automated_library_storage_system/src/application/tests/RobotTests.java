package application.tests;

import application.modules.Book;
import application.modules.Robot;
import application.modules.Task;
import application.modules.TaskPriority;
import application.modules.TaskStatus;
import application.modules.RobotExceptions;

import java.util.Arrays;

public class RobotTests {
    private static int run=0, pass=0; private static java.util.List<String> fails=new java.util.ArrayList<>();

    public static void run(){
        testDockUndock();
        testAddRemoveBooks();
        testExecuteNullTask();
        testExecuteLowBattery();
        testExecuteOverload();
        System.out.println("RobotTests: "+pass+"/"+run+" passed");
        for(String f:fails) System.out.println("FAIL: "+f);
    }
    private static void ok(boolean c,String m){run++; if(c)pass++; else fails.add(m);}    

    private static Robot robot(){ return new Robot("R1", 1); }

    private static void testDockUndock(){
        Robot r=robot(); r.dock(); r.undock(); ok(r.getCurrentChargePercent()==100, "undock should charge to 100");
    }

    private static void testAddRemoveBooks(){
        Robot r=robot(); Book b=new Book("B","A", "B", "B");
    }

    private static void testExecuteNullTask(){
        Robot r=robot();
        try { r.execute(null); ok(false, "execute should throw TaskNotFound"); }
        catch (RobotExceptions e){ ok(true, ""); }
    }

    private static void testExecuteLowBattery(){
        Robot r=robot(); r.setCurrentChargePercent(10);
        Task t=new Task("T","N","d", TaskPriority.LOW, "R1");
        try { r.execute(t); ok(false, "execute should throw LowBattery"); }
        catch (RobotExceptions e){ ok(true, ""); }
    }

    private static void testExecuteOverload(){
        Robot r=new Robot("R1",1);
        Task t=new Task("T","N","d", TaskPriority.LOW, "R1");
        try { r.execute(t); ok(false, "execute should throw Overload"); }
        catch (RobotExceptions e){ ok(true, ""); }
    }
}


