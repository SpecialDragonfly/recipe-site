package orme.dominic.recipe.models;

import java.util.List;

public record RecipeWithIngredients(String recipeId, String title, String location, List<Ingredient> ingredients) {
}
