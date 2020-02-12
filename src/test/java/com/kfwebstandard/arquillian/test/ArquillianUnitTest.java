package com.kfwebstandard.arquillian.test;

import com.kfwebstandard.jpacontroller.FishActionBeanJPA;
import com.kfwebstandard.entities.Fish;
import com.kfwebstandard.exceptions.RollbackFailureException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample Arquillian test class
 *
 * @author Ken Fogel with assistance from Bartosz Majsak
 * @version 1.1
 */
@RunWith(Arquillian.class)
public class ArquillianUnitTest {

    private final static Logger LOG = LoggerFactory.getLogger(ArquillianUnitTest.class);

    @Deployment
    public static WebArchive deploy() {

        // Use an alternative to the JUnit assert library called AssertJ
        // Need to reference MySQL driver as it is not part of either
        // embedded or remote
        final File[] dependencies = Maven
                .resolver()
                .loadPomFromFile("pom.xml")
                .resolve("mysql:mysql-connector-java",
                        "org.assertj:assertj-core",
                        "org.slf4j:slf4j-api",
                        "org.apache.logging.log4j:log4j-slf4j-impl",
                        "org.apache.logging.log4j:log4j-web"
                ).withTransitivity()
                .asFile();

        // The SQL script to create the database is in src/test/resources
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                .setWebXML(new File("src/main/webapp/WEB-INF/web.xml"))
                .addPackage(FishActionBeanJPA.class.getPackage())
                .addPackage(RollbackFailureException.class.getPackage())
                .addPackage(Fish.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/payara-resources.xml"), "payara-resources.xml")
                .addAsResource(new File("src/main/resources/META-INF/persistence.xml"), "META-INF/persistence.xml")
                .addAsResource(new File("src/main/resources/log4j2.xml"), "log4j2.xml")
                .addAsResource("createFishTable.sql")
                .addAsLibraries(dependencies);

        return webArchive;
    }

    @Inject
    private FishActionBeanJPA fab;

    @Resource(lookup = "java:app/jdbc/myAquarium")
    private DataSource ds;

//    @PersistenceContext(unitName = "fishiesPU")
//    private EntityManager em;
//
//    @Resource
//    private UserTransaction utx;

    @Test
    public void testCreate() throws RollbackFailureException, IllegalStateException, Exception {
        Fish bob = new Fish();
        bob.setCommonname("Moose Fish");
        bob.setDiet("Meat");
        bob.setFishsize("16");
        bob.setKh("4");
        bob.setLatin("Moose");
        bob.setSpeciesorigin("lachine");
        bob.setStocking("Yes");
        bob.setTanksize("Big");
        bob.setTemp("78");
        bob.setPh("14");

        fab.create(bob);

        Fish bob1 = fab.findFish(bob.getId());

        LOG.debug("ID " + bob.getId());

        assertEquals("Not same the same", bob1, bob);

    }

    /**
     * Verify that the FishActionBeanJPA is returning the 200 fish in the
     * database
     *
     * @throws SQLException
     */
    @Test
    public void should_find_all_fish_01() throws SQLException {
        LOG.info("should_find_all_fish_01()");

        List<Fish> lfd = fab.findFishEntities();
        assertThat(lfd).hasSize(200);
    }

    /**
     * Restore the database to a known state before testing. This is important
     * if the test is destructive. This routine is courtesy of Bartosz Majsak
     * who also solved my Arquillian remote server problem
     */
    @Before
    public void seedDatabase() {
        final String seedDataScript = loadAsString("createFishTable.sql");

        try (Connection connection = ds.getConnection()) {
            for (String statement : splitStatements(new StringReader(
                    seedDataScript), ";")) {
                connection.prepareStatement(statement).execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed seeding database", e);
        }
    }

    /**
     * The following methods support the seedDatabse method
     */
    private String loadAsString(final String path) {
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(path)) {
            return new Scanner(inputStream).useDelimiter("\\A").next();
        } catch (IOException e) {
            throw new RuntimeException("Unable to close input stream.", e);
        }
    }

    private List<String> splitStatements(Reader reader,
            String statementDelimiter) {
        final BufferedReader bufferedReader = new BufferedReader(reader);
        final StringBuilder sqlStatement = new StringBuilder();
        final List<String> statements = new LinkedList<>();
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || isComment(line)) {
                    continue;
                }
                sqlStatement.append(line);
                if (line.endsWith(statementDelimiter)) {
                    statements.add(sqlStatement.toString());
                    sqlStatement.setLength(0);
                }
            }
            return statements;
        } catch (IOException e) {
            throw new RuntimeException("Failed parsing sql", e);
        }
    }

    private boolean isComment(final String line) {
        return line.startsWith("--") || line.startsWith("//")
                || line.startsWith("/*");
    }
}
