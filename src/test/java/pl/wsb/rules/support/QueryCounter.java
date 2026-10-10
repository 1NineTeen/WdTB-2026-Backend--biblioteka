package pl.wsb.rules.support;

import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

/**
 * Liczy zapytania SQL wysłane przez Hibernate. Służy do łapania problemu N+1 w testach @DataJpaTest.
 *
 * <pre>{@code
 * long queries = QueryCounter.count(em, () ->
 *         offerRepository.findAllWithRestaurant().forEach(o -> o.getRestaurant().getName()));
 * assertThat(queries).isEqualTo(1);
 * }</pre>
 */
public final class QueryCounter {

    private QueryCounter() {
    }

    public static long count(EntityManager entityManager, Runnable action) {
        entityManager.flush();
        entityManager.clear();
        Statistics statistics = entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class)
                .getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
        action.run();
        return statistics.getPrepareStatementCount();
    }
}
