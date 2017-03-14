package patternbuilder.io;

import patternbuilder.core.Communication;

/**
 * Contains boilerplate initialization code for {@code name} and {@code consumer}
 *
 * @author Roman Katerinenko
 */
public abstract class MinimalCommunication implements Communication {
    private final String name;
    private final Consumer consumer;

    protected MinimalCommunication(Builder builder) {   // protected
        name = builder.name;
        consumer = builder.consumer;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Consumer getConsumer() {
        return consumer;
    }

    public static abstract class Builder {
        private Communication.Consumer consumer;
        private String name;

        public abstract MinimalCommunication build();

        public Builder consumer(Communication.Consumer consumer) {
            this.consumer = consumer;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

    }
}