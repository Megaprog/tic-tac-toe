package org.jmmo.tic_tac_toe.config;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.TupleType;
import org.apache.cassandra.service.EmbeddedCassandraService;
import org.jmmo.sc.Cassandra;
import org.jmmo.sc.EntityPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

@Configuration
public class PersistenceConfig {
    private static final Logger log = LoggerFactory.getLogger(PersistenceConfig.class);

    @Bean
    public Cassandra cassandra(@Value("${ttt.cassandra.embedded:true}") boolean embedded,
                               @Value("#{'${ttt.cassandra.hosts:localhost}'.split(',')}") String[] hosts,
                               @Value("${ttt.cassandra.keyspace:tic_tac_toe}") String keyspace,
                               @Value("${ttt.cassandra.truncate:false}") boolean truncate) throws IOException {

        if (embedded) {
            log.info("Starting embedded cassandra");
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

    @Bean
    public TupleType coordsTupleType(Cassandra cassandra) {
        return cassandra.getSession().getCluster().getMetadata().newTupleType(DataType.cint(), DataType.cint());
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

        cassandra.execute("truncate claim");
        cassandra.execute("truncate player");
        cassandra.execute("truncate game");
    }

    protected void ddl(Cassandra cassandra, Resource ddlResource) {
        readStatements(ddlResource).forEach(ddlStatement -> {
            log.debug("Executing ddl statement:\n" + ddlStatement);

            cassandra.execute(ddlStatement);
        });
    }

    static final Pattern STATEMENT_DELIMITER = Pattern.compile(";(?=([^']*'[^']*')*[^']*$)");

    protected List<String> readStatements(Resource resource) {
        final List<String> statements = new ArrayList<>();

        try (final Scanner scanner = new Scanner(resource.getInputStream()).useDelimiter(STATEMENT_DELIMITER)) {
            while (true) {
                try {
                    final String trimmed = scanner.next().trim();
                    if (!trimmed.isEmpty()) {
                        statements.add(trimmed + ";");
                    }
                }
                catch (NoSuchElementException e) {
                    break;
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return statements;
    }

    @Value("ddl/claim.cql")
    Resource pending;

    @Value("ddl/player.cql")
    Resource player;

    @Value("ddl/game.cql")
    Resource game;
}
