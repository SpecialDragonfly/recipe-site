package orme.dominic.recipe.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import orme.dominic.recipe.ConfigUtils;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        final long start = System.currentTimeMillis();

        deployMigrationVerticle(vertx)
            .flatMap(migrationVerticleId -> deployApiVerticle(vertx))
            .onSuccess(success -> System.out.println("System started in " + (System.currentTimeMillis() - start) + " ms "))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
            //.onSuccess(success -> LOGGER.info(LogUtils.RUN_APP_SUCCESSFULLY_MESSAGE.buildMessage(System.currentTimeMillis() - start)))
            //.onFailure(throwable -> LOGGER.error(throwable.getMessage()));
    }

    private Future<Void> deployMigrationVerticle(Vertx vertx) {
        final DeploymentOptions options = new DeploymentOptions()
            .setWorker(true)
            .setWorkerPoolName("migrations-worker-pool")
            .setInstances(1)
            .setWorkerPoolSize(1);

        return vertx.deployVerticle(MigrationVerticle.class.getName(), options)
            .flatMap(vertx::undeploy);
    }

    private Future<String> deployApiVerticle(Vertx vertx) {
        return vertx.deployVerticle(ApiVerticle.class.getName(),
            new DeploymentOptions().setInstances(ConfigUtils.getInstance().numberOfAvailableCores()));
    }
}
