package ru.hcc.customchestloot.util;


public enum FileName {

    CONFIG("config.json"),
    REGIONS_DATA("data.json"),
    TIMER_DATA("timer.txt");

    private final String fileName;

    FileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
