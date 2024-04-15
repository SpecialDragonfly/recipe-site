package orme.dominic.recipe.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient implements Serializable {

    @Getter
    private Integer id;
    @Getter
    private String name;

    public Ingredient withId(int id) {
        return new Ingredient(id, this.name);
    }
}
