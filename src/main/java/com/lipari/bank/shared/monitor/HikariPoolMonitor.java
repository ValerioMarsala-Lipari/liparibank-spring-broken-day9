package com.lipari.bank.shared.monitor;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@RequiredArgsConstructor
@Slf4j
public class HikariPoolMonitor {

    private final DataSource dataSource;

    @Value("${spring.datasource.hikari.maximum-pool-size}")
    private int configuredMaxPoolSize;

    @PostConstruct
    public void logPoolConfiguration() {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            log.info("HikariCP pool name: {}", hikariDataSource.getPoolName());
            log.info("HikariCP configured max-pool-size: {}", configuredMaxPoolSize);
            log.info("HikariCP connection timeout (ms): {}", hikariDataSource.getConnectionTimeout());
            log.info("HikariCP idle timeout (ms): {}", hikariDataSource.getIdleTimeout());
        }
    }
}
