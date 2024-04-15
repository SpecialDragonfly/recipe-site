package orme.dominic.recipe.router;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import orme.dominic.recipe.handler.RecipeHandler;

public class RecipeRouter {
    private final Vertx vertx;
    private final RecipeHandler recipeHandler;

    public RecipeRouter(Vertx vertx, RecipeHandler recipeHandler) {
        this.vertx = vertx;
        this.recipeHandler = recipeHandler;
    }

    public void setRouter(Router router) {
        router.mountSubRouter("/api/v1", buildRecipeRouter());
    }

    private Router buildRecipeRouter() {
        final Router recipeRouter = Router.router(vertx);
        recipeRouter.route("/recipes*").handler(BodyHandler.create());
        recipeRouter.get("/recipes").handler(LoggerHandler.create(LoggerFormat.DEFAULT)).handler(recipeHandler::getAll);
        recipeRouter.get("/recipes/:id").handler(LoggerHandler.create(LoggerFormat.DEFAULT)).handler(recipeHandler::getOne);
        recipeRouter.post("/recipes").handler(LoggerHandler.create(LoggerFormat.DEFAULT)).handler(recipeHandler::create);
        recipeRouter.put("/recipes/:id").handler(LoggerHandler.create(LoggerFormat.DEFAULT)).handler(recipeHandler::update);
        recipeRouter.delete("/recipes/:id").handler(LoggerHandler.create(LoggerFormat.DEFAULT)).handler(recipeHandler::delete);
        recipeRouter.post("/recipes/:id/add-ingredient").handler(LoggerHandler.create(LoggerFormat.DEFAULT)).handler(recipeHandler::addIngredient);

        return recipeRouter;
    }
}
