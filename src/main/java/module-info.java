module com.group8.csit228capstone {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;


    opens com.group8.csit228capstone to javafx.fxml;
    exports com.group8.csit228capstone;
}