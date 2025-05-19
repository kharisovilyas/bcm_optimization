package ru.ilcorp.bcm.optimization.model.dto;

public class dtoRange {
    private double min;
    private double max;

    public dtoRange() {}

    public dtoRange(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }
}
