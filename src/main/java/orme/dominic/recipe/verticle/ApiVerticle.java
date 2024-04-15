package orme.dominic.recipe.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.pgclient.PgPool;
import orme.dominic.recipe.ConfigUtils;
import orme.dominic.recipe.handler.IngredientHandler;
import orme.dominic.recipe.handler.RecipeHandler;
import orme.dominic.recipe.repository.IngredientRepository;
import orme.dominic.recipe.repository.RecipeRepository;
import orme.dominic.recipe.router.IngredientRouter;
import orme.dominic.recipe.router.RecipeRouter;
import orme.dominic.recipe.services.IngredientService;
import orme.dominic.recipe.services.RecipeService;

public class ApiVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> promise) throws Exception {
        PgPool dbClient = ConfigUtils.getInstance().buildDbClient(vertx);

        IngredientRepository ingredientRepository = new IngredientRepository();
        IngredientService ingredientService = new IngredientService(dbClient, ingredientRepository);

        RecipeRepository recipeRepository = new RecipeRepository();
        RecipeService recipeService = new RecipeService(dbClient, recipeRepository);

        RecipeHandler recipeHandler = new RecipeHandler(recipeService, ingredientService);
        RecipeRouter recipeRouter = new RecipeRouter(vertx, recipeHandler);


        IngredientHandler ingredientHandler = new IngredientHandler(ingredientService);
        IngredientRouter ingredientRouter = new IngredientRouter(vertx, ingredientHandler);

        Router router = Router.router(vertx);
        recipeRouter.setRouter(router);
        ingredientRouter.setRouter(router);

        int port = ConfigUtils.getInstance().getServerPort();

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(port, http -> {
                if (http.succeeded()) {
                    promise.complete();
                    //LOGGER.info(LogUtils.RUN_HTTP_SERVER_SUCCESS_MESSAGE.buildMessage(port));
                } else {
                    promise.fail(http.cause());
                    System.out.println(http.cause().getMessage());
                    //LOGGER.info(LogUtils.RUN_HTTP_SERVER_ERROR_MESSAGE.buildMessage());
                }
            });
    }
}
