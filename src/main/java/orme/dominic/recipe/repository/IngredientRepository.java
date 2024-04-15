package orme.dominic.recipe.repository;

import io.vertx.core.Future;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.templates.RowMapper;
import io.vertx.sqlclient.templates.SqlTemplate;
import orme.dominic.recipe.models.Ingredient;

import java.util.*;

public class IngredientRepository {

    private static final String SQL_SELECT_ALL = "SELECT * FROM ingredients LIMIT #{limit} OFFSET #{offset}";
    private static final String SQL_INSERT = "INSERT INTO ingredients (name) VALUES (#{name}) RETURNING id";
    private static final String SQL_UPDATE = "UPDATE ingredients SET name = #{name} WHERE id = #{id}";
    private static final String SQL_DELETE = "DELETE FROM ingredients WHERE id = #{id}";
    private static final String SQL_COUNT = "SELECT COUNT(*) AS total FROM ingredients";
    private static final String SQL_ADD_TO_PIVOT_TABLE = "INSERT INTO recipe_ingredients (recipe_id, ingredient_id) VALUES (#{recipe_id}, #{ingredient_id})";
    private static final String SQL_GET_ONE = "SELECT id FROM ingredients WHERE name = #{name}";
    private static final String SQL_RECIPE_INGREDIENT_EXISTS = "SELECT COUNT(*) AS count FROM recipe_ingredients WHERE recipe_id = #{recipe_id} AND ingredient_id = #{ingredient_id}";

    public Future<Integer> count(SqlConnection connection) {
        final RowMapper<Integer> ROW_MAPPER = row -> row.getInteger("total");

        return SqlTemplate
            .forQuery(connection, SQL_COUNT)
            .mapTo(ROW_MAPPER)
            .execute(Collections.emptyMap())
            .map(rowSet -> rowSet.iterator().next())
            .onSuccess(success -> System.out.println(SQL_COUNT))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Count books", SQL_COUNT)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Count book", throwable.getMessage())));
    }

    public Future<List<Ingredient>> selectAll(SqlConnection connection, int limit, int offset) {
        Map<String,Object> params = new HashMap<>();
        params.put("limit",limit);
        params.put("offset",offset);
        return SqlTemplate
            .forQuery(connection, SQL_SELECT_ALL)
            .mapTo(Ingredient.class)
            .execute(params)
            .map(rowSet -> {
                final List<Ingredient> ingredients = new ArrayList<>();
                rowSet.forEach(ingredients::add);

                return ingredients;
            })
            .onSuccess(success -> System.out.println(SQL_SELECT_ALL))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Read all books", SQL_SELECT_ALL)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Read all books", throwable.getMessage())));
    }

    public Future<Ingredient> insert(SqlConnection connection, Ingredient ingredient) {
        return SqlTemplate
            .forUpdate(connection, SQL_INSERT)
            .mapFrom(Ingredient.class)
            .execute(ingredient)
            .flatMap(rowSet -> {
                if (rowSet.rowCount() > 0) {
                    return Future.succeededFuture(ingredient);
                } else {
                    throw new NoSuchElementException("Id: " + ingredient.getId());
                    //throw new NoSuchElementException(LogUtils.NO_BOOK_WITH_ID_MESSAGE.buildMessage(book.getId()));
                }
            })
            .onSuccess(success -> System.out.println(SQL_INSERT))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Update book", SQL_UPDATE)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Update book", throwable.getMessage())));
    }

    public Future<Ingredient> update(SqlConnection connection, Ingredient ingredient) {
        return SqlTemplate
            .forUpdate(connection, SQL_UPDATE)
            .mapFrom(Ingredient.class)
            .execute(ingredient)
            .flatMap(rowSet -> {
                if (rowSet.rowCount() > 0) {
                    return Future.succeededFuture(ingredient);
                } else {
                    throw new NoSuchElementException("Id: " + ingredient.getId());
                    //throw new NoSuchElementException(LogUtils.NO_BOOK_WITH_ID_MESSAGE.buildMessage(book.id()));
                }
            })
            .onSuccess(success -> System.out.println(SQL_UPDATE))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Update book", SQL_UPDATE)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Update book", throwable.getMessage())));
    }

    public Future<Void> delete(SqlConnection connection, int id) {
        return SqlTemplate
            .forUpdate(connection, SQL_DELETE)
            .execute(Collections.singletonMap("id", id))
            .flatMap(rowSet -> {
                if (rowSet.rowCount() > 0) {
                    //LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Delete book", SQL_DELETE));
                    return Future.succeededFuture();
                } else {
                    //LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Delete book", LogUtils.NO_BOOK_WITH_ID_MESSAGE.buildMessage(id)));
                    throw new NoSuchElementException("Id: " + id);
                    //throw new NoSuchElementException(LogUtils.NO_BOOK_WITH_ID_MESSAGE.buildMessage(id));
                }
            });
    }

    private Future<Integer> getOrCreateIngredientId(SqlConnection connection, String ingredientName) {
        // Check whether the ingredient exists in the table by its name.
        return SqlTemplate
            .forQuery(connection, SQL_GET_ONE)
            .execute(Collections.singletonMap("name", ingredientName))
            .compose(result -> {
                if (result.size() > 0) {
                    // If the ingredient exists, retrieve its ID.
                    Row row = result.iterator().next();
                    int ingredientId = row.getInteger("id");
                    return Future.succeededFuture(ingredientId);
                } else {
                    // If the ingredient does not exist, insert it into the ingredients table and retrieve its ID.
                    return SqlTemplate
                        .forQuery(connection, SQL_INSERT)
                        .execute(Collections.singletonMap("name", ingredientName))
                        .compose(insertResult -> {
                            Row insertRow = insertResult.iterator().next();
                            int ingredientId = insertRow.getInteger("id");
                            return Future.succeededFuture(ingredientId);
                        });
                }
            })
            .onSuccess(success -> System.out.println(success))
            .onFailure(error -> System.out.println(error.getMessage()));
    }

    private Future<Void> addRecipeIngredient(SqlConnection connection, String recipeId, int ingredientId) {
        SqlTemplate
            .forQuery(connection, SQL_RECIPE_INGREDIENT_EXISTS)
            .execute(Map.of("recipe_id", recipeId, "ingredient_id", ingredientId))
            .compose(result -> {
                // Add a record to the recipe_ingredients pivot table with the given recipe ID and the ingredient's ID.
                if (result.iterator().next().getInteger("count") == 0) {
                    return SqlTemplate
                        .forQuery(connection, SQL_ADD_TO_PIVOT_TABLE)
                        .execute(Map.of("recipe_id", recipeId, "ingredient_id", ingredientId))
                        .mapEmpty();
                }
                return Future.succeededFuture();
            })
            .onSuccess(success -> System.out.println(success))
            .onFailure(error -> System.out.println(error.getMessage()));
        return Future.succeededFuture();
    }

    public Future<Void> addIngredient(SqlConnection connection, String recipeId, String ingredient) {
        return this.getOrCreateIngredientId(connection, ingredient)
            .compose(ingredientId -> this.addRecipeIngredient(connection, recipeId, ingredientId))
            .onSuccess(success -> System.out.println(success))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
    }
}
