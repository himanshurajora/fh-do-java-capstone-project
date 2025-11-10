# Dependencies Required

This project requires the following dependencies to run:

## JavaFX
- **Version**: 11 or higher
- **Modules**: javafx.controls, javafx.fxml
- **Usage**: For the GUI application

## Gson
- **Version**: 2.10.1 or higher
- **Usage**: For JSON serialization/deserialization of system state

## How to Add Dependencies

### If using Maven (pom.xml):
```xml
<dependencies>
    <!-- JavaFX -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>17.0.2</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>17.0.2</version>
    </dependency>
    
    <!-- Gson -->
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.10.1</version>
    </dependency>
</dependencies>
```

### If using Gradle (build.gradle):
```gradle
dependencies {
    implementation 'org.openjfx:javafx-controls:17.0.2'
    implementation 'org.openjfx:javafx-fxml:17.0.2'
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

### Manual Installation:
1. Download JavaFX SDK from https://openjfx.io/
2. Download Gson JAR from https://github.com/google/gson
3. Add both to your project's classpath

## Running the Application

### From IDE (Eclipse):
1. Add JavaFX and Gson libraries to build path
2. Run `Main.java` as Java Application
3. If you encounter module errors, add VM arguments:
   ```
   --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
   ```

### From Command Line:
```bash
javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -cp gson.jar src/application/*.java src/application/modules/*.java

java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -cp .:gson.jar application.Main
```

