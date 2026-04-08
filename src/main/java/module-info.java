module com.group8.csit228capstone {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.group8.csit228capstone to javafx.fxml;
    exports com.group8.csit228capstone;
}