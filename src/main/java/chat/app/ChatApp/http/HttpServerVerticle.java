package chat.app.ChatApp.http;

import chat.app.ChatApp.database.DatabaseService;

import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

  private FreeMarkerTemplateEngine templateEngine;
  private DatabaseService dbService;

  @Override
  public void start(Promise<Void> startPromise) {
    templateEngine = FreeMarkerTemplateEngine.create(vertx);
    String dbQueue = config().getString("db.queue", "db.queue");
    dbService = DatabaseService.createProxy(vertx, dbQueue);

    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    router.get("/").handler(this::indexHandler);
    router.post("/message").handler(this::messageCreationHandler);

    server.requestHandler(router).listen(8080, ar -> {
      if (ar.succeeded()) {
        LOGGER.info("HTTP server started on port 8080");
        startPromise.complete();
      } else {
        LOGGER.error("Failed to start HTTP server", ar.cause());
        startPromise.fail(ar.cause());
      }
    });
  }
  // handlers
  private void indexHandler(RoutingContext context) {
    dbService.fetchMessages(reply -> {
      if (reply.succeeded()) {

        context.put("title", "Chat home");
        context.put("messages", reply.result().getList());

        templateEngine.render(context.data(), "templates/index.ftl", ar -> {
          if (ar.succeeded()) {
            context.response()
              .putHeader("Content-Type", "text/html")
              .end(ar.result());
          } else {
            context.fail(ar.cause());
          }
        });

      } else {
        LOGGER.error("Failed to fetch messages", reply.cause());
        context.fail(reply.cause());
      }
    });
  }

  private void messageCreationHandler(RoutingContext context) {

    String sender = context.request().getParam("sender");
    String message = context.request().getParam("message");

    if (sender == null || sender.isEmpty() ||
      message == null || message.isEmpty()) {
      context.response()
        .setStatusCode(400)
        .end("Sender and message must not be empty");
      return;
    }

    dbService.createMessage(sender, message, reply -> {
      if (reply.succeeded()) {
        context.response()
          .setStatusCode(303)
          .putHeader("Location", "/")
          .end();

      } else {
        LOGGER.error("Failed to create message", reply.cause());
        context.fail(reply.cause());
      }
    });
  }
}
