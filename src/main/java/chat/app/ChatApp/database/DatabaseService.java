package chat.app.ChatApp.database;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ProxyGen
public interface DatabaseService {
  @Fluent
  DatabaseService fetchMessages(Handler<AsyncResult<JsonArray>> resultHandler);
  @Fluent
  DatabaseService createMessage(String message, Handler<AsyncResult<Void>> resultHandler);
}
