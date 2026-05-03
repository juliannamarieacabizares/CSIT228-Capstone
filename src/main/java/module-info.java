module com.group8.csit228capstone {
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;

    opens com.group8.csit228capstone to javafx.fxml;
    opens database;

    exports com.group8.csit228capstone;
}