package orme.dominic.recipe.models.response;

import java.util.List;

public record RecipeGetAllResponse(
    int total,
    int limit,
    int page,
    List<RecipeGetOneThinResponse> recipes) {
}
