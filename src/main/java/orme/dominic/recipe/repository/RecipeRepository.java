package orme.dominic.recipe.repository;

import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.templates.RowMapper;
import io.vertx.sqlclient.templates.SqlTemplate;
import orme.dominic.recipe.models.Ingredient;
import orme.dominic.recipe.models.Recipe;
import orme.dominic.recipe.models.RecipeWithIngredients;
import orme.dominic.recipe.models.sql.RecipeWithIngredientsQuery;

import java.util.*;
import java.util.stream.Collectors;

public class RecipeRepository {

    private static final String SQL_SELECT_ALL = "SELECT * FROM recipes LIMIT #{limit} OFFSET #{offset}";
    private static final String SQL_SELECT_WITH_INGREDIENTS_ID = "SELECT r.id as recipe_id, r.title, r.location, i.id as ingredient_id, i.name as ingredient_name FROM recipes r LEFT JOIN recipe_ingredients ri ON r.id = ri.recipe_id LEFT JOIN ingredients i ON ri.ingredient_id = i.id WHERE r.id = #{id}";
    private static final String SQL_INSERT = "INSERT INTO recipes (id, title) VALUES (#{id}, #{title}) RETURNING id";
    private static final String SQL_UPDATE = "UPDATE recipes SET title = #{title} WHERE id = #{id}";
    private static final String SQL_DELETE = "DELETE FROM recipes WHERE id = #{id}";
    private static final String SQL_COUNT = "SELECT COUNT(*) AS total FROM recipes";

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

    public Future<List<Recipe>> selectAll(SqlConnection connection, int limit, int offset) {
        Map<String,Object> params = new HashMap<>();
        params.put("limit",limit);
        params.put("offset",offset);
        return SqlTemplate
            .forQuery(connection, SQL_SELECT_ALL)
            .mapTo(Recipe.class)
            .execute(params)
            .map(rowSet -> {
                final List<Recipe> books = new ArrayList<>();
                rowSet.forEach(books::add);

                return books;
            })
            .onSuccess(success -> System.out.println(SQL_SELECT_ALL))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Read all books", SQL_SELECT_ALL)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Read all books", throwable.getMessage())));
    }

    public Future<RecipeWithIngredients> selectById(SqlConnection connection, String id) {
        return SqlTemplate
            .forQuery(connection, SQL_SELECT_WITH_INGREDIENTS_ID)
            .mapTo(rowmapper -> new RecipeWithIngredientsQuery(
                rowmapper.getString("recipe_id"),
                rowmapper.getString("title"),
                rowmapper.getString("location"),
                rowmapper.getValue("ingredient_id") == null ? 0 : rowmapper.getInteger("ingredient_id"),
                rowmapper.getString("ingredient_name")
            ))
            .execute(Collections.singletonMap("id", id))
            .map(rowSet -> {
                Map<String, List<RecipeWithIngredientsQuery>> groupedRows = new HashMap<>();
                rowSet.forEach(row -> {
                    String recipeId = row.recipeId();
                    groupedRows.computeIfAbsent(recipeId, k -> new ArrayList<>()).add(row);
                });

                if (groupedRows.containsKey(id)) {
                    List<Ingredient> ingredients = groupedRows.get(id).stream().filter(r -> r.ingredientId() != 0)
                        .map(row -> new Ingredient(row.ingredientId(), row.ingredientName()))
                        .collect(Collectors.toList());

                    RecipeWithIngredientsQuery recipe = rowSet.iterator().next();
                    return new RecipeWithIngredients(recipe.recipeId(), recipe.title(), "", ingredients);
                } else {
                    throw new NoSuchElementException(id);
                }
            })
            .onSuccess(success -> System.out.println(SQL_SELECT_WITH_INGREDIENTS_ID))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Read book by id", SQL_SELECT_BY_ID)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Read book by id", throwable.getMessage())));
    }

    public Future<Recipe> insert(SqlConnection connection, Recipe book) {
        return SqlTemplate
            .forUpdate(connection, SQL_INSERT)
            .mapFrom(Recipe.class)
            .execute(book)
            .flatMap(rowSet -> {
                if (rowSet.rowCount() > 0) {
                    return Future.succeededFuture(book);
                } else {
                    throw new NoSuchElementException("Id: " + book.id());
                    //throw new NoSuchElementException(LogUtils.NO_BOOK_WITH_ID_MESSAGE.buildMessage(book.getId()));
                }
            })
            .onSuccess(success -> System.out.println(SQL_INSERT))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Update book", SQL_UPDATE)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Update book", throwable.getMessage())));
    }

    public Future<Recipe> update(SqlConnection connection, Recipe book) {
        return SqlTemplate
            .forUpdate(connection, SQL_UPDATE)
            .mapFrom(Recipe.class)
            .execute(book)
            .flatMap(rowSet -> {
                if (rowSet.rowCount() > 0) {
                    return Future.succeededFuture(book);
                } else {
                    throw new NoSuchElementException("Id: " + book.id());
                    //throw new NoSuchElementException(LogUtils.NO_BOOK_WITH_ID_MESSAGE.buildMessage(book.id()));
                }
            })
            .onSuccess(success -> System.out.println(SQL_UPDATE))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Update book", SQL_UPDATE)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Update book", throwable.getMessage())));
    }

    public Future<Void> delete(SqlConnection connection, String id) {
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
}
