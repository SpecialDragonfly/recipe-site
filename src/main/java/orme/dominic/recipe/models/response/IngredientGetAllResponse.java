package orme.dominic.recipe.models.response;

import orme.dominic.recipe.models.Ingredient;

import java.util.List;

public record IngredientGetAllResponse(
    int total,
    int limit,
    int page,
    List<Ingredient> ingredients) {
}
