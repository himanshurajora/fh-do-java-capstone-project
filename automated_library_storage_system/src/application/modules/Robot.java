package application.modules;

import java.util.ArrayList;
import java.util.List;

import application.modules.RobotExceptions.LowBatteryException;
import application.modules.RobotExceptions.OverloadException;
import application.modules.RobotExceptions.TaskNotFoundException;

public class Robot extends Resource {
    private float maxWeightKg;
    private int maxBookCount;
    private float currentChargePercent = 100;
    private boolean isDocked = false;
    private List<Book> carryingBooks = new ArrayList<>();

    public Robot(String id, float executionDuration, float maxWeightKg, int maxBookCount) {
        super(id, executionDuration);
        this.maxWeightKg = maxWeightKg;
        this.maxBookCount = maxBookCount;
    }

    public boolean canCarry(List<Book> books) {
        float totalWeight = 0;
        for (Book book : books) {
            totalWeight += book.getWeightKg();
        }
        int totalBooks = carryingBooks.size() + books.size();
        return totalWeight + getCurrentLoadWeight() <= maxWeightKg && totalBooks <= maxBookCount;
    }

    public boolean needsCharging(float thresholdPercent) {
        return currentChargePercent < thresholdPercent;
    }

    public void dock() {
        isDocked = true;
    }

    public void undock() {
        isDocked = false;
        currentChargePercent = 100;
    }

    public void addBooks(List<Book> books) {
        if (canCarry(books)) {
            carryingBooks.addAll(books);
        }
    }

    public void removeBooks(List<Book> books) {
        carryingBooks.removeAll(books);
    }

    public void execute(Task task) throws RobotExceptions {
        try {
            if (task == null) {
                throw new TaskNotFoundException("Task is null");
            }
            if (!canCarry(task.getBooks())) {
                throw new OverloadException("Overload: cannot carry books");
            }
            if (needsCharging(20)) {
                throw new LowBatteryException("Battery too low");
            }
            addBooks(task.getBooks());
        } catch (TaskNotFoundException | OverloadException | LowBatteryException e) {
            throw new RobotExceptions("Robot execution failed", e);
        } catch (Exception e) {
            throw new RobotExceptions("Error occured during execution", e);
        }
    }

    public float getCurrentLoadWeight() {
        float sum = 0;
        for (Book book : carryingBooks) {
            sum += book.getWeightKg();
        }
        return sum;
    }

    public String getId() {
        return super.getId();
    }

    public float getMaxWeightKg() {
        return maxWeightKg;
    }

    public void setMaxWeightKg(float maxWeightKg) {
        this.maxWeightKg = maxWeightKg;
    }

    public int getMaxBookCount() {
        return maxBookCount;
    }

    public void setMaxBookCount(int maxBookCount) {
        this.maxBookCount = maxBookCount;
    }

    public float getCurrentChargePercent() {
        return currentChargePercent;
    }

    public void setCurrentChargePercent(float currentChargePercent) {
        this.currentChargePercent = currentChargePercent;
    }

    public boolean isDocked() {
        return isDocked;
    }

    public void setDocked(boolean docked) {
        isDocked = docked;
    }

    public List<Book> getCarryingBooks() {
        return carryingBooks;
    }

    public void setCarryingBooks(List<Book> carryingBooks) {
        this.carryingBooks = carryingBooks;
    }
}
