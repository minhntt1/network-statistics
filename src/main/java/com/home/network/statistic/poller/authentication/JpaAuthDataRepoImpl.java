package com.home.network.statistic.poller.authentication;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class JpaAuthDataRepoImpl implements AuthDataRepo {
    private static final String findByClass = "select e from AuthData e where e.dataClass = :clazz";
    private final EntityManager em;

    public JpaAuthDataRepoImpl(@Qualifier("appEm") EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional
    public void upsert(AuthData authData) {
        if (authData.hasId()) {
            var persisted = em.find(AuthData.class, authData.getId(), LockModeType.PESSIMISTIC_WRITE);
            persisted.update(authData);
            return;
        }

        // if it has no id, save to db
        em.persist(authData);
    }

    @Override
    public List<AuthData> findByClass(String clazz) {
        return em.createQuery(findByClass, AuthData.class)
                .setParameter("clazz", clazz).getResultList();
    }
}
