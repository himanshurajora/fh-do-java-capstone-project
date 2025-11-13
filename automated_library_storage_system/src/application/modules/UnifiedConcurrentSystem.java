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
    private final ScheduledExecutorService queueProcessor;
    private final List<Future<?>> allFutures;
    private int totalCharged = 0;
    private int totalLeftChargingQueue = 0;
    private int totalTasksCompleted = 0;
    private int totalTasksFailed = 0;
    private final long maxWaitTimeMinutes = 15;
    
    // Charging station integration
    private List<ChargingStation> chargingStations;
    private int totalSlots;
    
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
        this.queueProcessor = Executors.newScheduledThreadPool(1);
        this.allFutures = new ArrayList<>();
        this.chargingStations = new ArrayList<>();
        this.totalSlots = 0;
        
        // Start periodic queue processor (checks every 2 seconds)
        queueProcessor.scheduleAtFixedRate(() -> {
            try {
                processChargingQueue();
            } catch (Exception e) {
                application.Logger.logSystem("ERROR", "Queue processor error: " + e.getMessage());
            }
        }, 2, 2, TimeUnit.SECONDS);
    }
    
    public void setChargingStations(List<ChargingStation> stations) {
        synchronized (this) {
            this.chargingStations = new ArrayList<>(stations);
            this.totalSlots = 0;
            for (ChargingStation station : stations) {
                this.totalSlots += station.getTotalSlots();
            }
            application.Logger.logSystem("INFO", "Charging stations configured: " + 
                stations.size() + " stations, " + totalSlots + " total slots");
            
            // Trigger queue processing in case there were queued robots
            if (!chargingQueue.isEmpty()) {
                application.Logger.logSystem("INFO", 
                    "Processing existing queue of " + chargingQueue.size() + " robots");
            }
        }
        // Process queue after setting stations
        processChargingQueue();
    }
    
    public List<ChargingRequest> getChargingQueue() {
        synchronized (this) {
            return new ArrayList<>(chargingQueue);
        }
    }
    
    public void addRobot(Robot robot) {
        synchronized (this) {
            robots.add(robot);
            if (robot.getCurrentChargePercent() < robot.getBatteryThreshold()) {
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
            // Check if we have available charging slots
            if (activeCharging.size() < totalSlots) {
                // Try to find an available slot and plug in
                ChargingStation availableStation = findAvailableChargingStation();
                if (availableStation != null) {
                    try {
                        availableStation.plugInRobot(robot);
                        activeCharging.add(robot.getId());
                        request.setChargingStation(availableStation);
                        startCharging(request);
                        
                        application.Logger.logResources("SYSTEM", "INFO", 
                            robot.getId() + " plugged into " + availableStation.getId() + 
                            " (Queue: " + chargingQueue.size() + ")");
                    } catch (RobotExceptions.ResourceUnavailableException e) {
                        // Slot not available, add to queue
                        chargingQueue.add(request);
                        application.Logger.logResources("SYSTEM", "WARN", 
                            robot.getId() + " added to charging queue - " + e.getMessage());
                    }
                } else {
                    chargingQueue.add(request);
                    application.Logger.logResources("SYSTEM", "INFO", 
                        robot.getId() + " added to charging queue (no slots available)");
                }
            } else {
                chargingQueue.add(request);
                application.Logger.logResources("SYSTEM", "INFO", 
                    robot.getId() + " added to charging queue (Position: " + (chargingQueue.size() + 1) + ")");
            }
        }
    }
    
    private ChargingStation findAvailableChargingStation() {
        synchronized (this) {
            for (ChargingStation station : chargingStations) {
                int availableSlots = station.getAvailableSlots();
                application.Logger.logResources("SYSTEM", "DEBUG", 
                    station.getId() + " has " + availableSlots + " available slots");
                if (availableSlots > 0) {
                    return station;
                }
            }
            application.Logger.logResources("SYSTEM", "DEBUG", 
                "No charging stations with available slots");
            return null;
        }
    }
    
    private void startCharging(ChargingRequest request) {
        final Robot robot = request.getRobot();
        final ChargingStation station = request.getChargingStation();
        
        application.Logger.logResources("SYSTEM", "INFO", 
            robot.getId() + " started charging at " + 
            (station != null ? station.getId() : "unknown station"));
        
        Future<?> future = chargingExecutor.submit(() -> {
            try {
                performCharging(request);
            } finally {
                // Unplug robot from station FIRST (frees the slot)
                if (station != null) {
                    station.plugOutRobot(robot);
                    application.Logger.logResources("SYSTEM", "INFO", 
                        robot.getId() + " unplugged from " + station.getId() + 
                        " - slot now available");
                }
                
                synchronized (this) {
                    activeCharging.remove(robot.getId());
                }
                
                // Process queue AFTER slot is freed
                processChargingQueue();
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
        // Keep processing queue while we have capacity and waiting robots
        while (true) {
            ChargingRequest nextRequest = null;
            int currentlyCharging = 0;
            int queueSize = 0;
            int slots = 0;
            
            synchronized (this) {
                currentlyCharging = activeCharging.size();
                queueSize = chargingQueue.size();
                slots = totalSlots;
                
                if (queueSize > 0) {
                    application.Logger.logResources("SYSTEM", "INFO", 
                        "Queue check - Charging: " + currentlyCharging + 
                        "/" + slots + ", Queue: " + queueSize);
                }
                
                if (!chargingQueue.isEmpty() && currentlyCharging < slots && slots > 0) {
                    nextRequest = chargingQueue.remove(0);
                } else {
                    if (queueSize > 0 && currentlyCharging >= slots) {
                        application.Logger.logResources("SYSTEM", "INFO", 
                            "All " + slots + " slots occupied, " + queueSize + " robots waiting");
                    }
                    break;
                }
            }
            
            if (nextRequest != null) {
                Robot robot = nextRequest.getRobot(); // Define robot at outer scope
                long waitTimeMinutes = java.time.temporal.ChronoUnit.MINUTES.between(
                    nextRequest.getArrivalTime(), LocalDateTime.now());
                    
                if (waitTimeMinutes <= maxWaitTimeMinutes) {
                    ChargingStation availableStation = findAvailableChargingStation();
                    
                    if (availableStation != null) {
                        try {
                            availableStation.plugInRobot(robot);
                            
                            synchronized (this) {
                                activeCharging.add(robot.getId());
                            }
                            
                            nextRequest.setChargingStation(availableStation);
                            
                            application.Logger.logResources("SYSTEM", "INFO", 
                                robot.getId() + " removed from queue and plugged into " + 
                                availableStation.getId() + " (Queue remaining: " + chargingQueue.size() + ")");
                            
                            startCharging(nextRequest);
                            
                        } catch (RobotExceptions.ResourceUnavailableException e) {
                            // Put back in queue if slot not available
                            synchronized (this) {
                                chargingQueue.add(0, nextRequest);
                            }
                            application.Logger.logResources("SYSTEM", "WARN", 
                                robot.getId() + " could not plug in, returned to queue");
                            break; // Stop processing if slot issue
                        }
                    } else {
                        // Put back in queue - no available station
                        synchronized (this) {
                            chargingQueue.add(0, nextRequest);
                        }
                        application.Logger.logResources("SYSTEM", "WARN", 
                            "No available station found, " + robot.getId() + " returned to queue");
                        break; // Stop processing if no stations
                    }
                } else {
                    // Robot waited too long, remove from queue
                    synchronized (this) {
                        totalLeftChargingQueue++;
                    }
                    application.Logger.logResources("SYSTEM", "WARN", 
                        robot.getId() + " left charging queue after waiting " + 
                        waitTimeMinutes + " minutes");
                    // Continue processing next in queue
                }
            } else {
                break; // No more requests
            }
        }
    }
    
    private void processTaskQueue() {
        Task taskToExecute = null;
        Robot robotToUse = null;
        
        synchronized (this) {
            if (!taskQueue.isEmpty() && !availableRobots.isEmpty()) {
                // Peek at the first task to check battery requirements
                Task candidateTask = taskQueue.get(0);
                float requiredBattery = candidateTask.getBatteryRequired();
                
                for (Robot robot : new ArrayList<>(availableRobots)) {
                    // Check if robot has enough battery for assignment
                    if (robot.getCurrentChargePercent() < robot.getBatteryThreshold()) {
                        // Reject assignment - send robot to charging
                        application.Logger.logResources("SYSTEM", "WARN", 
                            robot.getId() + " rejected task assignment - battery too low (" + 
                            String.format("%.1f", robot.getCurrentChargePercent()) + "%)");
                        
                        availableRobots.remove(robot);
                        requestCharging(robot);
                        continue; // Try next robot
                    }
                    
                    // Check if robot has enough battery for THIS specific task
                    if (robot.getCurrentChargePercent() < requiredBattery) {
                        application.Logger.logResources("SYSTEM", "WARN", 
                            robot.getId() + " cannot execute task - needs " + 
                            String.format("%.1f", requiredBattery) + "% but has " + 
                            String.format("%.1f", robot.getCurrentChargePercent()) + "%");
                        continue; // Try next robot
                    }
                    
                    // Robot has enough battery for the task
                    taskToExecute = taskQueue.remove(0);
                    robotToUse = robot;
                    availableRobots.remove(robot);
                    busyRobots.add(robot);
                    break;
                }
            }
        }
        
        if (taskToExecute != null && robotToUse != null) {
            final Task finalTask = taskToExecute;
            final Robot finalRobot = robotToUse;
            
            application.Logger.logResources("SYSTEM", "INFO", 
                "Task " + finalTask.getTaskId() + " assigned to " + finalRobot.getId() + 
                " (Battery: " + String.format("%.1f", finalRobot.getCurrentChargePercent()) + "%)");
            
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
            
            // Check if robot needs charging after task completion
            if (robot.getCurrentChargePercent() < robot.getBatteryThreshold()) {
                application.Logger.logResources("SYSTEM", "INFO", 
                    robot.getId() + " released - battery low (" + 
                    String.format("%.1f", robot.getCurrentChargePercent()) + "%), sending to charge");
                requestCharging(robot);
            } else {
                availableRobots.add(robot);
                application.Logger.logResources("SYSTEM", "INFO", 
                    robot.getId() + " released - available for tasks (Battery: " + 
                    String.format("%.1f", robot.getCurrentChargePercent()) + "%)");
            }
        }
    }
    
    private void performTaskExecution(Task task, Robot robot) {
        application.Logger.logResources("SYSTEM", "INFO", 
            "Task " + task.getTaskId() + " started execution on " + robot.getId());
        
        try {
            task.startTask();
            robot.execute(task);
            
            // Pick up book if related task
            Book book = task.getRelatedBook();
            if (book != null) {
                robot.pickUpBook(book);
                book.setStatus(Book.BookStatus.IN_TRANSIT);
                book.setAssignedRobotId(robot.getId());
                application.Logger.logResources("SYSTEM", "INFO", 
                    robot.getId() + " picked up book: " + book.getTitle());
            }
            
            // Task execution based on shelf distance
            int taskDuration = task.getTaskDurationSeconds();
            float batteryDrain = task.getBatteryRequired();
            
            application.Logger.logResources("SYSTEM", "INFO", 
                "Task will take " + taskDuration + " seconds, drain " + 
                String.format("%.1f", batteryDrain) + "% battery");
            
            Thread.sleep(taskDuration * 1000);
            
            // Battery drain based on distance
            float newBattery = Math.max(0, robot.getCurrentChargePercent() - batteryDrain);
            robot.setCurrentChargePercent(newBattery);
            
            // Deliver book
            if (book != null) {
                robot.deliverBook();
                
                // Determine final status based on task type
                if (task.getTaskName().contains("Return")) {
                    // Return task - book goes back to shelf (AVAILABLE)
                    book.setStatus(Book.BookStatus.AVAILABLE);
                    application.Logger.logResources("SYSTEM", "INFO", 
                        robot.getId() + " returned book: " + book.getTitle() + " to " + book.getShelfId());
                } else {
                    // Get task - book is taken by user
                    book.setStatus(Book.BookStatus.TAKEN);
                    application.Logger.logResources("SYSTEM", "INFO", 
                        robot.getId() + " delivered book: " + book.getTitle() + " to user");
                }
                
                book.setAssignedRobotId(null);
            }
            
            robot.completeTask();
            task.completeTask();
            
            synchronized (this) {
                totalTasksCompleted++;
            }
            
            application.Logger.logResources("SYSTEM", "INFO", 
                "Task " + task.getTaskId() + " completed successfully on " + robot.getId() + 
                " (Battery: " + String.format("%.1f", robot.getCurrentChargePercent()) + "%)");
            
        } catch (RobotExceptions e) {
            synchronized (this) {
                totalTasksFailed++;
            }
            task.cancelTask();
            
            Book book = task.getRelatedBook();
            if (book != null) {
                book.setStatus(Book.BookStatus.AVAILABLE);
                book.setAssignedRobotId(null);
            }
            
            application.Logger.logResources("SYSTEM", "ERROR", 
                "Task " + task.getTaskId() + " failed on " + robot.getId() + ": " + e.getMessage());
            
        } catch (InterruptedException e) {
            task.cancelTask();
            synchronized (this) {
                totalTasksFailed++;
            }
            
            Book book = task.getRelatedBook();
            if (book != null) {
                book.setStatus(Book.BookStatus.AVAILABLE);
                book.setAssignedRobotId(null);
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
        queueProcessor.shutdown();
        chargingExecutor.shutdown();
        taskExecutor.shutdown();
        try {
            if (!queueProcessor.awaitTermination(5, TimeUnit.SECONDS)) {
                queueProcessor.shutdownNow();
            }
            if (!chargingExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                chargingExecutor.shutdownNow();
            }
            if (!taskExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                taskExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            queueProcessor.shutdownNow();
            chargingExecutor.shutdownNow();
            taskExecutor.shutdownNow();
        }
    }
    
    public static class ChargingRequest {
        private final Robot robot;
        private final float targetChargePercent;
        private final LocalDateTime arrivalTime;
        private ChargingStation chargingStation;
        
        public ChargingRequest(Robot robot, float targetChargePercent, LocalDateTime arrivalTime) {
            this.robot = robot;
            this.targetChargePercent = targetChargePercent;
            this.arrivalTime = arrivalTime;
        }
        
        public Robot getRobot() { return robot; }
        public float getTargetChargePercent() { return targetChargePercent; }
        public LocalDateTime getArrivalTime() { return arrivalTime; }
        public ChargingStation getChargingStation() { return chargingStation; }
        public void setChargingStation(ChargingStation station) { this.chargingStation = station; }
    }
}

