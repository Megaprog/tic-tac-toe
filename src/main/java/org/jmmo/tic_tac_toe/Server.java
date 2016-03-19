package org.jmmo.tic_tac_toe;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import org.jmmo.tic_tac_toe.config.AppConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertyResolver;

public class Server extends AbstractVerticle {

    ConfigurableApplicationContext applicationContext;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        vertx.<ApplicationContext>executeBlocking(future -> {
            try {
                applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
                applicationContext.registerShutdownHook();
                future.complete(applicationContext);
            }
            catch (Exception e) {
                future.fail(e);
            }

        }, initContextResult -> {

            if (initContextResult.failed()) {
                startFuture.fail(initContextResult.cause());
                return;
            }

            final PropertyResolver propertyResolver = applicationContext.getBean(PropertyResolver.class);
            final HttpServer httpServer = vertx.createHttpServer();

            httpServer
                    .requestHandler(applicationContext.getBean(Router.class)::reply)
                    .listen(propertyResolver.getProperty("ttt.http.port", Integer.class, 8080), propertyResolver.getProperty("ttt.http.host", "0.0.0.0"), res -> {
                        if (res.succeeded()) {
                            startFuture.complete();
                        } else {
                            startFuture.fail(res.cause());
                        }
                    });
        });
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        vertx.executeBlocking(future -> {
            try {
                applicationContext.close();
                future.complete();
            } catch (Exception e) {
                future.fail(e);
            }
        }, closeContextResult -> {
            if (closeContextResult.succeeded()) {
                stopFuture.complete();
            } else {
                stopFuture.fail(closeContextResult.cause());
            }
        });
    }
}
