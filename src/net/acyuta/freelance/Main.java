package net.acyuta.freelance;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class Main extends Application {



    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("clock.fxml"));
        primaryStage.setTitle("Фрилансера время");
        Scene scene = new Scene(root, 350, 350);

        primaryStage.setScene(scene);
        primaryStage.show();
    }




}