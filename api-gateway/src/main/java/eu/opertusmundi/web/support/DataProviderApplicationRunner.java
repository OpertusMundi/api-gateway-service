package eu.opertusmundi.web.support;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import eu.opertusmundi.common.service.integration.DataProviderManager;

/**
 * {@link ApplicationRunner} for initializing external data provider manager
 */
@Component
public class DataProviderApplicationRunner implements ApplicationRunner {

    private final DataProviderManager DataProviderManager;

    public DataProviderApplicationRunner(DataProviderManager dataProviderManager) {
        this.DataProviderManager = dataProviderManager;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.DataProviderManager.refreshProviders();
    }

}
