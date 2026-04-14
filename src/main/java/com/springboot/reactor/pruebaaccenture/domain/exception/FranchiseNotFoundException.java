package com.springboot.reactor.pruebaaccenture.domain.exception;

public class FranchiseNotFoundException extends DomainException {

    public FranchiseNotFoundException(String id) {
        super("Franchise not found: " + id);
    }
}

