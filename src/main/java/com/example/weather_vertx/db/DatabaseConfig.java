package com.example.weather_vertx.db;

import io.vertx.reactivex.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.reactivex.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

public class DatabaseConfig {

  private static Pool pool;

  public static synchronized Pool getPool(Vertx vertx) {
    if (pool == null) {
      MySQLConnectOptions connectOptions = new MySQLConnectOptions()
        .setPort(3306)
        .setHost("localhost")
        .setDatabase("db")
        .setUser("root")
        .setPassword("9121418845");

      PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

      pool = Pool.pool(vertx, connectOptions, poolOptions);
      checkDatabaseConnection();
    }
    return pool;
  }

  private static void checkDatabaseConnection() {
    pool.getConnection(res -> {
      if (res.succeeded()) {
        System.out.println("✅ Database connected successfully.");
        res.result().close();
      } else {
        System.err.println("❌ Database connection failed: " + res.cause().getMessage());
      }
    });
  }

  public static void closePool() {
    if (pool != null) {
      pool.close();
      System.out.println("Database pool closed.");
    }
  }
}
