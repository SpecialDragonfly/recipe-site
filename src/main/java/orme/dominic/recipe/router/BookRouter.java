package orme.dominic.recipe.router;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import orme.dominic.recipe.handler.BookHandler;

public class BookRouter {
    private final Vertx vertx;
    private final BookHandler bookHandler;

    public BookRouter(Vertx vertx, BookHandler bookHandler) {
        this.vertx = vertx;
        this.bookHandler = bookHandler;
    }

    public void setRouter(Router router) {
        router.mountSubRouter("/api/v1", buildBookRouter());
    }

    private Router buildBookRouter() {
        final Router bookRouter = Router.router(vertx);
        bookRouter.route("/books*").handler(BodyHandler.create());
        bookRouter.get("/books").handler(LoggerHandler.create(LoggerFormat.DEFAULT)).handler(bookHandler::getAll);
        bookRouter.get("/books/:id").handler(LoggerHandler.create(LoggerFormat.DEFAULT)).handler(bookHandler::getOne);
        bookRouter.post("/books").handler(LoggerHandler.create(LoggerFormat.DEFAULT)).handler(bookHandler::create);
        bookRouter.put("/books/:id").handler(LoggerHandler.create(LoggerFormat.DEFAULT)).handler(bookHandler::update);
        bookRouter.delete("/books/:id").handler(LoggerHandler.create(LoggerFormat.DEFAULT)).handler(bookHandler::delete);

        return bookRouter;
    }
}
