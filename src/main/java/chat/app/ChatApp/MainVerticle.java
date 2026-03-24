package chat.app.ChatApp;

import chat.app.ChatApp.database.DatabaseVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Future<String> dbVerticleDeployment = vertx.deployVerticle(new DatabaseVerticle());
  }
}
