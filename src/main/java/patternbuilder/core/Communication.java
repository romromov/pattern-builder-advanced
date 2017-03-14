package patternbuilder.core;

/**
 * @author Roman Katerinenko
 */
public interface Communication {

    String getName();

    Consumer getConsumer();

    void send(byte[] bytes) throws Exception;

    void close() throws Exception;

    interface Consumer {
        void handleDelivery(byte[] bytes);
    }

}