package ru.hcc.customchestloot.util;


public enum Number {

    FIRST("first"),
    SECOND("second");

    private final String number;

    Number(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }
}
