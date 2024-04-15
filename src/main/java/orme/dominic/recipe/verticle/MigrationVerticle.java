package orme.dominic.recipe.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.Configuration;
import orme.dominic.recipe.ConfigUtils;

public class MigrationVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> promise) {
        final Configuration config = ConfigUtils.getInstance().buildMigrationsConfiguration();
        final Flyway flyway = new Flyway(config);

        flyway.migrate();

        promise.complete();
    }

}
