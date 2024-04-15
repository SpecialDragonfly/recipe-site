package orme.dominic.recipe.repository;

import io.vertx.core.Future;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.templates.RowMapper;
import io.vertx.sqlclient.templates.SqlTemplate;
import orme.dominic.recipe.models.Book;

import java.util.*;

public class BookRepository {

    private static final String SQL_SELECT_ALL = "SELECT * FROM books LIMIT #{limit} OFFSET #{offset}";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM books WHERE id = #{id}";
    private static final String SQL_INSERT = "INSERT INTO books (id, title) VALUES (#{id}, #{title}) RETURNING id";
    private static final String SQL_UPDATE = "UPDATE books SET title = #{title} WHERE id = #{id}";
    private static final String SQL_DELETE = "DELETE FROM books WHERE id = #{id}";
    private static final String SQL_COUNT = "SELECT COUNT(*) AS total FROM books";

    public Future<Integer> count(SqlConnection connection) {
        final RowMapper<Integer> ROW_MAPPER = row -> row.getInteger("total");

        return SqlTemplate
            .forQuery(connection, SQL_COUNT)
            .mapTo(ROW_MAPPER)
            .execute(Collections.emptyMap())
            .map(rowSet -> rowSet.iterator().next())
            .onSuccess(success -> System.out.println(SQL_COUNT))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Count books", SQL_COUNT)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Count book", throwable.getMessage())));
    }

    public Future<List<Book>> selectAll(SqlConnection connection, int limit, int offset) {
        Map<String,Object> params = new HashMap<>();
        params.put("limit",limit);
        params.put("offset",offset);
        return SqlTemplate
            .forQuery(connection, SQL_SELECT_ALL)
            .mapTo(Book.class)
            .execute(params)
            .map(rowSet -> {
                final List<Book> books = new ArrayList<>();
                rowSet.forEach(books::add);

                return books;
            })
            .onSuccess(success -> System.out.println(SQL_SELECT_ALL))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Read all books", SQL_SELECT_ALL)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Read all books", throwable.getMessage())));
    }

    public Future<Book> selectById(SqlConnection connection, String id) {
        return SqlTemplate
            .forQuery(connection, SQL_SELECT_BY_ID)
            .mapTo(Book.class)
            .execute(Collections.singletonMap("id", id))
            .map(rowSet -> {
                final RowIterator<Book> iterator = rowSet.iterator();

                if (iterator.hasNext()) {
                    return iterator.next();
                } else {
                    throw new NoSuchElementException(id);
                    //throw new NoSuchElementException(LogUtils.NO_BOOK_WITH_ID_MESSAGE.buildMessage(id));
                }
            })
            .onSuccess(success -> System.out.println(SQL_SELECT_BY_ID))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Read book by id", SQL_SELECT_BY_ID)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Read book by id", throwable.getMessage())));
    }

    public Future<Book> insert(SqlConnection connection, Book book) {
        return SqlTemplate
            .forUpdate(connection, SQL_INSERT)
            .mapFrom(Book.class)
            .execute(book)
            .flatMap(rowSet -> {
                if (rowSet.rowCount() > 0) {
                    return Future.succeededFuture(book);
                } else {
                    throw new NoSuchElementException("Id: " + book.id());
                    //throw new NoSuchElementException(LogUtils.NO_BOOK_WITH_ID_MESSAGE.buildMessage(book.getId()));
                }
            })
            .onSuccess(success -> System.out.println(SQL_INSERT))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Update book", SQL_UPDATE)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Update book", throwable.getMessage())));
    }

    public Future<Book> update(SqlConnection connection, Book book) {
        return SqlTemplate
            .forUpdate(connection, SQL_UPDATE)
            .mapFrom(Book.class)
            .execute(book)
            .flatMap(rowSet -> {
                if (rowSet.rowCount() > 0) {
                    return Future.succeededFuture(book);
                } else {
                    throw new NoSuchElementException("Id: " + book.id());
                    //throw new NoSuchElementException(LogUtils.NO_BOOK_WITH_ID_MESSAGE.buildMessage(book.id()));
                }
            })
            .onSuccess(success -> System.out.println(SQL_UPDATE))
            .onFailure(throwable -> System.out.println(throwable.getMessage()));
//            .onSuccess(success -> LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Update book", SQL_UPDATE)))
//            .onFailure(throwable -> LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Update book", throwable.getMessage())));
    }

    public Future<Void> delete(SqlConnection connection, String id) {
        return SqlTemplate
            .forUpdate(connection, SQL_DELETE)
            .execute(Collections.singletonMap("id", id))
            .flatMap(rowSet -> {
                if (rowSet.rowCount() > 0) {
                    //LOGGER.info(LogUtils.REGULAR_CALL_SUCCESS_MESSAGE.buildMessage("Delete book", SQL_DELETE));
                    return Future.succeededFuture();
                } else {
                    //LOGGER.error(LogUtils.REGULAR_CALL_ERROR_MESSAGE.buildMessage("Delete book", LogUtils.NO_BOOK_WITH_ID_MESSAGE.buildMessage(id)));
                    throw new NoSuchElementException("Id: " + id);
                    //throw new NoSuchElementException(LogUtils.NO_BOOK_WITH_ID_MESSAGE.buildMessage(id));
                }
            });
    }
}
