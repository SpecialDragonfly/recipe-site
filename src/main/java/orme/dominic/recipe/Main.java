package orme.dominic.recipe;

import io.vertx.core.Vertx;
import orme.dominic.recipe.verticle.MainVerticle;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(MainVerticle.class.getName())
            .onFailure(throwable -> System.exit(-1))
            .onSuccess(res -> System.out.println("Success"));
    }
}
