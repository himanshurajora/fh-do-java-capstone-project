package application.tests;

import application.modules.TaskManager;
import application.modules.TaskPriority;
import application.modules.TaskStatus;
import application.modules.RobotExceptions;

public class TaskManagerTests {
    private static int run = 0, pass = 0; private static java.util.List<String> fails = new java.util.ArrayList<>();

    public static void run() {
        testCreateTask();
        testStartTask();
        testCompleteTask();
        testCancelTask();
        testStartTask_notFound();
        System.out.println("TaskManagerTests: " + pass + "/" + run + " passed");
        for (String f : fails) System.out.println("FAIL: " + f);
    }

    private static void ok(boolean c, String m){run++; if(c)pass++; else fails.add(m);}    

    private static void testCreateTask(){
        TaskManager tm=new TaskManager();
        var t=tm.createTask("T1","d", TaskPriority.MEDIUM, "R1");
        ok(t!=null && tm.getAllTasks().size()==1, "createTask should add one task");
    }
    private static void testStartTask(){
        TaskManager tm=new TaskManager(); var t=tm.createTask("T","d", TaskPriority.LOW, "R1");
        try { tm.startTask(t.getTaskId());
            ok(t.getStatus()== TaskStatus.IN_PROGRESS, "startTask should set IN_PROGRESS");
        } catch (RobotExceptions.TaskNotFoundException e) { ok(false, "startTask should not throw for valid id"); }
    }
    private static void testCompleteTask(){
        TaskManager tm=new TaskManager(); var t=tm.createTask("T","d", TaskPriority.LOW, "R1");
        try { tm.startTask(t.getTaskId()); tm.completeTask(t.getTaskId());
            ok(t.getStatus()== TaskStatus.COMPLETED, "completeTask should set COMPLETED");
        } catch (RobotExceptions.TaskNotFoundException e) { ok(false, "completeTask should not throw for valid id"); }
    }
    private static void testCancelTask(){
        TaskManager tm=new TaskManager(); var t=tm.createTask("T","d", TaskPriority.LOW, "R1");
        try { tm.cancelTask(t.getTaskId());
            ok(t.getStatus()== TaskStatus.CANCELLED, "cancelTask should set CANCELLED");
        } catch (RobotExceptions.TaskNotFoundException e) { ok(false, "cancelTask should not throw for valid id"); }
    }
    private static void testStartTask_notFound(){
        TaskManager tm=new TaskManager();
        try { tm.startTask("NOPE"); ok(false, "startTask should throw for missing"); }
        catch (RobotExceptions.TaskNotFoundException e){ ok(true, ""); }
    }
}


