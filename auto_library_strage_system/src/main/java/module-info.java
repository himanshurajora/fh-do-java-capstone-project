module com.group2_fhdo_capstone_project {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.group2_fhdo_capstone_project to javafx.fxml;
    exports com.group2_fhdo_capstone_project;
}
