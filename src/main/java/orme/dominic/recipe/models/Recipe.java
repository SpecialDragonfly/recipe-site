package orme.dominic.recipe.models;

public record Recipe(String id, String title, String location) {
    public Recipe withId(String id) {
        return new Recipe(id, this.title(), this.location());
    }
}
