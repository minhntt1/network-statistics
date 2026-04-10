package com.home.network.statistic.poller.authentication;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Profile({"dev-admin", "prd-admin", "dev-executor","prd-executor"})
@Component
public class JpaAuthDataRepoImpl implements AuthDataRepo {
	private static final String findByClass = "select e from AuthData e where e.dataClass = :clazz";
    private static final String findAll = "select e from AuthData e";
    private static final String countByClass = "select count(e) from AuthData e";

    private final EntityManager em;

    public JpaAuthDataRepoImpl(@Qualifier("appEm") EntityManager em) {
        this.em = em;
    }

    @Override
    public void upsert(AuthData authData) {
        if (authData.checkId()) {
            var persisted = em.find(AuthData.class, authData.getId(), LockModeType.PESSIMISTIC_WRITE);
            persisted.update(authData);
            return;
        }

        em.persist(authData);
    }

    public Optional<AuthData> findById(Integer id) {
        return Optional.ofNullable(em.find(AuthData.class, id));
    }

    @Override
    public List<AuthData> findAll(Integer page, Integer limit) {
        return em.createQuery(findAll, AuthData.class)
                .setFirstResult((page - 1) * limit)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public boolean deleteById(Integer id) {
        var entity = em.find(AuthData.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (entity == null) return false;
        em.remove(entity);
        return true;
    }

    @Override
	public long countResultAll() {
		return (long)em.createQuery(countByClass).getSingleResult();
	}

    @Override
    public List<AuthData> findByClass(String clazz) {
        return em.createQuery(findByClass, AuthData.class)
                .setParameter("clazz", clazz)
                .getResultList();
    }

    @Override
	public List<AuthData> findByClassForUpdate(String clazz) {
		return em.createQuery(findByClass, AuthData.class)
                .setParameter("clazz", clazz)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();
	}
}
