package eu.opertusmundi.web.support;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataTruncateService {

    private static final Logger logger = LoggerFactory.getLogger(DataTruncateService.class);

    /**
     * Recent search cleanup interval in milliseconds
     */
    private static final long RECENT_SEARCH_INTERVAL = 60 * 60 * 1000;

    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    @Value("${opertus-mundi.recent-search.truncate-limit:10}")
    private int limit;

    @Scheduled(fixedRate = RECENT_SEARCH_INTERVAL, initialDelay = 5000L)
    @Transactional
    public void invalidateSessions() {
        try {
            final String sqlString =
                "DELETE FROM web.account_recent_search WHERE id IN ( " +
                "   SELECT id FROM ( " +
                "       SELECT s.id, ROW_NUMBER() OVER (PARTITION BY a.id ORDER BY s.id DESC) i " +
                "       FROM   web.account_recent_search s INNER JOIN web.account a ON s.account = a.id " +
                "   ) AS p WHERE p.i > ? " +
                ")";

            final Query nativeQuery = entityManager.createNativeQuery(sqlString);
            nativeQuery.setParameter(1, limit);

            nativeQuery.executeUpdate();
        } catch (final Exception ex) {
            logger.error("Failed to truncate data [table=web.account_recent_search]", ex);
        }
    }

}
