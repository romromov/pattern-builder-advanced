package patternbuilder.io;

/**
 * @author Roman Katerinenko
 */
public class InMemoryCommunication extends MinimalCommunication {
    private final int memoryBufferSize;

    private InMemoryCommunication(Builder builder) {
        super(builder);
        memoryBufferSize = builder.memoryBufferSize;
    }

    @Override
    public void send(byte[] bytes) throws Exception {
        if (bytes.length > memoryBufferSize) {
            throw new IllegalStateException("Too big message");
        }
        getConsumer().handleDelivery(bytes);
    }

    @Override
    public void close() throws Exception {
    }

    public int getMemoryBufferSize() {
        return memoryBufferSize;
    }

    public static class Builder extends MinimalCommunication.Builder {
        private int memoryBufferSize;

        public Builder memoryBufferSize(int memoryBufferSize) {
            this.memoryBufferSize = memoryBufferSize;
            return this;
        }

        @Override
        public InMemoryCommunication build() {
            return new InMemoryCommunication(this);
        }
    }
}