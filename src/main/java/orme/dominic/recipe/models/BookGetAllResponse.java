package orme.dominic.recipe.models;

import java.util.List;

public record BookGetAllResponse(
    int total,
    int limit,
    int page,
    List<BookGetOneResponse> books) {
}
