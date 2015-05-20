package net.acyuta.freelance;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

/**
 * Created on 5/19/15.
 *
 * @author Akim Glushkov <acyuta.lpt@gmail.com>
 */
public class Controller implements Initializable {

    //TODO потом настроить автосохранение

    private static String filename = "base.cvs";

    public Clock clock;
    @FXML
    public Label hours;
    @FXML
    public Label minutes;
    @FXML
    public Label seconds;
    @FXML
    public Button actionButton;

    private boolean start = false;
    private BufferedWriter writer;
    private int autoSaveSecond = 10 * 60;


    @FXML
    public void changeTimer(ActionEvent actionEvent) {
        if (start) {
            clock.stop();
            writeResult();
            actionButton.setText("Начать работу");
        } else {
            clock.start();
            actionButton.setText("Приостановить работу");
        }
        start = !start;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        File directory = new File(System.getProperty("user.home") + File.separator + ".fltime");
        if (!directory.exists())
            directory.mkdir();
        File file = new File(directory.getAbsolutePath() + File.separator + filename);
        System.out.println(file.getAbsolutePath());
        StringBuilder buffer = new StringBuilder();
        try {
            if (file.exists()) {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);

                String line;
                while ((line = br.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                br.close();
                fr.close();

            }
        } catch (FileNotFoundException e) {
            System.out.println("Исходных данных нету");
        } catch (IOException e) {
            System.out.println("Ошибка чтения базы");
        }

        clock = new Clock(getTodaySeconds(buffer.toString()));
        clock.addListener((observable, oldValue, newValue) -> {
            if (newValue % autoSaveSecond == 0)
                writeResult();
        });
        seconds.setText(String.valueOf(clock.getPreparedSeconds()));
        minutes.setText(String.valueOf(clock.getPreparedMinutes()));
        hours.setText(String.valueOf(clock.getPreparedHours()));

        hours.textProperty().bind(clock.hour.asString());
        minutes.textProperty().bind(clock.minute.asString());
        seconds.textProperty().bind(clock.second.asString());


        try {
            if (!file.exists()) {
                if (!file.createNewFile())
                    throw new IOException("Ошибка записи в файл БД");
            }
            writer = new BufferedWriter(new FileWriter(filename, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void endWork(Event event) {
        writeResult();
    }

    private void writeResult() {
        LocalDateTime dateTime = LocalDateTime.now();
        String result = dateTime.toString() + ';' + clock.toString() + ";";
        System.out.println(result);

        try {
            writer.write(result + "\n");
            writer.flush();
        } catch (IOException e) {
            System.out.println("Ошибка записи данных");
        }

    }


    private long getTodaySeconds(String line) {
        System.out.println(line);
        if (line == null || line.isEmpty())
            return 0;

        String[] values = line.split("\n");
        if (values.length <= 0)
            return 0;

        LocalDate now = LocalDate.now();
        int seconds = 0;

        for (String v : values) {
            String[] data = v.split(";");
            if (!data[0].isEmpty() && data.length >= 2 && !data[1].isEmpty())
                if (now.compareTo(
                        LocalDate.parse(data[0],
                                DateTimeFormatter.ISO_DATE_TIME)) == 0) {
                    Long value = Long.valueOf(data[1]);
                    if (value > 0)
                        seconds = (int) Math.max(seconds, value);
                }
        }
        return seconds;
    }
}
