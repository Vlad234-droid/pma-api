package com.tesco.pma.validation;

/**
 * Used to implement different validation rules depending on use cases.
 *
 * @see <a href="https://docs.jboss.org/hibernate/validator/6.1/reference/en-US/html_single/#chapter-groups">Grouping constraints</a>
 */
public interface ValidationGroup {
    interface OnCreate extends WithoutId {
    }

    interface OnUpdate extends WithId {
    }

    interface WithId {
    }

    interface WithoutId {
    }
}