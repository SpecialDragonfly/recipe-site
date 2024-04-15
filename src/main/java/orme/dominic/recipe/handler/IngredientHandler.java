package orme.dominic.recipe.handler;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import orme.dominic.recipe.ResponseUtils;
import orme.dominic.recipe.models.*;
import orme.dominic.recipe.models.response.IngredientGetAllResponse;
import orme.dominic.recipe.services.IngredientService;

public class IngredientHandler {

    private static final String PAGE_PARAMETER = "page";
    private static final String LIMIT_PARAMETER = "limit";
    private final IngredientService ingredientService;

    public IngredientHandler(IngredientService ingredientService) {

        this.ingredientService = ingredientService;
    }

    public Future<IngredientGetAllResponse> getAll(RoutingContext rc) {
        final String page = rc.queryParams().get(PAGE_PARAMETER);
        final String limit = rc.queryParams().get(LIMIT_PARAMETER);

        return this.ingredientService.getAll(page, limit)
            .onSuccess(success -> ResponseUtils.buildOkResponse(rc, success))
            .onFailure(throwable -> ResponseUtils.buildErrorResponse(rc, throwable));
    }

    public Future<Ingredient> create(RoutingContext routingContext) {
        Ingredient ingredient = routingContext.body().asJsonObject().mapTo(Ingredient.class);

        return ingredientService.create(ingredient)
            .onSuccess(success -> ResponseUtils.buildCreatedResponse(routingContext, success))
            .onFailure(throwable -> ResponseUtils.buildErrorResponse(routingContext, throwable));
    }

    public Future<Ingredient> update(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.pathParam("id"));
        Ingredient ingredient = routingContext.body().asJsonObject().mapTo(Ingredient.class);

        return ingredientService.update(id, ingredient)
            .onSuccess(success -> ResponseUtils.buildOkResponse(routingContext, success))
            .onFailure(throwable -> ResponseUtils.buildErrorResponse(routingContext, throwable));
    }

    public Future<Void> delete(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.pathParam("id"));

        return ingredientService.delete(id)
            .onSuccess(success -> ResponseUtils.buildNoContentResponse(routingContext))
            .onFailure(throwable -> ResponseUtils.buildErrorResponse(routingContext, throwable));
    }
}
