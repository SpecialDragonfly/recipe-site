package orme.dominic.recipe;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtils {

    private static ConfigUtils instance;

    private final Properties properties;

    private static final String HOST_CONFIG = "datasource.host";
    private static final String PORT_CONFIG = "datasource.port";
    private static final String DATABASE_CONFIG = "datasource.database";
    private static final String USERNAME_CONFIG = "datasource.username";
    private static final String PASSWORD_CONFIG = "datasource.password";

    private ConfigUtils() {
        this.properties = readProperties();
    }

    public static ConfigUtils getInstance() {
        if (instance == null) {
            instance = new ConfigUtils();
        }

        return instance;
    }

    private Properties readProperties() {
        Properties properties = new Properties();

        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("application.settings");
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }

    public int getServerPort() {
        return Integer.parseInt(this.properties.getProperty("server.port"));
    }

    public int numberOfAvailableCores() {
        // I divide this in half to save some resources while developing
        return Runtime.getRuntime().availableProcessors() / 2;
    }

    public PgPool buildDbClient(Vertx vertx) {
        final Properties properties = this.properties;

        final PgConnectOptions connectOptions = new PgConnectOptions()
            .setPort(Integer.parseInt(properties.getProperty(PORT_CONFIG)))
            .setHost(properties.getProperty(HOST_CONFIG))
            .setDatabase(properties.getProperty(DATABASE_CONFIG))
            .setUser(properties.getProperty(USERNAME_CONFIG))
            .setPassword(properties.getProperty(PASSWORD_CONFIG));

        final PoolOptions poolOptions = new PoolOptions().setMaxSize(this.numberOfAvailableCores());

        return PgPool.pool(vertx, connectOptions, poolOptions);
    }

    public Configuration buildMigrationsConfiguration() {
        final Properties properties = this.properties;

        final String url = "jdbc:postgresql://" + properties.getProperty(HOST_CONFIG) + ":" + properties.getProperty(PORT_CONFIG) + "/" + properties.getProperty(DATABASE_CONFIG);

        return new FluentConfiguration().dataSource(url, properties.getProperty(USERNAME_CONFIG), properties.getProperty(PASSWORD_CONFIG));
    }

}
