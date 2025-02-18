package ru.antonov.securitytest.exception;

public class SessionOverlappingEx extends Exception{
    public SessionOverlappingEx(String msg) {
        super(msg);
    }
}
