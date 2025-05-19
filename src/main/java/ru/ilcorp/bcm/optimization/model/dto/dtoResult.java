package ru.ilcorp.bcm.optimization.model.dto;

public class dtoResult {
    private double t1;
    private double t2;
    private double objectiveValue;

    public dtoResult() {}

    public dtoResult(double t1, double t2, double objectiveValue) {
        this.t1 = t1;
        this.t2 = t2;
        this.objectiveValue = objectiveValue;
    }

    public double getT1() {
        return t1;
    }

    public void setT1(double t1) {
        this.t1 = t1;
    }

    public double getT2() {
        return t2;
    }

    public void setT2(double t2) {
        this.t2 = t2;
    }

    public double getObjectiveValue() {
        return objectiveValue;
    }

    public void setObjectiveValue(double objectiveValue) {
        this.objectiveValue = objectiveValue;
    }
}