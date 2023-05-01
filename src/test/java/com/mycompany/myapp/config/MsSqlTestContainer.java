package com.mycompany.myapp.config;

import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

public class MsSqlTestContainer implements SqlTestContainer {

    private static final Logger log = LoggerFactory.getLogger(MsSqlTestContainer.class);

    private MSSQLServerContainer<?> mSSQLServerContainer;

    @Override
    public void destroy() {
        if (null != mSSQLServerContainer && mSSQLServerContainer.isRunning()) {
            mSSQLServerContainer.stop();
        }
    }

    @Override
    public void afterPropertiesSet() {
        if (null == mSSQLServerContainer) {
            mSSQLServerContainer =
                new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2019-CU16-GDR1-ubuntu-20.04")
                    .withTmpFs(Collections.singletonMap("/testtmpfs", "rw"))
                    .withLogConsumer(new Slf4jLogConsumer(log))
                    .withReuse(true);
        }
        if (!mSSQLServerContainer.isRunning()) {
            mSSQLServerContainer.start();
        }
    }

    @Override
    public JdbcDatabaseContainer<?> getTestContainer() {
        return mSSQLServerContainer;
    }
}
