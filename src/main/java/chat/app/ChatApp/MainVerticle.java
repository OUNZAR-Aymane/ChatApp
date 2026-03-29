package chat.app.ChatApp;

import chat.app.ChatApp.database.DatabaseVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startPromise) {
    Future<String> dbVerticleDeployment = vertx.deployVerticle(new
      DatabaseVerticle()); //(1)
    dbVerticleDeployment.compose(id ->
        vertx.deployVerticle("io.vertx.guides.wiki.HttpServerVerticle", //(2)
          new DeploymentOptions().setInstances(2))) //(3)
      .onComplete(ar -> { //(4)
        if (ar.succeeded()) {
          startPromise.complete();
        } else {
          startPromise.fail(ar.cause());
        }
      });
  }
}
