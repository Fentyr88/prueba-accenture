package com.springboot.reactor.pruebaaccenture.domain.exception;

public class BranchNotFoundException extends DomainException {

    public BranchNotFoundException(String id) {
        super("Branch not found: " + id);
    }
}

