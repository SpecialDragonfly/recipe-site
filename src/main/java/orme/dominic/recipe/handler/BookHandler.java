package orme.dominic.recipe.handler;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import orme.dominic.recipe.ResponseUtils;
import orme.dominic.recipe.models.*;
import orme.dominic.recipe.services.BookService;

import java.util.UUID;

public class BookHandler {

    private static final String PAGE_PARAMETER = "page";
    private static final String LIMIT_PARAMETER = "limit";
    private final BookService bookService;

    public BookHandler(BookService bookService) {

        this.bookService = bookService;
    }

    public Future<BookGetAllResponse> getAll(RoutingContext rc) {
        final String page = rc.queryParams().get(PAGE_PARAMETER);
        final String limit = rc.queryParams().get(LIMIT_PARAMETER);

        return this.bookService.getAll(page, limit)
            .onSuccess(success -> ResponseUtils.buildOkResponse(rc, success))
            .onFailure(throwable -> ResponseUtils.buildErrorResponse(rc, throwable));
    }

    public Future<BookGetOneResponse> getOne(RoutingContext routingContext) {
        String id = routingContext.pathParam("id");
        return this.bookService.getOne(id);
    }

    public Future<BookGetOneResponse> create(RoutingContext routingContext) {
        UUID uuid = UUID.randomUUID();
        Book book = new Book(uuid.toString(), routingContext.body().asJsonObject().getString("title"));

        return bookService.create(book)
            .onSuccess(success -> ResponseUtils.buildCreatedResponse(routingContext, success))
            .onFailure(throwable -> ResponseUtils.buildErrorResponse(routingContext, throwable));
    }

    public Future<BookGetOneResponse> update(RoutingContext routingContext) {
        String id = routingContext.pathParam("id");
        Book book = routingContext.body().asJsonObject().mapTo(Book.class);

        return bookService.update(id, book)
            .onSuccess(success -> ResponseUtils.buildOkResponse(routingContext, success))
            .onFailure(throwable -> ResponseUtils.buildErrorResponse(routingContext, throwable));
    }

    public Future<Void> delete(RoutingContext routingContext) {
        String id = routingContext.pathParam("id");

        return bookService.delete(id)
            .onSuccess(success -> ResponseUtils.buildNoContentResponse(routingContext))
            .onFailure(throwable -> ResponseUtils.buildErrorResponse(routingContext, throwable));
    }
}
