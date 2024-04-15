package orme.dominic.recipe.router;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import orme.dominic.recipe.handler.IngredientHandler;

public class IngredientRouter {
    private final Vertx vertx;
    private final IngredientHandler ingredientHandler;

    public IngredientRouter(Vertx vertx, IngredientHandler bookHandler) {
        this.vertx = vertx;
        this.ingredientHandler = bookHandler;
    }

    public void setRouter(Router router) {
        router.mountSubRouter("/api/v1", buildRecipeRouter());
    }

    private Router buildRecipeRouter() {
        final Router ingredientRouter = Router.router(vertx);
        ingredientRouter.route("/ingredients*").handler(BodyHandler.create());
        ingredientRouter.get("/ingredients").handler(LoggerHandler.create(LoggerFormat.DEFAULT)).handler(ingredientHandler::getAll);
        ingredientRouter.post("/ingredients").handler(LoggerHandler.create(LoggerFormat.DEFAULT)).handler(ingredientHandler::create);
        ingredientRouter.put("/ingredients/:id").handler(LoggerHandler.create(LoggerFormat.DEFAULT)).handler(ingredientHandler::update);
        ingredientRouter.delete("/ingredients/:id").handler(LoggerHandler.create(LoggerFormat.DEFAULT)).handler(ingredientHandler::delete);

        return ingredientRouter;
    }
}
