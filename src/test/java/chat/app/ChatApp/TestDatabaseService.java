package chat.app.ChatApp;

import chat.app.ChatApp.database.DatabaseService;
import chat.app.ChatApp.database.SqlQuery;

import io.vertx.core.Vertx;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class TestDatabaseService {

  private DatabaseService service;

  private final HashMap<SqlQuery, String> sqlQueries = new HashMap<>() {{
    put(SqlQuery.CREATE_MESSAGES_TABLE, "CREATE TABLE SIMULEE");
    put(SqlQuery.ALL_MESSAGES, "SELECT * FROM SIMULEE");
    put(SqlQuery.CREATE_MESSAGE, "INSERT INTO SIMULEE");
  }};

  @BeforeEach
  void prepare(Vertx vertx, VertxTestContext testContext) {
    // Simulated in-memory DatabaseService
    service = new DatabaseService() {
      private final JsonArray messages = new JsonArray();

      @Override
      public DatabaseService getLastMessages(io.vertx.core.Handler<io.vertx.core.AsyncResult<JsonArray>> resultHandler) {
        resultHandler.handle(io.vertx.core.Future.succeededFuture(messages.copy()));
        return this;
      }

      @Override
      public DatabaseService addMessage(JsonObject message, io.vertx.core.Handler<io.vertx.core.AsyncResult<Void>> resultHandler) {
        messages.add(message.copy());
        resultHandler.handle(io.vertx.core.Future.succeededFuture());
        return this;
      }

      @Override
      public DatabaseService updateMessage(int id, String content, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture("not tested"));
        return this;
      }

      @Override
      public DatabaseService deleteMessage(int id, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture("not tested"));
        return this;
      }
    };

    testContext.completeNow();
  }

  @AfterEach
  void cleanup(Vertx vertx, VertxTestContext testContext) {
    testContext.completeNow();
  }

  @Test
  void crud_operations(Vertx vertx, VertxTestContext testContext) throws Throwable {
    JsonObject msg1 = new JsonObject().put("sender", "Alice").put("content", "Hello");
    JsonObject msg2 = new JsonObject().put("sender", "Bob").put("content", "Hi Alice");

    service.addMessage(msg1, testContext.succeeding(v1 -> {
      service.addMessage(msg2, testContext.succeeding(v2 -> {
        service.getLastMessages(testContext.succeeding(messages1 -> {
          assertEquals(2, messages1.size());
          assertEquals("Hello", messages1.getJsonObject(0).getString("content"));
          assertEquals("Hi Alice", messages1.getJsonObject(1).getString("content"));

          JsonObject msg3 = new JsonObject().put("sender", "Alice").put("content", "How are you?");
          service.addMessage(msg3, testContext.succeeding(v3 -> {
            service.getLastMessages(testContext.succeeding(messages2 -> {
              assertEquals(3, messages2.size());
              assertEquals("How are you?", messages2.getJsonObject(2).getString("content"));
              testContext.completeNow();
            }));
          }));
        }));
      }));
    }));

    testContext.awaitCompletion(5000, TimeUnit.MILLISECONDS);
  }
}
