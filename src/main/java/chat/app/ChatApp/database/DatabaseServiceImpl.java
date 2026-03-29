package chat.app.ChatApp.database;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.stream.Collectors;

public class DatabaseServiceImpl implements DatabaseService {

  private final SqlClient dbClient;
  private final HashMap<SqlQuery, String> sqlQueries;

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseServiceImpl.class);

  public DatabaseServiceImpl(SqlClient dbClient,
                             HashMap<SqlQuery, String> sqlQueries,
                             Handler<AsyncResult<DatabaseService>> readyHandler) {

    this.dbClient = dbClient;
    this.sqlQueries = sqlQueries;

    dbClient
      .query(sqlQueries.get(SqlQuery.CREATE_MESSAGES_TABLE))
      .execute()
      .onComplete(ar -> {
        if (ar.succeeded()) {
          readyHandler.handle(Future.succeededFuture(this));
        } else {
          LOGGER.error("Database preparation error", ar.cause());
          readyHandler.handle(Future.failedFuture(ar.cause()));
        }

      });

  }

  @Override
  public DatabaseService getLastMessages(Handler<AsyncResult<JsonArray>> resultHandler) {
    dbClient
      .query(sqlQueries.get(SqlQuery.ALL_MESSAGES))
      .execute()
      .onComplete(ar -> {
        if (ar.succeeded()) {
          JsonArray messages = new JsonArray(
            ar.result().stream()
              .map(row -> new JsonObject()
                .put("sender", row.getString("sender"))
                .put("content", row.getString("content"))
              )
              .collect(Collectors.toList())
          );
          resultHandler.handle(Future.succeededFuture(messages));
        } else {
          LOGGER.error("Database query error", ar.cause());
          resultHandler.handle(Future.failedFuture(ar.cause()));
        }
      });
    return this;
  }

  @Override
  public DatabaseService addMessage(JsonObject message, Handler<AsyncResult<Void>> resultHandler) {
    String sender = message.getString("sender");
    String content = message.getString("content");

    dbClient
      .preparedQuery(sqlQueries.get(SqlQuery.CREATE_MESSAGE))
      .execute(Tuple.of(sender, content))
      .onComplete(ar -> {
        if (ar.succeeded()) {
          resultHandler.handle(Future.succeededFuture());
        } else {
          LOGGER.error("Database query error", ar.cause());
          resultHandler.handle(Future.failedFuture(ar.cause()));
        }
      });
    return this;
  }

}
