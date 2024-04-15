package orme.dominic.recipe.services;

import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import orme.dominic.recipe.QueryUtils;
import orme.dominic.recipe.models.*;
import orme.dominic.recipe.models.response.RecipeGetAllResponse;
import orme.dominic.recipe.models.response.RecipeGetOneThinResponse;
import orme.dominic.recipe.repository.RecipeRepository;

import java.util.List;
import java.util.stream.Collectors;

public class RecipeService {
    private final PgPool dbClient;
    private final RecipeRepository recipeRepository;

    public RecipeService(PgPool dbClient, RecipeRepository bookRepository) {

        this.dbClient = dbClient;
        this.recipeRepository = bookRepository;
    }

    public Future<RecipeGetAllResponse> getAll(String p, String l) {
        return dbClient.withTransaction(
                connection -> {
                    final int page = QueryUtils.getPage(p);
                    final int limit = QueryUtils.getLimit(l);
                    final int offset = QueryUtils.getOffset(page, limit);

                    return recipeRepository.count(connection)
                        .flatMap(total ->
                            recipeRepository.selectAll(connection, limit, offset)
                                .map(result -> {
                                    final List<RecipeGetOneThinResponse> recipes = result.stream()
                                        .map(b -> new RecipeGetOneThinResponse(b.id(), b.title()))
                                        .collect(Collectors.toList());

                                    return new RecipeGetAllResponse(total, limit, page, recipes);
                                })
                        );
                })
            .onSuccess(success -> System.out.println(success.recipes()))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
            //.onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Read all books", success.getBooks())))
            //.onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Read all books", throwable.getMessage())));

    }

    public Future<RecipeWithIngredients> getOne(String id) {
        return dbClient.withTransaction(connection -> recipeRepository.selectById(connection, id))
            .onSuccess(success -> System.out.println("Get one recipe " + id))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Read one book", success)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Read one book", throwable.getMessage())));
    }

    public Future<Recipe> create(Recipe book) {
        return dbClient.withTransaction(connection -> recipeRepository.insert(connection, book))
            .onSuccess(success -> System.out.println("Create one recipe " + book.id()))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Create one book", success)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Create one book", throwable.getMessage())));
    }

    public Future<Recipe> update(String id, Recipe book) {
        return dbClient.withTransaction(connection -> recipeRepository.update(connection, book.withId(id)))
            .onSuccess(success -> System.out.println("Update one recipe " + id))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Update one book", success)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Update one book", throwable.getMessage())));
    }

    public Future<Void> delete(String id) {
        return dbClient.withTransaction(connection -> recipeRepository.delete(connection, id))
            .onSuccess(success -> System.out.println("Delete one recipe " + id))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Delete one book", id)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Delete one book", throwable.getMessage())));
    }
}
