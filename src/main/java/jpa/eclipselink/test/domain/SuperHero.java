package jpa.eclipselink.test.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * A super-hero is a special type of Human and we want to use it in a TREAT query to demonstrate the bug.
 */
@DiscriminatorValue("SuperHero")
@Entity
public class SuperHero extends Human {

    /**
     * Jpql consutrctor
     *
     */
    public SuperHero() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Create a new SuperHero.
     *
     * @param name
     */
    public SuperHero(String name) {
        super(name);
    }

}
