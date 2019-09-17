package com.rbkmoney.cashreg;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@ContextConfiguration(classes = CashRegApplication.class, initializers = AbstractIntegrationTest.Initializer.class)
public abstract class AbstractIntegrationTest {

    protected String cashregId = "cashreg_id";
    protected String partyId = "party_id";
    protected String shopId = "shop_id";
    protected String namespace = "cashreg";

    public final static String MG_IMAGE = "dr2.rbkmoney.com/rbkmoney/machinegun";
    public final static String MG_TAG = "5b85e3c73041e5cbcfcc35c465cf14214163389b";

    public final static String POSTGRES_IMAGE = "dr2.rbkmoney.com/rbkmoney/postgres";
    public final static String POSTGRES_TAG = "a5ebd642e6efdd1ef87d844c58765fd3766b0440";

    @ClassRule
    public static GenericContainer machinegunContainer = new GenericContainer(MG_IMAGE + ":" + MG_TAG)
            .withExposedPorts(8022)
            .withClasspathResourceMapping(
                    "/machinegun/config.yaml",
                    "/opt/machinegun/etc/config.yaml",
                    BindMode.READ_ONLY
            )
            .waitingFor(
                    new HttpWaitStrategy()
                            .forPath("/health")
                            .forStatusCode(200)
            );

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer(POSTGRES_IMAGE + ":" + POSTGRES_TAG)
            .withDatabaseName("cashreg")
            .withUsername("test")
            .withPassword("test");

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "service.mg.automaton.url=http://" + machinegunContainer.getContainerIpAddress() + ":" + machinegunContainer.getMappedPort(8022) + "/v1/automaton",
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword()
            ).applyTo(configurableApplicationContext);
        }
    }

}
