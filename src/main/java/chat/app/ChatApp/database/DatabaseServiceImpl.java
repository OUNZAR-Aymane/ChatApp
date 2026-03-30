package chat.app.ChatApp.database;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

public class DatabaseServiceImpl implements DatabaseService {

  private final SqlClient dbClient;
  private final HashMap<SqlQuery, String> sqlQueries;

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseServiceImpl.class);

  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
            .map(row -> {
              // 1. On crée le JSON de base
              JsonObject json = new JsonObject()
                .put("id", row.getInteger("id"))
                .put("sender", row.getString("sender"))
                .put("content", row.getString("content"))
                .put("created_at", row.getLocalDateTime("created_at").format(formatter));

              // 2. ON AJOUTE LA VÉRIFICATION ICI
              // Si updated_at n'est pas vide dans la base, on l'ajoute au JSON
              if (row.getLocalDateTime("updated_at") != null) {
                json.put("updated_at", row.getLocalDateTime("updated_at").format(formatter));
              }

              return json;
            })
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
  @Override
  public DatabaseService updateMessage(int id, String content, Handler<AsyncResult<String>> resultHandler) {
    dbClient.preparedQuery(sqlQueries.get(SqlQuery.UPDATE_MESSAGE))
      .execute(Tuple.of(content, id), res -> {
        if (res.succeeded() && res.result().size() > 0) {
          // On récupère la date générée par la base de données
          LocalDateTime dbDate = res.result().iterator().next().getLocalDateTime("updated_at");
          // On la formate (sans secondes et avec des slashes pour ton format)
          String formattedDate = dbDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
          resultHandler.handle(Future.succeededFuture(formattedDate));
        } else {
          resultHandler.handle(Future.failedFuture(res.cause()));
        }
      });
    return this;
  }
  @Override
  public DatabaseService deleteMessage(int id, Handler<AsyncResult<String>> resultHandler) {
    dbClient.preparedQuery(sqlQueries.get(SqlQuery.DELETE_MESSAGE))
      .execute(Tuple.of(id), res -> {
        if (res.succeeded()) {
          resultHandler.handle(Future.succeededFuture("le message a été supprimé avec succes"));
        } else {
          resultHandler.handle(Future.failedFuture(res.cause()));
        }
      });
    return this;
  }
}
