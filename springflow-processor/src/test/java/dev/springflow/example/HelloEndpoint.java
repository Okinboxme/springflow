package dev.springflow.example;

import dev.springflow.core.annotation.Endpoint;
import dev.springflow.core.annotation.EndpointMethod;

@Endpoint
public class HelloEndpoint {

    @EndpointMethod
    public String hello(String name) {
        return "Hello " + name;
    }

    @EndpointMethod
    public int add(int a, int b) {
        return a + b;
    }

}