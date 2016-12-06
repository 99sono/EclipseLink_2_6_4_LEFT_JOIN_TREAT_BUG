package jpa.eclipselink.test.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

/**
 * This entity has no JSR annotations. Eclipse link will not have the intelligence of notince that the parent class does
 * have and so the blowup will happen on the DB not null field itself.
 */
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "CLASS_TYPE", length = 32)
@DiscriminatorValue("Human")
@Entity
public class Human extends GenericEntity {

    /**
     * The name of the human
     */
    @NotNull
    @Column(name = "HUMAN_NAME")
    String name;

    /**
     * The parent of the human. Not all Humans need to have a parent. E.g. Adam and Even are parentless.
     */
    @ManyToOne()
    Human parent;

    /**
     * JPQL constructor
     */
    public Human() {
        super();
    }

    /**
     * Create a new Human.
     *
     * @param name
     */
    public Human(String name) {
        super();
        this.name = name;
    }

    /**
     * The children of the human.
     */
    @OneToMany(mappedBy = "parent")
    final List<Human> children = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Human getParent() {
        return parent;
    }

    /**
     * Speicy the parent of the child node. This api takes care of registering the child in the parent.
     *
     * @param parent
     *            the human that becomes the parent of the current child
     */
    public void setParent(Human parent) {
        // (a) trivial case - nothing to do
        // the relationship is already set
        if (this.parent == parent) {
            return;
        }

        // (b) our node stops being a child of its parent
        // we have to update the child node list on the parent entity
        // to not have it become in-memory-stale in the sever cahce
        if (this.parent != null) {
            this.parent.children.remove(this);
        }

        // (c) we can now update our parent to whatever we have been given as input
        // our bi-directional relationship is kep clean
        this.parent = parent;
        this.parent.addChild(this);
    }

    public List<Human> getChildren() {
        return Collections.unmodifiableList(children);
    }

    // Low level apis that if imporperly used can break the bi-directional relationship between parent and child
    /**
     * Do not invoke this APi. Establish a parent-child relationship by invoking the setParent on the child node.
     *
     * @param child
     *            The child node to be added to the parent node.
     */
    private void addChild(Human child) {
        children.add(child);
    }

    /**
     * Remove a child from the current parent node. This API is not to be invoked directly. Only the setParent public
     * APi is allowed.
     *
     * @param child
     *            child that stops belonging to the current parent
     */
    private void removeChild(Human child) {
        children.remove(child);
    }

}
