package com.blissroms.blissify.stats.models;

public class ServerRequest {

    private String operation;
    private StatsData stats;

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setStats(StatsData stats) {
        this.stats = stats;
    }

}
