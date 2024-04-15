package orme.dominic.recipe.models;

public record Book(String id, String title) {
    public Book withId(String id) {
        return new Book(id, this.title());
    }
}
