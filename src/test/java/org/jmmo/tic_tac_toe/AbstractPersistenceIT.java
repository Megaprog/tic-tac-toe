package org.jmmo.tic_tac_toe;

import org.jmmo.sc.Cassandra;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ITPersistenceConfig.class)
public abstract class AbstractPersistenceIT {

    @Autowired
    protected Cassandra cassandra;

    @Autowired
    protected ITPersistenceConfig.TestDateSupplier testDateSupplier;

    @Autowired
    protected ITPersistenceConfig.TestRandomSupplier testRandomSupplier;
}
