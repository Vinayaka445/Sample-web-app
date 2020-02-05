package com.kfwebstandard.jpacontroller;

import com.kfwebstandard.entities.Fish;
import com.kfwebstandard.exceptions.NonexistentEntityException;
import com.kfwebstandard.exceptions.RollbackFailureException;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kfogel
 */
@Named
@SessionScoped
public class FishActionBeanJPA implements Serializable {

    private final static Logger LOG = LoggerFactory.getLogger(FishActionBeanJPA.class);

    @Resource
    private UserTransaction utx;

    @PersistenceContext(unitName = "fishiesPU")
    private EntityManager em;

    public FishActionBeanJPA() {
    }

    public void create(Fish fish) throws RollbackFailureException {
        try {
            utx.begin();
            em.persist(fish);
            utx.commit();
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            try {
                utx.rollback();
                LOG.error("Rollback");
            } catch (IllegalStateException | SecurityException | SystemException re) {
                LOG.error("Rollback2");

                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
        }
    }

    public void edit(Fish fish) throws NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            fish = em.merge(fish);
            utx.commit();
        } catch (IllegalStateException | SecurityException | HeuristicMixedException | HeuristicRollbackException | NotSupportedException | RollbackException | SystemException ex) {
            try {
                utx.rollback();
            } catch (IllegalStateException | SecurityException | SystemException re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = fish.getId();
                if (findFish(id) == null) {
                    throw new NonexistentEntityException("The fish with id " + id + " no longer exists.");
                }
            }
            throw ex;
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            Fish fish;
            try {
                fish = em.getReference(Fish.class, id);
                fish.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The fish with id " + id + " no longer exists.", enfe);
            }
            em.remove(fish);
            utx.commit();
        } catch (NonexistentEntityException | IllegalStateException | SecurityException | HeuristicMixedException | HeuristicRollbackException | NotSupportedException | RollbackException | SystemException ex) {
            try {
                utx.rollback();
            } catch (IllegalStateException | SecurityException | SystemException re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        }
    }

    public List<Fish> findFishEntities() {
        LOG.info("getting all the fish");
        return findFishEntities(true, -1, -1);
    }

    public List<Fish> findFishEntities(int maxResults, int firstResult) {
        return findFishEntities(false, maxResults, firstResult);
    }

    private List<Fish> findFishEntities(boolean all, int maxResults, int firstResult) {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Fish.class));
        Query q = em.createQuery(cq);
        if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
        }
        return q.getResultList();
    }

    public Fish findFish(Integer id) {
        return em.find(Fish.class, id);
    }

    public int getFishCount() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<Fish> rt = cq.from(Fish.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}
