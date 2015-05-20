package net.acyuta.freelance;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

import java.util.HashSet;
import java.util.Set;


/**
 * Created on 5/19/15.
 *
 * @author Akim Glushkov <acyuta.lpt@gmail.com>
 */
public class Clock implements ObservableValue<Long> {

    public SimpleIntegerProperty hour = new SimpleIntegerProperty(0);
    public SimpleIntegerProperty minute = new SimpleIntegerProperty(0);
    public SimpleIntegerProperty second = new SimpleIntegerProperty(0);
    private long seconds;
    private Timeline timeline;
    private Set<ChangeListener<? super Long>> set;

    public Clock() {
        init();
    }

    public Clock(long todaySeconds) {
        seconds = todaySeconds;
        setupSeconds(seconds);
        init();
    }

    private void init() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1),
                e -> setupSeconds(getSeconds())
        ));
        timeline.setCycleCount(Timeline.INDEFINITE);
        set = new HashSet<>();
    }

    private void setupSeconds(long time) {
        hour.set(formatHours(time));
        minute.set(formatMinute(time));
        second.set(formatSeconds(time));
    }

    public long getSeconds() {
        for (ChangeListener<? super Long> v : set)
            v.changed(this,seconds,seconds+1);
        return seconds++;
    }

    public void start() {
        if (timeline != null) {
            timeline.play();
        }
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    @Override
    public String toString() {
        return String.valueOf(seconds);
    }

    public int getPreparedSeconds() {
        return formatSeconds(seconds);
    }

    private int formatSeconds(long time) {
        return (int) (time % 60);
    }

    private int formatMinute(long time) {
        return (int) ((time / 60) % 60);
    }

    private int formatHours(long time) {
        return (int) ((time / 3600) % 24);
    }


    public int getPreparedMinutes() {
        return formatMinute(seconds);
    }

    public int getPreparedHours() {
        return formatHours(seconds);
    }

    @Override
    public void addListener(ChangeListener<? super Long> listener) {
        set.add(listener);
    }

    @Override
    public void removeListener(ChangeListener<? super Long> listener) {
        set.remove(listener);
    }

    @Override
    public Long getValue() {
        return seconds;
    }

    @Override
    public void addListener(InvalidationListener listener) {

    }

    @Override
    public void removeListener(InvalidationListener listener) {

    }
}
