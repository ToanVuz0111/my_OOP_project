module com.demo3 {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.demo3 to javafx.fxml;
    opens com.demo3.controller to javafx.fxml;

    exports com.demo3;
    exports com.demo3.model.core;
    exports com.demo3.model.binary;
    exports com.demo3.model.generic;
    exports com.demo3.model.redblack;
    exports com.demo3.model;
    exports com.demo3.service;
    exports com.demo3.view;
    exports com.demo3.controller;
    exports com.demo3.util;
}