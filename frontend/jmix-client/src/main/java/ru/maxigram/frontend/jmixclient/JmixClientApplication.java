package ru.maxigram.frontend.jmixclient;

import com.google.common.base.Strings;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import jakarta.persistence.Cache;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Query;
import jakarta.persistence.SynchronizationType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.Metamodel;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@Push
@Theme(value = "jmix-client")
@PWA(name = "Jmix Client", shortName = "Jmix Client")
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class JmixClientApplication implements AppShellConfigurator {

  @Autowired private Environment environment;

  public static void main(String[] args) {
    SpringApplication.run(JmixClientApplication.class, args);
  }

  @EventListener
  public void printApplicationUrl(final ApplicationStartedEvent event) {
    LoggerFactory.getLogger(JmixClientApplication.class)
            .info(
                    "Application started at "
                            + "http://localhost:"
                            + environment.getProperty("local.server.port")
                            + Strings.nullToEmpty(environment.getProperty("server.servlet.context-path")));
  }

  @Bean
  public UserRepository userRepository() {
    return new InMemoryUserRepository();
  }

  @Bean
  public DataSource dataSource() {
    return new DataSource() {
      @Override
      public Connection getConnection() throws SQLException {
        return null;
      }

      @Override
      public Connection getConnection(String username, String password) throws SQLException {
        return null;
      }

      @Override
      public PrintWriter getLogWriter() throws SQLException {
        return null;
      }

      @Override
      public void setLogWriter(PrintWriter out) throws SQLException {}

      @Override
      public void setLoginTimeout(int seconds) throws SQLException {}

      @Override
      public int getLoginTimeout() throws SQLException {
        return 0;
      }

      @Override
      public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
      }

      @Override
      public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
      }

      @Override
      public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
      }
    };
  }

  @Bean
  public EntityManagerFactory entityManagerFactory() {
    return new EntityManagerFactory() {
      @Override
      public EntityManager createEntityManager() {
        return null;
      }

      @Override
      public EntityManager createEntityManager(Map map) {
        return null;
      }

      @Override
      public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        return null;
      }

      @Override
      public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        return null;
      }

      @Override
      public CriteriaBuilder getCriteriaBuilder() {
        return null;
      }

      @Override
      public Metamodel getMetamodel() {
        return null;
      }

      @Override
      public boolean isOpen() {
        return false;
      }

      @Override
      public void close() {}

      @Override
      public Map<String, Object> getProperties() {
        return null;
      }

      @Override
      public Cache getCache() {
        return null;
      }

      @Override
      public PersistenceUnitUtil getPersistenceUnitUtil() {
        return null;
      }

      @Override
      public void addNamedQuery(String s, Query query) {}

      @Override
      public <T> T unwrap(Class<T> aClass) {
        return null;
      }

      @Override
      public <T> void addNamedEntityGraph(String s, EntityGraph<T> entityGraph) {}
    };
  }
}
