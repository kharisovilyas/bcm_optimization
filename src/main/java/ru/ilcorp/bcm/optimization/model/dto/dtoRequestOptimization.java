package ru.ilcorp.bcm.optimization.model.dto;

public class dtoRequestOptimization {
    private dtoRange t1Range;
    private dtoRange t2Range;
    private String method;
    private double alpha;
    private double beta;
    private double gamma;

    // Геттеры и сеттеры
    public dtoRange getT1Range() {
        return t1Range;
    }

    public void setT1Range(dtoRange t1Range) {
        this.t1Range = t1Range;
    }

    public dtoRange getT2Range() {
        return t2Range;
    }

    public void setT2Range(dtoRange t2Range) {
        this.t2Range = t2Range;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }
}
