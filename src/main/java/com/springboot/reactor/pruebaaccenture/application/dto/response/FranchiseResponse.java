package com.springboot.reactor.pruebaaccenture.application.dto.response;

import java.util.List;

public record FranchiseResponse(String id, String name, List<BranchResponse> branches) {}