package com.wtx.blestation;

import java.util.Date;

public class RecvData {
    private Date time;
    private float temp;
    private float humid;
    private float press;
    private float rain;
    private WindType wind_curr;
    private WindType wind_1min;
    private WindType wind_10min;
    private float voltage;

    public static class WindType {
        public float speed;
        public int dir;

        public WindType() {
        }
    }

    public RecvData() {

    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public float getHumid() {
        return humid;
    }

    public void setHumid(float humid) {
        this.humid = humid;
    }

    public float getPress() {
        return press;
    }

    public void setPress(float press) {
        this.press = press;
    }

    public float getRain() {
        return rain;
    }

    public void setRain(float rain) {
        this.rain = rain;
    }

    public WindType getWind_curr() {
        return wind_curr;
    }

    public void setWind_curr(WindType wind_curr) {
        this.wind_curr = wind_curr;
    }

    public WindType getWind_1min() {
        return wind_1min;
    }

    public void setWind_1min(WindType wind_1min) {
        this.wind_1min = wind_1min;
    }

    public WindType getWind_10min() {
        return wind_10min;
    }

    public void setWind_10min(WindType wind_10min) {
        this.wind_10min = wind_10min;
    }

    public float getVoltage() {
        return voltage;
    }

    public void setVoltage(float voltage) {
        this.voltage = voltage;
    }

}
