package chat.app.ChatApp.http;

import chat.app.ChatApp.database.DatabaseService;

import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.bridge.PermittedOptions;

public class HttpServerVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

  private FreeMarkerTemplateEngine templateEngine;
  private DatabaseService dbService;

  @Override
  public void start(Promise<Void> startPromise) {
    templateEngine = FreeMarkerTemplateEngine.create(vertx);
    String dbQueue = config().getString("db.queue", "chatdb.queue");
    dbService = DatabaseService.createProxy(vertx, dbQueue);

    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    router.route("/js/*").handler(StaticHandler.create("webroot/js"));
    router.route("/css/*").handler(StaticHandler.create("webroot/css"));
    router.route().handler(BodyHandler.create());

    SockJSBridgeOptions options = new SockJSBridgeOptions()
      .addInboundPermitted(new PermittedOptions().setAddress("chat.message"))
      .addOutboundPermitted(new PermittedOptions().setAddress("chat.message"));

    router.mountSubRouter("/eventbus", SockJSHandler.create(vertx).bridge(options));

    // GET / -> render index.ftl
    router.get("/").handler(ctx -> {
      dbService.getLastMessages(reply -> {
        if (reply.succeeded()) {
          ctx.put("title", "Chat home");
          ctx.put("messages", reply.result().getList());

          templateEngine.render(ctx.data(), "templates/index.ftl", ar -> {
            if (ar.succeeded()) {
              ctx.response()
                .putHeader("Content-Type", "text/html")
                .end(ar.result());
            } else {
              ctx.fail(ar.cause());
            }
          });

        } else {
          LOGGER.error("Failed to fetch messages", reply.cause());
          ctx.fail(reply.cause());
        }
      });
    });

    // GET /api/messages
    router.get("/api/messages").handler(ctx -> {
      dbService.getLastMessages(reply -> {
        if (reply.succeeded()) {
          ctx.response()
            .putHeader("Content-Type", "application/json")
            .end(reply.result().encode());
        } else {
          ctx.fail(reply.cause());
        }
      });
    });

    // POST /api/messages
    router.post("/api/messages").handler(ctx -> {
      String sender = ctx.request().getFormAttribute("sender");
      String content = ctx.request().getFormAttribute("content");

      if (sender == null || sender.isEmpty() || content == null || content.isEmpty()) {
        ctx.response().setStatusCode(400).end("sender and content required");
        return;
      }

      JsonObject message = new JsonObject()
        .put("sender", sender)
        .put("content", content);

      dbService.addMessage(message, reply -> {
        if (reply.succeeded()) {
          // send to all clients
          vertx.eventBus().publish("chat.message", message);
          // Redirect back to home page
          ctx.response()
            .setStatusCode(303)
            .putHeader("Location", "/")
            .end();
        } else {
          ctx.fail(reply.cause());
        }
      });
    });

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
}




