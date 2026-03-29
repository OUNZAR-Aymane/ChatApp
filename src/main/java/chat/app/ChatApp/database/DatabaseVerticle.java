package chat.app.ChatApp.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class DatabaseVerticle extends AbstractVerticle {

  public static final String CONFIG_DB_QUEUE = "chatdb.queue";
  public static final String CONFIG_DB_SQL_QUERIES_RESOURCE_FILE = "chatdb.sqlqueries.resource.file";

  private HashMap<SqlQuery, String> loadSqlQueries() throws IOException {
    String queriesFile = config().getString(CONFIG_DB_SQL_QUERIES_RESOURCE_FILE);
    InputStream queriesInputStream;
    if (queriesFile != null) {
      queriesInputStream = new FileInputStream(queriesFile);
    } else {
      queriesInputStream = getClass().getResourceAsStream("/db-queries.properties");
    }
    Properties queriesProps = new Properties();
    queriesProps.load(queriesInputStream);
    queriesInputStream.close();
    HashMap<SqlQuery, String> sqlQueries = new HashMap<>();
    sqlQueries.put(SqlQuery.CREATE_MESSAGES_TABLE, queriesProps.getProperty("create-messages-table"));
    sqlQueries.put(SqlQuery.ALL_MESSAGES, queriesProps.getProperty("all-messages"));
    sqlQueries.put(SqlQuery.CREATE_MESSAGE, queriesProps.getProperty("create-message"));
    return sqlQueries;
  }
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    HashMap<SqlQuery, String> sqlQueries = loadSqlQueries();

    PgConnectOptions connectOptions = new PgConnectOptions()
      .setPort(5432)
      .setHost("localhost")
      .setDatabase("the-db")
      .setUser("user")
      .setPassword("secret");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    // Create the pooled client
    SqlClient dbClient = PgBuilder
      .client()
      .with(poolOptions)
      .connectingTo(connectOptions)
      .using(vertx)
      .build();

    DatabaseService.create(dbClient, sqlQueries, ready -> {
      if (ready.succeeded()) {
        ProxyHelper.registerService(DatabaseService.class, vertx, ready.result(), CONFIG_DB_QUEUE);
        startPromise.complete();
      } else {
        startPromise.fail(ready.cause());
      }
    });
  }
}
