package org.jmmo.tic_tac_toe.config;

import com.datastax.driver.core.Cluster;
import org.apache.cassandra.service.EmbeddedCassandraService;
import org.jmmo.sc.Cassandra;
import org.jmmo.sc.EntityPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Configuration
public class PersistenceConfig {
    private static final Logger log = LoggerFactory.getLogger(PersistenceConfig.class);

    @Bean
    public Cassandra cassandra(@Value("${ttt.cassandra.embedded:true}") boolean embedded,
                               @Value("#{'${ttt.cassandra.hosts:localhost}'.split(',')}") String[] hosts,
                               @Value("${ttt.cassandra.keyspace:tic_tac_toe}") String keyspace,
                               @Value("${ttt.cassandra.truncate:false}") boolean truncate) throws IOException {

        if (embedded) {
            new EmbeddedCassandraService().start();
        }

        final EntityPool entityPool = new EntityPool();
        final Cassandra cassandra = createCassandra(hosts);

        initKeyspace(cassandra, keyspace);

        createTables(cassandra);

        if (truncate) {
            truncateTables(cassandra);
        }

        return cassandra;
    }

    public Cassandra createCassandra(String ...contactPoints) {
        return new Cassandra(Cluster.builder().addContactPoints(contactPoints).build().connect());
    }

    public void initKeyspace(Cassandra cassandra, String keyspace) {
        cassandra.execute("CREATE KEYSPACE IF NOT EXISTS " + keyspace + " WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};");
        cassandra.execute("USE " + keyspace);
    }

    public void createTables(Cassandra cassandra) {
        ddl(cassandra, pending);
        ddl(cassandra, player);
        ddl(cassandra, game);
    }

    public static void truncateTables(Cassandra cassandra) {
        log.info("Truncate database tables");

        cassandra.execute("truncate pending");
        cassandra.execute("truncate player");
        cassandra.execute("truncate game");
    }

    protected void ddl(Cassandra cassandra, Resource ddlResource) {
        try {
            final String ddlStatement = new BufferedReader(new InputStreamReader(ddlResource.getInputStream())).lines().collect(Collectors.joining("\n"));

            log.debug("Executing ddl statement:\n" + ddlStatement);

            cassandra.execute(ddlStatement);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Value("ddl/pending.cql")
    Resource pending;

    @Value("ddl/player.cql")
    Resource player;

    @Value("ddl/game.cql")
    Resource game;
}
