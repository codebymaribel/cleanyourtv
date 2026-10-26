package com.gathin.tvhome;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Sample titles for building the carousel before the companion app exists. The ids are real IMDb ids, so OK opens
 * the real title in Stremio. No posters: the cards draw their placeholder.
 */
final class FakeCatalogSource implements CatalogSource {

    @NonNull
    @Override
    public List<CatalogItem> load(int tab) {
        switch (tab) {
            case NavTabs.POPULAR:
                return Arrays.asList(
                        movie("tt6751668", "Parasite", "Una familia sin trabajo se infiltra, uno por uno, en la casa de una familia rica."),
                        movie("tt0166924", "Mulholland Drive", "Una aspirante a actriz y una mujer sin memoria buscan quién es ella en un Los Ángeles de sueño."),
                        movie("tt0118694", "In the Mood for Love", "Dos vecinos descubren que sus parejas los engañan y se acercan sin cruzar la línea."),
                        movie("tt0245429", "Spirited Away", "Una niña queda atrapada en un mundo de espíritus y trabaja en una casa de baños para salvar a sus padres."),
                        movie("tt6710474", "Everything Everywhere All at Once", "Una dueña de lavandería descubre que puede saltar entre versiones de su propia vida."),
                        movie("tt13238346", "Past Lives", "Dos amigos de la infancia se reencuentran veinte años después, en otra ciudad y otra vida."));
            case NavTabs.WATCHLIST:
                return Arrays.asList(
                        movie("tt0110148", "Interview with the Vampire", "Un vampiro cuenta a un periodista doscientos años de culpa junto a quien lo convirtió."),
                        movie("tt0368447", "The Village", "Un pueblo aislado vive con miedo de las criaturas que habitan el bosque que lo rodea."),
                        movie("tt13651794", "May December", "Una actriz estudia a la mujer que va a interpretar, cuyo escándalo de hace años sigue sin cerrar."),
                        movie("tt0947798", "Black Swan", "Una bailarina obsesionada con el papel del cisne negro empieza a perder el límite con la realidad."),
                        movie("tt1663202", "The Revenant", "Un trampero dado por muerto cruza el invierno salvaje para vengarse de quien lo abandonó."));
            case NavTabs.FRIENDS:
                return Arrays.asList(
                        movie("tt0091064", "The Fly", "Un científico prueba en sí mismo su máquina de teletransporte y algo más viaja con él."),
                        movie("tt1783732", "John Dies at the End", "Dos amigos prueban una droga que les abre la puerta a otras dimensiones, y a lo que vive en ellas."),
                        movie("tt0061138", "A Man and a Woman", "Un viudo y una viuda se conocen en el internado de sus hijos y no pueden dejar de pensarse."));
            default:
                return Collections.emptyList();
        }
    }

    private static CatalogItem movie(String id, String title, String summary) {
        return new CatalogItem("movie", id, title, summary, null);
    }
}
