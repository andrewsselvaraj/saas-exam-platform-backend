package com.saasexam.common;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String entity, String id) {
        super(entity + " not found: " + id);
    }
}
