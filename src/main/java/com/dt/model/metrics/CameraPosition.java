package com.dt.model.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Модель позиции камеры для метрик
 */
public class CameraPosition {
    
    @JsonProperty("x")
    private Double x;
    
    @JsonProperty("y")
    private Double y;
    
    @JsonProperty("z")
    private Double z;
    
    @JsonProperty("d")
    private Double d;
    
    @JsonProperty("heading")
    private Double heading;
    
    @JsonProperty("pitch")
    private Double pitch;
    
    @JsonProperty("roll")
    private Double roll;
    
    public CameraPosition() {
    }
    
    // Getters and Setters
    public Double getX() {
        return x;
    }
    
    public void setX(Double x) {
        this.x = x;
    }
    
    public Double getY() {
        return y;
    }
    
    public void setY(Double y) {
        this.y = y;
    }
    
    public Double getZ() {
        return z;
    }
    
    public void setZ(Double z) {
        this.z = z;
    }
    
    public Double getD() {
        return d;
    }
    
    public void setD(Double d) {
        this.d = d;
    }
    
    public Double getHeading() {
        return heading;
    }
    
    public void setHeading(Double heading) {
        this.heading = heading;
    }
    
    public Double getPitch() {
        return pitch;
    }
    
    public void setPitch(Double pitch) {
        this.pitch = pitch;
    }
    
    public Double getRoll() {
        return roll;
    }
    
    public void setRoll(Double roll) {
        this.roll = roll;
    }
}
