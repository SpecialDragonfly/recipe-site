package orme.dominic.recipe.models.response;

import orme.dominic.recipe.models.Ingredient;

import java.util.List;

public record RecipeGetOneResponse(String id, String title, List<Ingredient> ingredientList) {
}
