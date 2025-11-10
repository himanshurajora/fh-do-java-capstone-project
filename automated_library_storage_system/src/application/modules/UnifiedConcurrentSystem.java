package application.modules;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class UnifiedConcurrentSystem {
    private final int numChargingStations;
    private final int numAGVs;
    private final List<Robot> robots;
    private final List<String> activeCharging;
    private final List<ChargingRequest> chargingQueue;
    private final List<Task> taskQueue;
    private final List<Robot> availableRobots;
    private final List<Robot> busyRobots;
    private final ExecutorService chargingExecutor;
    private final ExecutorService taskExecutor;
    private final List<Future<?>> allFutures;
    private int totalCharged = 0;
    private int totalLeftChargingQueue = 0;
    private int totalTasksCompleted = 0;
    private int totalTasksFailed = 0;
    private final long maxWaitTimeMinutes = 15;
    
    public UnifiedConcurrentSystem(int numChargingStations, int numAGVs) {
        this.numChargingStations = numChargingStations;
        this.numAGVs = numAGVs;
        this.robots = new ArrayList<>();
        this.activeCharging = new ArrayList<>();
        this.chargingQueue = new ArrayList<>();
        this.taskQueue = new ArrayList<>();
        this.availableRobots = new ArrayList<>();
        this.busyRobots = new ArrayList<>();
        this.chargingExecutor = Executors.newFixedThreadPool(numChargingStations);
        this.taskExecutor = Executors.newFixedThreadPool(numAGVs);
        this.allFutures = new ArrayList<>();
    }
    
    public void addRobot(Robot robot) {
        synchronized (this) {
            robots.add(robot);
            if (robot.getCurrentChargePercent() < 20) {
                requestCharging(robot);
            } else {
                availableRobots.add(robot);
            }
        }
    }
    
    public void addTask(Task task) {
        synchronized (this) {
            taskQueue.add(task);
        }
        processTaskQueue();
    }
    
    public void addTasks(List<Task> tasks) {
        synchronized (this) {
            taskQueue.addAll(tasks);
        }
        for (int i = 0; i < numAGVs && i < tasks.size(); i++) {
            processTaskQueue();
        }
    }
    
    private void requestCharging(Robot robot) {
        ChargingRequest request = new ChargingRequest(robot, 100.0f, LocalDateTime.now());
        
        synchronized (this) {
            if (activeCharging.size() < numChargingStations) {
                activeCharging.add(robot.getId());
                startCharging(request);
            } else {
                chargingQueue.add(request);
                application.Logger.logResources("SYSTEM", "INFO", 
                    "AGV " + robot.getId() + " added to charging queue");
            }
        }
    }
    
    private void startCharging(ChargingRequest request) {
        final Robot robot = request.getRobot();
        application.Logger.logResources("SYSTEM", "INFO", 
            "AGV " + robot.getId() + " started charging");
        
        Future<?> future = chargingExecutor.submit(() -> {
            try {
                performCharging(request);
            } finally {
                synchronized (this) {
                    activeCharging.remove(robot.getId());
                    processChargingQueue();
                }
            }
        });
        
        synchronized (this) {
            allFutures.add(future);
        }
    }
    
    private void performCharging(ChargingRequest request) {
        Robot robot = request.getRobot();
        float targetCharge = request.getTargetChargePercent();
        float currentCharge = robot.getCurrentChargePercent();
        
        robot.dock();
        
        int chargeSteps = (int) (targetCharge - currentCharge);
        
        for (int i = 0; i < chargeSteps; i++) {
            try {
                Thread.sleep(100);
                float newCharge = robot.getCurrentChargePercent() + 1.0f;
                if (newCharge > targetCharge) {
                    newCharge = targetCharge;
                }
                robot.setCurrentChargePercent(newCharge);
            } catch (InterruptedException e) {
                break;
            }
        }
        
        robot.undock();
        
        synchronized (this) {
            totalCharged++;
        }
        
        application.Logger.logResources("SYSTEM", "INFO", 
            "AGV " + robot.getId() + " completed charging. Final charge: " + 
            robot.getCurrentChargePercent() + "%");
        
        synchronized (this) {
            if (!availableRobots.contains(robot)) {
                availableRobots.add(robot);
            }
        }
        
        processTaskQueue();
    }
    
    private void processChargingQueue() {
        synchronized (this) {
            if (!chargingQueue.isEmpty() && activeCharging.size() < numChargingStations) {
                ChargingRequest nextRequest = chargingQueue.remove(0);
                long waitTimeMinutes = java.time.temporal.ChronoUnit.MINUTES.between(
                    nextRequest.getArrivalTime(), LocalDateTime.now());
                if (waitTimeMinutes <= maxWaitTimeMinutes) {
                    activeCharging.add(nextRequest.getRobot().getId());
                    startCharging(nextRequest);
                } else {
                    totalLeftChargingQueue++;
                    application.Logger.logResources("SYSTEM", "WARN", 
                        "AGV " + nextRequest.getRobot().getId() + " left charging queue after waiting " + waitTimeMinutes + " minutes");
                }
            }
        }
    }
    
    private void processTaskQueue() {
        Task taskToExecute = null;
        Robot robotToUse = null;
        
        synchronized (this) {
            if (!taskQueue.isEmpty() && !availableRobots.isEmpty()) {
                for (Robot robot : availableRobots) {
                    if (robot.getCurrentChargePercent() >= 20) {
                        taskToExecute = taskQueue.remove(0);
                        robotToUse = robot;
                        availableRobots.remove(robot);
                        busyRobots.add(robot);
                        break;
                    }
                }
            }
        }
        
        if (taskToExecute != null && robotToUse != null) {
            final Task finalTask = taskToExecute;
            final Robot finalRobot = robotToUse;
            
            application.Logger.logResources("SYSTEM", "INFO", 
                "Task " + finalTask.getTaskId() + " assigned to AGV " + finalRobot.getId());
            
            Future<?> future = taskExecutor.submit(() -> {
                try {
                    performTaskExecution(finalTask, finalRobot);
                } finally {
                    releaseRobot(finalRobot);
                    processTaskQueue();
                }
            });
            
            synchronized (this) {
                allFutures.add(future);
            }
        }
    }
    
    private void releaseRobot(Robot robot) {
        synchronized (this) {
            busyRobots.remove(robot);
            
            if (robot.getCurrentChargePercent() < 20) {
                requestCharging(robot);
            } else {
                availableRobots.add(robot);
            }
            
            application.Logger.logResources("SYSTEM", "INFO", 
                "AGV " + robot.getId() + " released");
        }
    }
    
    private void performTaskExecution(Task task, Robot robot) {
        application.Logger.logResources("SYSTEM", "INFO", 
            "Task " + task.getTaskId() + " started execution on AGV " + robot.getId());
        
        try {
            task.startTask();
            robot.execute(task);
            Thread.sleep((long) (robot.getExecutionDuration() * 1000));
            task.completeTask();
            
            synchronized (this) {
                totalTasksCompleted++;
            }
            
            application.Logger.logResources("SYSTEM", "INFO", 
                "Task " + task.getTaskId() + " completed successfully on AGV " + robot.getId());
            
        } catch (RobotExceptions e) {
            synchronized (this) {
                totalTasksFailed++;
            }
            task.cancelTask();
            
            application.Logger.logResources("SYSTEM", "ERROR", 
                "Task " + task.getTaskId() + " failed on AGV " + robot.getId() + ": " + e.getMessage());
            
        } catch (InterruptedException e) {
            task.cancelTask();
            synchronized (this) {
                totalTasksFailed++;
            }
            
            application.Logger.logResources("SYSTEM", "ERROR", 
                "Task " + task.getTaskId() + " interrupted");
        }
    }
    
    public synchronized int getActiveChargingCount() {
        return activeCharging.size();
    }
    
    public synchronized int getChargingQueueSize() {
        return chargingQueue.size();
    }
    
    public synchronized int getAvailableRobotCount() {
        return availableRobots.size();
    }
    
    public synchronized int getBusyRobotCount() {
        return busyRobots.size();
    }
    
    public synchronized int getTaskQueueSize() {
        return taskQueue.size();
    }
    
    public synchronized int getTotalCharged() {
        return totalCharged;
    }
    
    public synchronized int getTotalLeftChargingQueue() {
        return totalLeftChargingQueue;
    }
    
    public synchronized int getTotalTasksCompleted() {
        return totalTasksCompleted;
    }
    
    public synchronized int getTotalTasksFailed() {
        return totalTasksFailed;
    }
    
    public void waitForAll() {
        while (true) {
            synchronized (this) {
                if (taskQueue.isEmpty() && busyRobots.isEmpty() && 
                    chargingQueue.isEmpty() && activeCharging.isEmpty()) {
                    break;
                }
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
        
        List<Future<?>> futuresCopy;
        synchronized (this) {
            futuresCopy = new ArrayList<>(allFutures);
        }
        
        for (Future<?> future : futuresCopy) {
            try {
                future.get();
            } catch (Exception e) {
            }
        }
    }
    
    public void shutdown() {
        chargingExecutor.shutdown();
        taskExecutor.shutdown();
        try {
            if (!chargingExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                chargingExecutor.shutdownNow();
            }
            if (!taskExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                taskExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            chargingExecutor.shutdownNow();
            taskExecutor.shutdownNow();
        }
    }
    
    private static class ChargingRequest {
        private final Robot robot;
        private final float targetChargePercent;
        private final LocalDateTime arrivalTime;
        
        public ChargingRequest(Robot robot, float targetChargePercent, LocalDateTime arrivalTime) {
            this.robot = robot;
            this.targetChargePercent = targetChargePercent;
            this.arrivalTime = arrivalTime;
        }
        
        public Robot getRobot() { return robot; }
        public float getTargetChargePercent() { return targetChargePercent; }
        public LocalDateTime getArrivalTime() { return arrivalTime; }
    }
}

