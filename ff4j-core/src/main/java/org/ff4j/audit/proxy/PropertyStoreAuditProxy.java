package org.ff4j.audit.proxy;

import java.util.Map;
import java.util.Set;

import org.ff4j.FF4j;
import org.ff4j.audit.EventBuilder;
import org.ff4j.audit.EventConstants;
import org.ff4j.audit.EventPublisher;
import org.ff4j.property.Property;
import org.ff4j.property.store.PropertyStore;

/**
 * Implementation of audit on top of store.
 *
 * @author Cedrick Lunven (@clunven)
 */
public class PropertyStoreAuditProxy implements PropertyStore, EventConstants {

    /** Current FeatureStore. */
    private PropertyStore target = null;
    
    /** Reference. */
    private FF4j ff4j = null;
    
    /**
     * Only constructor.
     *
     * @param pTarget
     */
    public PropertyStoreAuditProxy(FF4j pFF4j, PropertyStore pTarget) {
        this.target = pTarget;
        this.ff4j   = pFF4j;
    }

    /** {@inheritDoc} */
    public  < T > void createProperty(Property<T> prop) {
        long start = System.nanoTime();
        target.createProperty(prop);
        long duration = System.nanoTime() - start;
        publish(builder(ACTION_CREATE)
                    .property(prop.getName())
                    .value(prop.asString())
                    .duration(duration));
    }

    /** {@inheritDoc} */
    public void updateProperty(String name, String newValue) {
        long start = System.nanoTime();
        target.updateProperty(name, newValue);
        long duration = System.nanoTime() - start;
        publish(builder(ACTION_UPDATE)
                    .property(name)
                    .value(newValue)
                    .duration(duration));
    }

    /** {@inheritDoc} */
    public <T> void updateProperty(Property<T> prop) {
        long start = System.nanoTime();
        target.updateProperty(prop);
        long duration = System.nanoTime() - start;
        publish(builder(ACTION_UPDATE)
                    .property(prop.getName())
                    .value(prop.asString())
                    .duration(duration));
    }

    /** {@inheritDoc} */
    public void deleteProperty(String name) {
        long start = System.nanoTime();
        target.deleteProperty(name);
        long duration = System.nanoTime() - start;
        publish(builder(ACTION_DELETE)
                    .property(name)
                    .duration(duration));
    }
    
    /** {@inheritDoc} */
    public boolean existProperty(String name) {
        return target.existProperty(name);
    }

    /** {@inheritDoc} */
    public Property<?> readProperty(String name) {
        return target.readProperty(name);
    }
    
    /** {@inheritDoc} */
    public Map<String, Property<?>> readAllProperties() {
        return target.readAllProperties();
    }

    /** {@inheritDoc} */
    public Set<String> listPropertyNames() {
        return target.listPropertyNames();
    }

    /** {@inheritDoc} */
    public boolean isEmpty() {
        return target.isEmpty();
    }

    /** {@inheritDoc} */
    public void clear() {
        long start = System.nanoTime();
        target.clear();
        long duration = System.nanoTime() - start;
        publish(builder(ACTION_CLEAR).type(TARGET_PSTORE)
                .name(ff4j.getPropertiesStore().getClass().getName())
                .duration(duration));
    }
    
    /**
     * Init a new builder;
     *
     * @return
     *      new builder
     */
    private EventBuilder builder(String action) {
        EventBuilder eb = new EventBuilder(ff4j);
        return eb.action(action);
    }
    
    /**
     * Publish target event to {@link EventPublisher}
     *
     * @param eb
     *      current builder
     */
    private void publish(EventBuilder eb) {
        ff4j.getEventPublisher().publish(eb.build());
    }

}
