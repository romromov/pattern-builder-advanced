package patternbuilder.io;

import org.junit.Test;
import patternbuilder.core.Communication;

import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Roman Katerinenko
 */
public class BuilderTest {
    /**
     * {@link MinimalCommunication} parameters
     */
    private static final Communication.Consumer CONSUMER = null;
    private static final String NAME = "TestCommunication";
    /**
     * {@link InMemoryCommunication} parameters
     */
    private static final int MEMORY_BUFFER_SIZE = 1024; //bytes
    /**
     * {@link NetworkCommunication} parameters
     */
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 12346;
    /**
     * {@link TextBasedCommunication} parameters
     */
    private static final Charset CHARSET = Charset.forName("UTF-8");

    @Test
    public void checkInMemoryCommunication() throws Exception {
        InMemoryCommunication inMemoryCommunication = null;
        try {
            InMemoryCommunication.Builder builder = new InMemoryCommunication.Builder();
            builder.memoryBufferSize(MEMORY_BUFFER_SIZE)
                    .consumer(CONSUMER)
                    .name(NAME); // can't call builder here, because it would build MinimalCommunication not InMemoryCommunication
            inMemoryCommunication = builder.build();
            checkMinimalCommunication(inMemoryCommunication);
            checkInMemoryCommunication(inMemoryCommunication);
            inMemoryCommunication.close();
        } finally {
            if (inMemoryCommunication != null) {
                inMemoryCommunication.close();
            }
        }
    }

    @Test
    public void checkNetworkCommunication() throws Exception {
        NetworkCommunication networkCommunication = null;
        try {
            NetworkCommunication.Builder builder = new NetworkCommunication.Builder();
            builder.host(HOST)
                    .port(PORT)
                    .consumer(CONSUMER)
                    .name(NAME);
            networkCommunication = builder.build();
            checkMinimalCommunication(networkCommunication);
            checkNetworkCommunication(networkCommunication);
        } finally {
            if (networkCommunication != null) {
                networkCommunication.close();
            }
        }
    }

    @Test
    public void checkTextCommunication() throws Exception {
        TextBasedCommunication textCommunication = null;
        try {
            TextBasedCommunication.Builder builder = new TextBasedCommunication.Builder();
            builder.charset(CHARSET)
                    .host(HOST)
                    .port(PORT)
                    .consumer(CONSUMER)
                    .name(NAME);
            textCommunication = builder.build();
            checkMinimalCommunication(textCommunication);
            checkNetworkCommunication(textCommunication);
            checkTextCommunication(textCommunication);
        } finally {
            if (textCommunication != null) {
                textCommunication.close();
            }
        }
    }

    private void checkNetworkCommunication(NetworkCommunication networkCommunication) throws Exception {
        assertEquals(HOST, networkCommunication.getHost());
        assertEquals(PORT, networkCommunication.getPort());
        assertNotNull(networkCommunication.getInputSocket());
    }

    private void checkMinimalCommunication(MinimalCommunication minimalCommunication) throws Exception {
        assertEquals(CONSUMER, minimalCommunication.getConsumer());
        assertEquals(NAME, minimalCommunication.getName());
    }

    private void checkInMemoryCommunication(InMemoryCommunication inMemoryCommunication) throws Exception {
        assertEquals(MEMORY_BUFFER_SIZE, inMemoryCommunication.getMemoryBufferSize());
    }

    private void checkTextCommunication(TextBasedCommunication textBasedCommunication) throws Exception {
        assertEquals(CHARSET, textBasedCommunication.getCharset());
    }
}