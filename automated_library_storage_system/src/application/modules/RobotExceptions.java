package application.modules;

public class RobotExceptions extends Exception {

    public RobotExceptions(String message) {
        super(message);
    }

    public RobotExceptions(String message, Throwable cause) {
        super(message, cause);
    }

    public static class TaskNotFoundException extends RobotExceptions {
        public TaskNotFoundException(String message) {
            super(message);
        }

        public TaskNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class OverloadException extends RobotExceptions {
        public OverloadException(String message) {
            super(message);
        }

        public OverloadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class LowBatteryException extends RobotExceptions {
        public LowBatteryException(String message) {
            super(message);
        }

        public LowBatteryException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class InvalidOperationException extends RobotExceptions {
        public InvalidOperationException(String message) { super(message); }
        public InvalidOperationException(String message, Throwable cause) { super(message, cause); }
    }

    public static class ResourceUnavailableException extends RobotExceptions {
        public ResourceUnavailableException(String message) { super(message); }
        public ResourceUnavailableException(String message, Throwable cause) { super(message, cause); }
    }
}
