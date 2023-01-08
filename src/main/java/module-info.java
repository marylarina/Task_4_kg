module com.cgvsu {
    requires javafx.controls;
    requires javafx.fxml;
    requires vecmath;
    requires java.desktop;


    opens com.cgvsu to javafx.fxml;
    exports com.cgvsu;
    exports com.cgvsu.model;
    opens com.cgvsu.model to javafx.fxml;
    exports com.cgvsu.render_engine.triangle_rasterization;
    opens com.cgvsu.render_engine.triangle_rasterization to javafx.fxml;
}