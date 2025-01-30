package com.example.weather_vertx;

import com.example.weather_vertx.db.DatabaseConfig;
import com.example.weather_vertx.routes.WeatherRoutes;

import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.mysqlclient.MySQLPool;

public class MainVerticle extends AbstractVerticle {

    private static final int PORT = 8000;
    private MySQLPool mySQLPool;

    @Override
    public void start() throws Exception {

            mySQLPool = DatabaseConfig.getMySQLPool(vertx);
        
            Router mainRouter = Router.router(vertx);

           WeatherRoutes weatherRoutes = new WeatherRoutes(vertx, mySQLPool);
           mainRouter.mountSubRouter("", weatherRoutes.getRouter());

            mainRouter.get("/").handler(ctx -> {
                ctx.response()
                    .setStatusCode(200)
                    .end("Hello Pookie!!");
            });
    
            vertx.createHttpServer()
                .requestHandler(mainRouter)
                .rxListen(PORT)
                .subscribe(httpServer -> System.out.println("Weather Server is running on port :: " + PORT),
                           throwable -> {
                               System.err.println("Failed to start the server: " + throwable.getMessage());
                               throwable.printStackTrace();
                           });
    }
    
}
