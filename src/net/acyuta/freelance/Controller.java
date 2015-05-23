package net.acyuta.freelance;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

/**
 * Created on 5/19/15.
 *
 * @author Akim Glushkov <acyuta.lpt@gmail.com>
 */
public class Controller implements Initializable {

    //TODO потом настроить автосохранение

    private static String baseFilename = "base.cvs";
    private static String configFilename = "rc.conf";
    private final LocalDate startDate = LocalDate.now();
    public Clock clock;
    @FXML
    public Label hours;
    @FXML
    public Label minutes;
    @FXML
    public Label seconds;
    @FXML
    public Button actionButton;
    @FXML
    public Label weekTime;
    @FXML
    public Label salary;
    @FXML
    public NumericTextField rates;
    @FXML
    public VBox ratesBox;
    private boolean start = false;
    private BufferedWriter writer;
    private int autoSaveSecond = 10 * 60;
    private HashMap<String, Long> weekMap;
    private long weekSeconds = 0;

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
        File baseFile = new File(directory.getAbsolutePath() + File.separator + baseFilename);

        File rate = new File(directory.getAbsolutePath() + File.separator + configFilename);
        if (rate.exists()) {
            int readRate = 0;
            if ((readRate = getRates(rate)) != 0) {
                ratesBox.setVisible(false);
            }
            rates.setText(String.valueOf(readRate));
        }
        System.out.println(baseFile.getAbsolutePath());

        StringBuilder buffer = new StringBuilder();
        try {
            if (baseFile.exists()) {
                FileReader fr = new FileReader(baseFile);
                BufferedReader br = new BufferedReader(fr);

                String line;
                while ((line = br.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                br.close();
                fr.close();
                System.out.println("Считана база данных");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Исходных данных нету");
        } catch (IOException e) {
            System.out.println("Ошибка чтения базы");
        }

        weekMap = new HashMap<String, Long>();
        clock = new Clock(getTodaySeconds(buffer.toString()));
        clock.addListener((observable, oldValue, newValue) -> {
            if (newValue % autoSaveSecond == 0) {
                writeResult();
            }
            Long longRates = (rates.getText().isEmpty()) ? 0 : Long.parseLong(rates.getText());
            weekTime.setText(BigDecimal.valueOf((weekSeconds + newValue))
                    .divide(BigDecimal.valueOf(3600), 1, RoundingMode.HALF_UP)
                    .toString() + " ч.");
            salary.setText(BigDecimal.valueOf((weekSeconds + newValue))
                    .multiply(BigDecimal.valueOf(longRates))
                    .divide(BigDecimal.valueOf(3600), 1, RoundingMode.HALF_UP)
                    .toString() + " р.");
        });
        seconds.setText(String.valueOf(clock.getPreparedSeconds()));
        minutes.setText(String.valueOf(clock.getPreparedMinutes()));
        hours.setText(String.valueOf(clock.getPreparedHours()));

        hours.textProperty().bind(clock.hour.asString());
        minutes.textProperty().bind(clock.minute.asString());
        seconds.textProperty().bind(clock.second.asString());

        try {
            if (!baseFile.exists()) {
                if (!baseFile.createNewFile())
                    throw new IOException("Ошибка записи в файл БД");
            }
            writer = new BufferedWriter(new FileWriter(baseFile, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getRates(File rate) {
        try {
            Scanner scanner = new Scanner(rate);
            return (scanner.hasNextInt()) ? scanner.nextInt() : 0;
        } catch (FileNotFoundException e) {
            return 0;
        }
    }


    public void endWork(Event event) {
        writeResult();
    }

    private void writeResult() {
        LocalDate dateTime = LocalDate.now();
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
        if (line == null || line.isEmpty())
            return 0;

        String[] values = line.split("\n");
        if (values.length <= 0)
            return 0;

        LocalDate now = LocalDate.now();
        weekMap = makeWeekMap(weekMap, now);

        int seconds = 0;

        for (String v : values) {
            String[] data = v.split(";");
            if (!data[0].isEmpty() && data.length >= 2 && !data[1].isEmpty()) {
                LocalDate readedDate = LocalDate.parse(data[0]);
                if (now.compareTo(readedDate) == 0) {
                    Long value = Long.valueOf(data[1]);
                    if (value > 0)
                        seconds = (int) Math.max(seconds, value);
                }
                if (weekMap.containsKey(readedDate.toString()))
                    weekMap.put(readedDate.toString(),
                            Long.max(weekMap.get(readedDate.toString()), Long.valueOf(data[1])));
            }
        }
        for (Long entry : weekMap.values()) {
            weekSeconds += entry;
        }
        return seconds;
    }

    private HashMap<String, Long> makeWeekMap(HashMap<String, Long> weekMap, LocalDate today) {
        LocalDate it = LocalDate.from(today);
        if (today.getDayOfWeek().getValue() == 1)
            return weekMap;
        for (int i = today.minusWeeks(1).getDayOfWeek().getValue(); i >= 1; i--) {
            weekMap.put(it.toString(), (long) 0);
            it = it.minusDays(1);
        }
        return weekMap;
    }
}
