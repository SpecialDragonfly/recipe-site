package orme.dominic.recipe.services;

import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import orme.dominic.recipe.QueryUtils;
import orme.dominic.recipe.models.*;
import orme.dominic.recipe.repository.BookRepository;

import java.util.List;
import java.util.stream.Collectors;

public class BookService {
    private final PgPool dbClient;
    private final BookRepository bookRepository;

    public BookService(PgPool dbClient, BookRepository bookRepository) {

        this.dbClient = dbClient;
        this.bookRepository = bookRepository;
    }

    public Future<BookGetAllResponse> getAll(String p, String l) {
        return dbClient.withTransaction(
                connection -> {
                    final int page = QueryUtils.getPage(p);
                    final int limit = QueryUtils.getLimit(l);
                    final int offset = QueryUtils.getOffset(page, limit);

                    return bookRepository.count(connection)
                        .flatMap(total ->
                            bookRepository.selectAll(connection, limit, offset)
                                .map(result -> {
                                    final List<BookGetOneResponse> books = result.stream()
                                        .map(b -> new BookGetOneResponse(b.id(), b.title()))
                                        .collect(Collectors.toList());

                                    return new BookGetAllResponse(total, limit, page, books);
                                })
                        );
                })
            .onSuccess(success -> System.out.println(success.books()))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
            //.onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Read all books", success.getBooks())))
            //.onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Read all books", throwable.getMessage())));

    }

    public Future<BookGetOneResponse> getOne(String id) {
        return dbClient.withTransaction(connection -> bookRepository.selectById(connection, id).map(b -> new BookGetOneResponse(b.id(), b.title())))
            .onSuccess(success -> System.out.println("Get one book " + id))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Read one book", success)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Read one book", throwable.getMessage())));
    }

    public Future<BookGetOneResponse> create(Book book) {
        return dbClient.withTransaction(connection -> bookRepository.insert(connection, book).map(b -> new BookGetOneResponse(b.id(), b.title())))
            .onSuccess(success -> System.out.println("Create one book " + book.id()))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Create one book", success)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Create one book", throwable.getMessage())));
    }

    public Future<BookGetOneResponse> update(String id, Book book) {
        return dbClient.withTransaction(connection -> bookRepository.update(connection, book.withId(id)).map(b -> new BookGetOneResponse(b.id(), b.title())))
            .onSuccess(success -> System.out.println("Update one book " + id))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Update one book", success)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Update one book", throwable.getMessage())));
    }

    public Future<Void> delete(String id) {
        return dbClient.withTransaction(connection -> bookRepository.delete(connection, id))
            .onSuccess(success -> System.out.println("Delete one book " + id))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Delete one book", id)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Delete one book", throwable.getMessage())));
    }
}
