package chat.app.ChatApp.http;

import chat.app.ChatApp.database.DatabaseService;
import chat.app.ChatApp.database.DatabaseVerticle;
import io.vertx.core.*;

import com.github.rjeschke.txtmark.Processor;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static chat.app.ChatApp.database.DatabaseVerticle.CONFIG_DB_QUEUE;

public class HttpServerVerticle extends AbstractVerticle {
  private FreeMarkerTemplateEngine templateEngine;
  private DatabaseService dbService;
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    templateEngine = FreeMarkerTemplateEngine.create(vertx);
    String DbQueue = config().getString(CONFIG_DB_QUEUE, "db.queue");

    dbService = DatabaseService.createProxy(vertx, DbQueue);
    HttpServer server = vertx.createHttpServer();
  }
  private void indexHandler(RoutingContext context) {
    dbService.fetchMessages(reply -> {
      if (reply.succeeded()) {
        context.put("title", "Chat home");
        context.put("pages", reply.result().getList());
        templateEngine.render(context.data(), "/templates/index.ftl", ar -> {
          if (ar.succeeded()) {
            context.response().putHeader("Content-Type", "text/html");
            context.response().end(ar.result());
          } else {
            context.fail(ar.cause());
          }
        });
      } else {
        context.fail(reply.cause());
      }
    });
  }
  private void messageCreationHandler(RoutingContext context) {
    dbService.createMessage(reply -> {
    });
  }
  // ça ça vient du TP
  private static final String EMPTY_PAGE_MARKDOWN = """
  # A new page
  Feel-free to write in Markdown!
  """;
  private void pageRenderingHandler(RoutingContext context) {
    String requestedPage = context.request().getParam("page");
    dbService.fetchPage(requestedPage, reply -> {
      if (reply.succeeded()) {
        JsonObject body = reply.result();
        boolean found = body.getBoolean("found");
        String rawContent = body.getString("rawContent", EMPTY_PAGE_MARKDOWN);
        context.put("title", requestedPage);
        context.put("id", body.getInteger("id", -1));
        context.put("newPage", found ? "no" : "yes");
        context.put("rawContent", rawContent);
        context.put("content", Processor.process(rawContent));
        context.put("timestamp", new Date().toString());
        templateEngine.render(context.data(), "/templates/page.ftl", ar -> {
          if (ar.succeeded()) {
            context.response().putHeader("Content-Type", "text/html");
            context.response().end(ar.result());
          } else {
            context.fail(ar.cause());
          }
        });
      } else {
        context.fail(reply.cause());
      }
    });
  }
  private void pageCreateHandler(RoutingContext context) {
    String pageName = context.request().getParam("name");
    String location = "/chat/" + pageName;
    if (pageName == null || pageName.isEmpty()) {
      location = "/";
    }
    context.response().setStatusCode(303);
    context.response().putHeader("Location", location);
    context.response().end();
  }
  private void pageUpdateHandler(RoutingContext context) {
    String title = context.request().getParam("title");
    Handler<AsyncResult<Void>> handler = reply -> {
      if (reply.succeeded()) {
        context.response().setStatusCode(303);
        context.response().putHeader("Location", "/chat/" + title);
        context.response().end();
      } else {
        context.fail(reply.cause());
      }
    };
    String markdown = context.request().getParam("markdown");
    if ("yes".equals(context.request().getParam("newPage"))) {
      dbService.createPage(title, markdown, handler);
    } else {
      dbService.savePage(Integer.valueOf(context.request().getParam("id")), markdown,
        handler);
    }
  }
  private void pageDeletionHandler(RoutingContext context) {
    dbService.deletePage(Integer.valueOf(context.request().getParam("id")), reply -> {
      if (reply.succeeded()) {
        context.response().setStatusCode(303);
        context.response().putHeader("Location", "/");
        context.response().end();
      } else {
        context.fail(reply.cause());
      }
    });
  }
}
