# Pattern Builder and Hierarchies
[Builder pattern](https://en.wikipedia.org/wiki/Builder_pattern) is well known and widely used to 
make object construction cleaner. But in this post we are going to examine
less known feature - cleaner construction of class hierarchies. You can find usage examples and implementation details
in this repository.

## Why Builder?
Before we jump straight into the main point of the post let me give you a quick 
context. The builder considered here is different from the one described in
[Gang of Four book](https://www.amazon.com/Design-Patterns-Elements-Reusable-Object-Oriented/dp/0201633612/ref=pd_bxgy_b_text_y).
Our version is intended at removing complexity of multiple constructors and 
excessive use of setters while the original  version focuses on abstracting
object creation steps.

Let's assume that we need to design a communication layer in our system. For that
purpose we create Communication class containing a basic set of communication
properties. Although the set contains only the basic properties it could have a dozen 
of them.
```java
class Communication {
    private final Consumer consumer;    // required
    private String name;                // optional
    private Charset charset;            // optional
    private String host;                // optional
    private int prefetchCount;          // optional
    //...
}
```
How would you implement construction of such an object that? Since there is one field required we need to set
it in constructor:
```java
class Communication {
    private final Consumer consumer;    // required
    private String name;                // optional
    private Charset charset;            // optional
    private String host;                // optional
    private int prefetchCount;          // optional
    // ...
    Communication(Consumer consumer) {
        this.consumer = consumer;
    }
}
```
The rest combinations we can handle with various constructors:
```java
class Communication {
    private final Consumer consumer;    // required
    private String name;                // optional
    private Charset charset;            // optional
    private String host;                // optional
    private int prefetchCount;          // optional
    // ...
    Communication(Consumer consumer) {
        this.consumer = consumer;
    }
    Communication(Consumer consumer, String name) {
        this.consumer = consumer;
        this.name = name;
    }
    Communication(Consumer consumer, Charset charset) {
        this.consumer = consumer;
        this.charset = charset;
    }
    Communication(Consumer consumer, String name, Charset charset) {
        this.consumer = consumer;
        this.name = name;
        this.charset = charset;
    }
    Communication(Consumer consumer, String name, Charset charset, String host) {
        this.consumer = consumer;
        this.name = name;
        this.charset = charset;
        this.host = host;
    }
    // ...
}
```
or with setters:
```java
class Communication {
    private final Consumer consumer;    // required
    private String name;                // optional
    private Charset charset;            // optional
    private String host;                // optional
    private int prefetchCount;          // optional
    // ...
    Communication(Consumer consumer) {
        this.consumer = consumer;
    }
    void setName(String name) {
        this.name = name;
    }
    void setCharset(Charset charset) {
        this.charset = charset;
    }
    void setHost(String host) {
        this.host = host;
    }
    void setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
    }
}
```
These would work and it is not a big deal if there are a couple of fields.
But this approach doesn't scale well. Imagine 5-10 fields... As the number
grows, code tends to become less readable and harder to maintain.
   
It is also difficult for a client to construct such an object - too many
possibilities to do it in a wrong way. Which constructor should I use? If I
use constructor with a single Consumer argument, does it automatically
set defaults for other fields or I need to call setters?

## Introducing Builder
Fortunately, we can do better employing builder pattern:
```java
class Communication {
    private final Consumer consumer;
    private final String name;
    private final Charset charset;
    private final String host;
    private final int prefetchCount;

    private Communication(Builder builder) {
        consumer = builder.consumer;
        name = builder.name;
        charset = builder.charset;
        host = builder.host;
        prefetchCount = builder.prefetchCount; 
    }

    static class Builder {
        private final Consumer consumer; // required
        private String name;
        private Charset charset;
        private String host;
        private int prefetchCount;

        Builder(Consumer consumer) {
            this.consumer = consumer;
        }

        Builder name(String name) {
            this.name = name;
            return this;
        }

        Builder charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        Builder host(String host) {
            this.host = host;
            return this;
        }

        Builder prefetchCount(int prefetchCount) {
            this.prefetchCount = prefetchCount;
            return this;
        }
        
        Communication build() {
            return new Communication(this);
        }
    }
}
```
Constructor became private making instantiation of Communication class possible through `Builder` only.
At the same time, all fields became `final` making object immutable. Notice, that in `Builder` we pass
required arguments to constructor ensuring the are set. All the reset fields are set through setters
following [Fluent Interface](https://martinfowler.com/bliki/FluentInterface.html) idiom.

Check out possible client code. It is pretty straightforward, isn't it?
```java
class CommunicationClient {
    static Communication createCommunication() {
        Communication result = new Communication.Builder(new Consumer())
                .name("Network Communication")
                .charset(Charset.forName("UTF-8"))
                .host("localhost")
                .prefetchCount(10)
                .build();
        return  result;
    }
}
```
Sometimes, you can see `Builder` with private constructor and method `builder()` used
to create `Builder` object:
```java
class Communication {
    // ...
    static Builder builder() {
        return new Builder();
    } 
    
    static class Builder {
        private Builder(){} // private constructor
        // ...
    }
}
```
This way allows you to create anonymous builder. I have no idea why one may need that, but it is possible:
```java
class Communication {
    // ...
    static Builder builder() {
        return new Builder(){
            // override some Builder's methods
        };
    } 
    
    static class Builder {
        // ...
    }
}
```
In both last cases client creates Communication similarly:
```java
    static Communication createCommunication() {
        Communication result = Communication.bulder() // call to static method
                .build();
        return result;
    }
```
## Builder and Validation
Do you remember this feeling when you last time needed to throw an exception in a constructor?
Something like this:
```java
class Communication {
    private final Reader reader;
    
    Communication() throws IOException {
        reader = Files.newBufferedReader(Paths.get("./log"));    // throws IOException
    }
}
```
Probably, your good half was screaming "What a shame! It's a bad practice", while the bad half
whispering "It's OK, just quick and dirty, go ahead!" =) It is bad because constructor should always happen, it is
just for initialization. All complex construction logic you can incapsulate into builder:
```java
class Communication {
    private final Reader reader;
    
    private Communication(Builder builder) {
        reader = builder.reader; 
    }

    static class Builder {
        private int prefetchCount;
        private Reader reader;
                    
        Communication build() throws IOException{
            if (prefetchCount < 1) {
                return null;
            } 
            try{
                reader = Files.newBufferedReader(Paths.get("./log"));    // throws IOException
                return new Communication(this);    
            } catch(){}
            return null;
        }
    }
}
```
The statement `new Communication.Builder().build()` returns a valid object or null. In other words,
the pattern allows to include sophisticated validation logic into object creation process.
## Builder and Hierarchies
So far you've seen enough to believe that Builder pattern helps to avoid a need in multiple constructors.
Now we are going to examine how this property of the Builder helps to make construction of hierarchies 
cleaner. Assume that we have base interface shown below. The interface could model 
'topic communication' like the one where subscribers connected to a topic can send a message to the topic and consume all messages
sent to the topic.
```Java
public interface Communication {

    String getName();

    Consumer getConsumer();

    void send(byte[] bytes) throws Exception;

    void close() throws Exception;

    interface Consumer {
        void handleDelivery(byte[] bytes);
    }
}
```
We want to have 3 different implementations: in-memory, network and network text-based (when
we can send text instead of bytes). From the interface above, we can conclude that any
possible implementations should have a `name` and a `consumer`, so let's create a minimal 
implementation encapsulating these two properties and related boilerplate code:
```Java
public abstract class MinimalCommunication implements Communication {
    private final String name;
    private final Consumer consumer;

    protected MinimalCommunication(Builder builder) {   // protected, not private
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
```
Nothing special so far, except *abstract* `Builder`. That means this `Builder` can not
construct `Communication` object but can be used to set `name` and `consumer` which is exactly
our intention.

Now, let's subclass it and create in-memory communication which can be used, for instance,
to test a business logic without the need to establish real networking:
```java
public class InMemoryCommunication extends MinimalCommunication {
    private final int memoryBufferSize;

    private InMemoryCommunication(Builder builder) {
        super(builder);
        memoryBufferSize = builder.memoryBufferSize;
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
```
Here, we introduced a new property - `memoryBufferSize` (the size of a memory buffer
used to transmit messages). Note that `InMemoryCommunication` *extends* `MinimalCommunication`
and, which is more unusual, `Builder` *extends* `MinimalCommunication.Builder` so that
we can set properties of the superclass (`MinimalCommunication`) to the subclass (`InMemoryCommunication`):
```java
InMemoryCommunication.Builder builder = new InMemoryCommunication.Builder();
builder.memoryBufferSize(MEMORY_BUFFER_SIZE)
    .consumer(CONSUMER)     // superclass property
    .name(NAME);            // superclass property
InMemoryCommunication inMemoryCommunication = builder.build();
```
Cool, isn't it ?! But there is one tricky thing that you would've immediately noticed
if had tried to call the same methods in different order:
```java
InMemoryCommunication.Builder builder = new InMemoryCommunication.Builder()
    .consumer(CONSUMER)
    .memoryBufferSize(MEMORY_BUFFER_SIZE) // can't call this after the previous call
    .name(NAME)
    .build(); // can't call builder here, because it would build superclass but not InMemoryCommunication
```
Since `consumer()` is defined in superclass it returns `Builder` of the superclass which
has no `memoryBufferSize()` defined, so the consequent call to `memoryBufferSize()`
will not compile. By the same reason `name()` returns `Builder` of superclass which has *abstract* `build()`
and can not be called.

Meanwhile, we continue to grow our hierarchy and are about to implement network-based communication with some
standard properties like `host` and `port`:
```java
public class NetworkCommunication extends MinimalCommunication {
    private final String host;
    private final int port;
    private final Socket inputSocket;

    protected NetworkCommunication(Builder builder) {
        super(builder);
        host = builder.host;
        port = builder.port;
        inputSocket = builder.inputSocket;
    }

    public static class Builder extends MinimalCommunication.Builder {
        private String host;
        private int port;
        private Socket inputSocket;

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        @Override
        public NetworkCommunication build() {
            try {
                inputSocket = new Socket();
                InetSocketAddress address = new InetSocketAddress(host, port);
                inputSocket.bind(address);
                return new NetworkCommunication(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
```
And, finally, we derive `TextBasedCommunication` from `NetworkCommunication` by adding `charset`
property which allows us to work with chars the same way we work with bytes:
```java
public class TextBasedCommunication extends NetworkCommunication {
    private final Charset charset;

    protected TextBasedCommunication(Builder builder) {
        super(builder);
        charset = builder.charset;
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
            return new TextBasedCommunication(this); // Implicitly assume that creation of the parent happens only through constructor, not setters.
        }
    }
}
```
So far so good, but notice that in `build()` we call `super.build()` and ignore result...
Actually we are not interested in construction of object of the  superclass (`NetworkCommunication`)
at all, because we construct `TextBasedCommunication` in the next line. What we want with `super.build()`
is to initialize `NetworkCommunication.Builder` so when we pass the `Builder` to constructor
(`TextBasedCommunication(this)`) it correctly initializes parent:
```java
protected TextBasedCommunication(Builder builder) {
    super(builder);
    charset = builder.charset;
}
```
Here we implicitly assume that construction of the object is done through constructor only,
and no additional `init()` or setters are needed to be called. Sounds dodgy? But if you give
it a second thought you will find it ok because:
* it is normarl to call `super()` when you override is. For instance, if you override `void foo();`
then probably you want to call `super.foo()` in the new method:
```java
    @Override
    void foo() {
        super.foo();
    }
```
because client code expects certain behavior of original method in the new method.
* it is just normal to construct objects by calling its constructor without any additional tweaks.

To conclude, if a class you are designing requires more than N parameters (there is no exact number,
we use 3) to be supplied you might consider benefits of Builder pattern:
* Cleaner object construction and dealing with inheretance hierarchies
* Moving parameters validataion out of constructor and, at the same time, making it a necessary
step for object construction.

