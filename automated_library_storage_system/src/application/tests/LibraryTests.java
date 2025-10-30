package application.tests;

import application.modules.*;

public class LibraryTests {
    private static int run=0, pass=0; private static java.util.List<String> fails=new java.util.ArrayList<>();

    public static void run(){
        testInit();
        testAddBook();
        testAddBook_null();
        testAddRobot_null();
        testMonitor();
        System.out.println("LibraryTests: "+pass+"/"+run+" passed");
        for(String f:fails) System.out.println("FAIL: "+f);
    }
    private static void ok(boolean c,String m){run++; if(c)pass++; else fails.add(m);}    

    private static void testInit(){ Library lib=new Library(); ok(lib.getBooks()!=null && lib.getRobots()!=null, "init should setup lists"); }
    private static void testAddBook(){ Library lib=new Library(); lib.addBook(new Book("B","A")); ok(lib.getBooks().size()==1, "addBook should add"); }
    private static void testAddBook_null(){ Library lib=new Library(); try{ lib.addBook(null); ok(false,"addBook null should throw"); } catch(RuntimeException e){ ok(true,""); } }
    private static void testAddRobot_null(){ Library lib=new Library(); try{ lib.addRobot(null); ok(false,"addRobot null should throw"); } catch(RuntimeException e){ ok(true,""); } }
    private static void testMonitor(){ Library lib=new Library(); lib.monitorSystem(); ok(true, "monitor prints"); }
}


