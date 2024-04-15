package orme.dominic.recipe.services;

import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import orme.dominic.recipe.QueryUtils;
import orme.dominic.recipe.models.*;
import orme.dominic.recipe.models.response.IngredientGetAllResponse;
import orme.dominic.recipe.repository.IngredientRepository;

import java.util.List;
import java.util.stream.Collectors;

public class IngredientService {
    private final PgPool dbClient;
    private final IngredientRepository ingredientRepository;

    public IngredientService(PgPool dbClient, IngredientRepository ingredientRepository) {

        this.dbClient = dbClient;
        this.ingredientRepository = ingredientRepository;
    }

    public Future<IngredientGetAllResponse> getAll(String p, String l) {
        return dbClient.withTransaction(
            connection -> {
                final int page = QueryUtils.getPage(p);
                final int limit = QueryUtils.getLimit(l);
                final int offset = QueryUtils.getOffset(page, limit);

                return ingredientRepository.count(connection)
                    .flatMap(total ->
                        ingredientRepository.selectAll(connection, limit, offset)
                            .map(ingredients -> new IngredientGetAllResponse(total, limit, page, ingredients))
                    );
            })
            .onSuccess(success -> System.out.println(success.ingredients()))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
            //.onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Read all books", success.getBooks())))
            //.onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Read all books", throwable.getMessage())));

    }

    public Future<Ingredient> create(Ingredient ingredient) {
        return dbClient.withTransaction(connection -> ingredientRepository.insert(connection, ingredient))
            .onSuccess(success -> System.out.println("Create one recipe " + ingredient.getId()))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Create one book", success)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Create one book", throwable.getMessage())));
    }

    public Future<Ingredient> update(int id, Ingredient ingredient) {
        return dbClient.withTransaction(connection -> ingredientRepository.update(connection, ingredient.withId(id)))
            .onSuccess(success -> System.out.println("Update one recipe " + id))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Update one book", success)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Update one book", throwable.getMessage())));
    }

    public Future<Void> delete(int id) {
        return dbClient.withTransaction(connection -> ingredientRepository.delete(connection, id))
            .onSuccess(success -> System.out.println("Delete one recipe " + id))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Delete one book", id)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Delete one book", throwable.getMessage())));
    }

    public Future<Void> addIngredient(String recipeId, String ingredient) {
        return dbClient.withTransaction(connection -> ingredientRepository.addIngredient(connection, recipeId, ingredient))
            .onSuccess(success -> System.out.println("Added ingredient"))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
    }
}
