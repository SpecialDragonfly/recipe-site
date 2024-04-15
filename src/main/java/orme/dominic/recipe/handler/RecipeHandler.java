package orme.dominic.recipe.handler;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import orme.dominic.recipe.ResponseUtils;
import orme.dominic.recipe.models.*;
import orme.dominic.recipe.models.response.RecipeGetAllResponse;
import orme.dominic.recipe.models.response.RecipeGetOneResponse;
import orme.dominic.recipe.models.response.RecipeGetOneThinResponse;
import orme.dominic.recipe.services.IngredientService;
import orme.dominic.recipe.services.RecipeService;

import java.util.UUID;

public class RecipeHandler {

    private static final String PAGE_PARAMETER = "page";
    private static final String LIMIT_PARAMETER = "limit";
    private final RecipeService recipeService;
    private final IngredientService ingredientService;

    public RecipeHandler(RecipeService recipeService, IngredientService ingredientService) {

        this.recipeService = recipeService;
        this.ingredientService = ingredientService;
    }

    public Future<RecipeGetAllResponse> getAll(RoutingContext rc) {
        final String page = rc.queryParams().get(PAGE_PARAMETER);
        final String limit = rc.queryParams().get(LIMIT_PARAMETER);

        return this.recipeService.getAll(page, limit)
            .onSuccess(success -> ResponseUtils.buildOkResponse(rc, success))
            .onFailure(throwable -> ResponseUtils.buildErrorResponse(rc, throwable));
    }

    public Future<RecipeGetOneResponse> getOne(RoutingContext routingContext) {
        String id = routingContext.pathParam("id");
        return this.recipeService.getOne(id)
            .map(r -> new RecipeGetOneResponse(r.recipeId(), r.title(), r.ingredients()))
            .onSuccess(success -> ResponseUtils.buildOkResponse(routingContext, success))
            .onFailure(throwable ->  ResponseUtils.buildErrorResponse(routingContext, throwable));
    }

    public Future<RecipeGetOneThinResponse> create(RoutingContext routingContext) {
        UUID uuid = UUID.randomUUID();
        JsonObject body = routingContext.body().asJsonObject();
        Recipe book = new Recipe(uuid.toString(), body.getString("title"), body.getString("location"));

        return recipeService.create(book).map(r -> new RecipeGetOneThinResponse(r.id(), r.title()))
            .onSuccess(success -> ResponseUtils.buildCreatedResponse(routingContext, success))
            .onFailure(throwable -> ResponseUtils.buildErrorResponse(routingContext, throwable));
    }

    public Future<RecipeGetOneThinResponse> update(RoutingContext routingContext) {
        String id = routingContext.pathParam("id");
        Recipe recipe = routingContext.body().asJsonObject().mapTo(Recipe.class);

        return recipeService.update(id, recipe).map(r -> new RecipeGetOneThinResponse(r.id(), r.title()))
            .onSuccess(success -> ResponseUtils.buildOkResponse(routingContext, success))
            .onFailure(throwable -> ResponseUtils.buildErrorResponse(routingContext, throwable));
    }

    public Future<Void> delete(RoutingContext routingContext) {
        String id = routingContext.pathParam("id");

        return recipeService.delete(id)
            .onSuccess(success -> ResponseUtils.buildNoContentResponse(routingContext))
            .onFailure(throwable -> ResponseUtils.buildErrorResponse(routingContext, throwable));
    }

    public Future<Void> addIngredient(RoutingContext routingContext) {
        String recipeId = routingContext.pathParam("id");
        String ingredient = routingContext.body().asJsonObject().getString("ingredient");

        return ingredientService.addIngredient(recipeId, ingredient)
            .onSuccess(success -> ResponseUtils.buildNoContentResponse(routingContext))
            .onFailure(throwable -> ResponseUtils.buildErrorResponse(routingContext, throwable));
    }
}
