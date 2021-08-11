package com.tesco.pma.event;

public enum EventPriority {

    HIGHEST(9),
    HIGH_H(8),
    HIGH(7),
    HIGH_L(6),
    NORMAL_H(5),
    NORMAL(4),
    NORMAL_L(3),
    LOW_H(2),
    LOW(1),
    LOW_L(0),
    LOWEST(0);

    private int id;

    EventPriority(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * Returns an event by id or default NORMAL
     * @param id priority identifier
     * @return event priority
     */
    public static EventPriority getById(int id) {
        for (EventPriority value : values()) {
            if (value.id == id) {
                return value;
            }
        }
        return getDefaultPriority();
    }

    /**
     * Returns an event by the name or default NORMAL
     * @param name event name
     * @return event priority
     */
    public static EventPriority getByName(String name) {
        for (EventPriority value : values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return getDefaultPriority();
    }

    public static EventPriority getDefaultPriority() {
        return NORMAL;
    }

    public static EventPriority getLessPriority(EventPriority priority) {
        int id = priority.getId();
        return priority == LOW_L || priority == LOWEST ? priority : getById(--id);
    }

    public static EventPriority getGreaterPriority(EventPriority priority) {
        int id = priority.getId();
        return priority == HIGHEST ? priority : getById(++id);
    }
}
