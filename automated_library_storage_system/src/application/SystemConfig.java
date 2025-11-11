package application;

public class SystemConfig {
    private float batteryThreshold = 15.0f;
    private int logRefreshInterval = 2;
    private int numChargingStations = 2;
    private int numSlotsPerStation = 1;
    private int maxShelfCapacity = 10;
    
    public float getBatteryThreshold() {
        return batteryThreshold;
    }
    
    public void setBatteryThreshold(float batteryThreshold) {
        this.batteryThreshold = batteryThreshold;
    }
    
    public int getLogRefreshInterval() {
        return logRefreshInterval;
    }
    
    public void setLogRefreshInterval(int logRefreshInterval) {
        this.logRefreshInterval = logRefreshInterval;
    }
    
    public int getNumChargingStations() {
        return numChargingStations;
    }
    
    public void setNumChargingStations(int numChargingStations) {
        this.numChargingStations = numChargingStations;
    }
    
    public int getNumSlotsPerStation() {
        return numSlotsPerStation;
    }
    
    public void setNumSlotsPerStation(int numSlotsPerStation) {
        this.numSlotsPerStation = numSlotsPerStation;
    }
    
    public int getMaxShelfCapacity() {
        return maxShelfCapacity;
    }
    
    public void setMaxShelfCapacity(int maxShelfCapacity) {
        this.maxShelfCapacity = maxShelfCapacity;
    }
}

