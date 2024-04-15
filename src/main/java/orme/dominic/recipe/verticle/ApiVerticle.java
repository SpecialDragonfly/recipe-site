package orme.dominic.recipe.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.pgclient.PgPool;
import orme.dominic.recipe.ConfigUtils;
import orme.dominic.recipe.handler.BookHandler;
import orme.dominic.recipe.repository.BookRepository;
import orme.dominic.recipe.router.BookRouter;
import orme.dominic.recipe.services.BookService;

public class ApiVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> promise) throws Exception {
        PgPool dbClient = ConfigUtils.getInstance().buildDbClient(vertx);

        BookRepository bookRepository = new BookRepository();

        BookService bookService = new BookService(dbClient, bookRepository);
        BookHandler bookHandler = new BookHandler(bookService);
        BookRouter bookRouter = new BookRouter(vertx, bookHandler);

        Router router = Router.router(vertx);
        bookRouter.setRouter(router); // <-- bad name. Actually sets up the routes.

        int port = ConfigUtils.getInstance().getServerPort();

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(port, http -> {
                if (http.succeeded()) {
                    promise.complete();
                    //LOGGER.info(LogUtils.RUN_HTTP_SERVER_SUCCESS_MESSAGE.buildMessage(port));
                } else {
                    promise.fail(http.cause());
                    System.out.println(http.cause().getMessage());
                    //LOGGER.info(LogUtils.RUN_HTTP_SERVER_ERROR_MESSAGE.buildMessage());
                }
            });
    }
}
