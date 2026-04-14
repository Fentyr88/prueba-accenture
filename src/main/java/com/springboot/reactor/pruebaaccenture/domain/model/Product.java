package com.springboot.reactor.pruebaaccenture.domain.model;

import com.springboot.reactor.pruebaaccenture.domain.vo.Id;
import com.springboot.reactor.pruebaaccenture.domain.vo.Name;
import com.springboot.reactor.pruebaaccenture.domain.vo.Stock;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Product {

    private final Id id;
    private Name name;
    private Stock stock;


    public void updateName(Name name) {
        this.name = name;
    }

    public void updateStock(Stock stock) {
        this.stock = stock;
    }
}
