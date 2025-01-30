package com.example.weather_vertx.db;

import io.vertx.reactivex.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.reactivex.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;

public class DatabaseConfig {

    private static MySQLPool mySQLPool;

    public static MySQLPool getMySQLPool(Vertx vertx) {
        if (mySQLPool == null) {
            MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                .setPort(3306)
                .setHost("localhost")
                .setDatabase("db1")
                .setUser("root")
                .setPassword("9121418845");

            PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

            mySQLPool = MySQLPool.pool(vertx, connectOptions, poolOptions);

            checkDatabaseConnection(vertx);
        }
        return mySQLPool;
    }

    private static void checkDatabaseConnection(Vertx vertx) {

        mySQLPool.getConnection(res -> {
            if (res.succeeded()) {
                System.out.println("Database connection successful.");
                res.result().close(); 
            } else {
                System.err.println("Failed to connect to the database: " + res.cause().getMessage());
            }
        });
    }
}
