package orme.dominic.recipe.models.sql;

public record RecipeWithIngredientsQuery(String recipeId, String title, String location, int ingredientId, String ingredientName) {
}
