package com.example.tracker.automation.sql;

import com.example.tracker.automation.api.WorkItemApiClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Base class for the SQL/JDBC persistence-verification layer. Independent of
 * BaseUiTest and BaseApiTest by design: state changes are driven through the
 * REST API (via WorkItemApiClient), and persistence is verified read-only,
 * directly over JDBC - never through application repositories or Hibernate
 * entities.
 *
 * Requires the app under test to be started with
 * -Dspring.profiles.active=sql-verify (see H2TcpServerConfig in the app
 * repo), which exposes its in-memory H2 database over H2's TCP protocol on
 * localhost:9092. Without that profile active, connecting here fails fast
 * with a clear "connection refused" rather than silently skipping.
 */
public abstract class BaseSqlTest {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final String DEFAULT_DB_URL = "jdbc:h2:tcp://localhost:9092/mem:trackerdb";
    private static final String DEFAULT_DB_USER = "sa";
    private static final String DEFAULT_DB_PASSWORD = "";

    protected static WorkItemApiClient api;
    protected static WorkItemSqlHelper sql;

    private static Connection connection;

    @BeforeAll
    static void setUp() throws SQLException {
        String baseUrl = System.getProperty("base.url", DEFAULT_BASE_URL);
        api = new WorkItemApiClient(baseUrl);

        String dbUrl = System.getProperty("db.url", DEFAULT_DB_URL);
        String dbUser = System.getProperty("db.user", DEFAULT_DB_USER);
        String dbPassword = System.getProperty("db.password", DEFAULT_DB_PASSWORD);
        connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        sql = new WorkItemSqlHelper(connection);
    }

    @AfterAll
    static void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
