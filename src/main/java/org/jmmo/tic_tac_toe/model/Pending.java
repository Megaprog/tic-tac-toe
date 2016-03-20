package org.jmmo.tic_tac_toe.model;

import org.jmmo.sc.annotation.Key;
import org.jmmo.sc.annotation.Table;

import java.util.Date;
import java.util.Objects;

@Table("pending")
public class Pending {
    public static final String KEY = "pending_players";

    @Key(1)
    private String key = KEY;
    @Key(2)
    private Date time;
    @Key(3)
    private String name;

    public Pending() {
    }

    public Pending(Date time, String name) {
        this.time = time;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pending that = (Pending) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(time, that.time) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, time, name);
    }

    @Override
    public String toString() {
        return "Pending{" +
                "time=" + time +
                ", name='" + name + '\'' +
                '}';
    }
}
