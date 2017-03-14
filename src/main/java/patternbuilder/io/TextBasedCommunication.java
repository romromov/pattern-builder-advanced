package patternbuilder.io;

import java.nio.charset.Charset;

/**
 * @author Roman Katerinenko
 */
public class TextBasedCommunication extends NetworkCommunication {
    private final Charset charset;

    protected TextBasedCommunication(Builder builder) {
        super(builder);
        charset = builder.charset;
    }

    public Charset getCharset() {
        return charset;
    }

    public void send(String string) throws Exception {
        send(string.getBytes(getCharset()));
    }

    public static class Builder extends NetworkCommunication.Builder {
        private Charset charset;

        public Builder charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        @Override
        public TextBasedCommunication build() { // Note! we call super.build() to initialize parent.
            super.build(); // ignore result
            return new TextBasedCommunication(this); // implicitely assume that creation of the parent happens only through constructor, not setters. Builder is only for creation, not for anything else!
        }
    }
}
