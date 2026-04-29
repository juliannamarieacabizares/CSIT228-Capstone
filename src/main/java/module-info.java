module com.group8.csit228capstone {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.group8.csit228capstone to javafx.fxml;
    opens database;

    exports com.group8.csit228capstone;
}