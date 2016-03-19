package org.jmmo.tic_tac_toe.config;

import com.datastax.driver.core.Cluster;
import org.apache.cassandra.service.EmbeddedCassandraService;
import org.jmmo.sc.Cassandra;
import org.jmmo.sc.EntityPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class PersistenceConfig {

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

    public static void initKeyspace(Cassandra cassandra, String keyspace) {
        cassandra.execute("CREATE KEYSPACE IF NOT EXISTS " + keyspace + " WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};");
        cassandra.execute("USE " + keyspace);
    }

    public static void createTables(Cassandra cassandra) {
        cassandra.execute("CREATE TABLE IF NOT EXISTS my (\n" +
                "  id text PRIMARY KEY,\n" +
                "  pings counter,\n" +
                ")");
    }

    public static void truncateTables(Cassandra cassandra) {
        cassandra.execute("truncate " + "my");
    }
}
