package jpa.eclipselink.test.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jpa.eclipselink.test.domain.Human;
import jpa.eclipselink.test.domain.SuperHero;

public class LeftJoinBugTest {

    EntityManagerFactory emf;
    EntityManager em;

    @Before
    public void before() {
        // (a) create an entity manager to HSQL
        emf = Persistence.createEntityManagerFactory("eclipseLinkPersistenceUnit");
        em = emf.createEntityManager();

        // (b) begin a transaction
        em.getTransaction().begin();

        // (c) populate the DB so that we have something to query to proove the bug
        populateDbForTest();

    }

    /**
     * Populate the DB with some dummy entities
     * <P>
     * (1) Batman <br>
     * ---- (2) BatmanChild <br>
     * (1) SpiderMan <br>
     * (1) RegularDaddy <br>
     * ---- (2) RegularChild
     */
    private void populateDbForTest() {
        // Level 1
        SuperHero batman = new SuperHero("Batmam");
        em.persist(batman);

        SuperHero spiderMan = new SuperHero("SpiderMan");
        em.persist(spiderMan);

        Human regularDaddy = new Human("RegularDaddy");
        em.persist(regularDaddy);

        // Level 2
        Human batmanChild = new Human("BatmanChild");
        batmanChild.setParent(batman);
        em.persist(batmanChild);

        Human regularChild = new Human("RegularChild");
        regularChild.setParent(regularDaddy);
        em.persist(regularChild);
        em.flush();
    }

    @After
    public void after() {
        // (a) rollback the transaction
        em.getTransaction().rollback();

    }

    @Test
    public void placeboQueryNoLeftJoinTreat() {
        // (a) prepare the JPQL query
        String jpql = "SELECT h " //
                // FROM :
                // Query all humans, and their parents. If a human does not have a preant
                // this is not a problem because we are left joining
                // we expect all results returned
                + " FROM Human h " //
                + " LEFT JOIN h.parent p " //
                // Where predicate is irrelevant in our case
                + " WHERE " + " 1 =1 ";
        TypedQuery<Human> typedQuery = em.createQuery(jpql, Human.class);

        // (b) execute the query
        List<Human> allHumans = typedQuery.getResultList();
        Assert.assertEquals(5, allHumans.size());

        System.out.println(
                "In this case all humans were returned, because our LEFT JOIN was essentially just there to proove the "
                        + " point that an entity without a parent is still returned. In particular our RegularDaddy, Batmam and SpiderMan  are part of the result list. ");
    }

    @Test
    public void leftJoinTreatQueryBugTest() {
        // (a) prepare the JPQL query
        String jpql = "SELECT h " //
                // FROM :
                // Query all humans, that either have no parent
                // or whose parent is SuperHero or a some sub-type of super-hero
                // we would expect:
                // (1) batman, SpiderMan and regularDaddy returned - these have no parent
                // so left JOIN is null
                // (2) BatmanChild since it has a parent and his parent is a super-hero.
                // PROBLEM:
                // THe bug here is that JPQL compilation will put into the query a
                // CLASS_TYPE = 'SuperHero', thus killing the left join and turning it into a join
                // as a result the only human returned is BatmanChild
                + " FROM Human h " //
                + " LEFT JOIN TREAT (h.parent AS SuperHero) p " //
                // Where predicate is irrelevant in our case
                + " WHERE " + " 1 =1 ";
        TypedQuery<Human> typedQuery = em.createQuery(jpql, Human.class);

        // (b) execute the query
        List<Human> result = typedQuery.getResultList();
        for (Human obtainedHuman : result) {
            System.out.println("THe Human we have in the BUGY left join query  is the: " + obtainedHuman.getName());
        }
        Assert.assertEquals(4, result.size());

    }

}
