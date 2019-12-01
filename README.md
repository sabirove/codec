## Codec utility



#### Overview
- [Features](#features)
- [Showcase](#showcase)
- [About](#about)
- [Codec function](#codec-function)
- [Codec filter](#codec-filter)
- [Codec API](#codec-api)
- [Obtaining a Codec](#obtaining-a-codec)
- [Predefined components](#predefined-components)
- [Java serialization codec function](#java-serialization-function)
- [Binary serialization codec function](#binary-serialization-function)
- [AES encryption codec filter](#aes-encryption-filter)
- [Noteworthy application examples](#noteworthy-application-examples)


#### Features
- Tiny library with no external dependencies (apart from `com.google.code.findbugs:jsr305:3.0.2` for API annotations)
- Based on the plain `java.io` APIs
- Composable design
- Features a bunch of predefined implementations
- Base for ad-hoc binary serialization implementation
- Streaming AES encryption filter with sensible configuration
- `UncheckedIOException` to keep the API clean


#### Showcase
Given an arbitrary pojo:
```java
class Person implements Serializable {
        final String name;
        final int age;
        //constructor, proper equals & hashCode...
}
```

Create and use the Codec like so:
```java
    @Test
    void testCodec() {
        final Codec<Person> personCodec =
                Codec.withFunction(CodecFunctions.javaSerializing(Person.class))
                        .withBuffer(CodecBufferSpec.withDefaultSize())
                        .withFilterChain(
                                CodecFilters.compressWithGzip(),
                                CodecFilters.encodeWithBase64Url()
                        ).build();

        Person person1 = new Person("Jack", 24);
        Person person2 = new Person("Sarah", 30);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (EncoderStream<Person> encoder = personCodec.wrap(bos)) {
            encoder.write(person1);
            encoder.write(person2);
        }

        byte[] bytes = bos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

        try (DecoderStream<Person> decoder = personCodec.wrap(bis)) {
            assertEquals(person1, decoder.read());
            assertEquals(person2, decoder.read());
        }
    }
```
Now `bytes` array contains data corresponding to the two `Person` objects serialized with standard java serialization
then Gzip compressed and finally base64-url encoded.  
Reversing read (decode) operation is straightforward granted we use the same `Codec` instance that was used 
to write the data.


#### About
Codec is a bidirectional IO function suitable to encode/decode values (streams of values) of the specific type
that works against good old `java.io` streams.  
In this context *encoding* = *serialization* of an object of type `<T>` against the `OutputStream`
and *decoding* = *deserialization* of such object from the `InputStream` having the contents previously encoded
by the same Codec.

Component-wise, 
[Codec](src/main/java/sabirove/codec/Codec.java) = [CodecFunction](src/main/java/sabirove/codec/function/CodecFunction.java) +
 [CodecFilter](src/main/java/sabirove/codec/filter/CodecFilter.java)  

And workflow-wise, `Codec` operations can be described as following: 
- Encoding: `T -> CodecFunction<T> -> CodecFilter1 -> CodecFilter2 -> ... CodecFilterN -> OutputStream`
- Decoding: `T <- CodecFunction<T> <- CodecFilter1 <- CodecFilter2 -> ... CodecFilterN <- InputStream`


#### Codec function
[CodecFunction](src/main/java/sabirove/codec/function/CodecFunction.java) is a bidirectional function that writes and
 reads back the `<T>` values against the `java.io` streams.
```java
public abstract class CodecFunction<T> {
    public abstract void write(T value, @WillNotClose OutputStream out) throws IOException;
    public abstract T read(@WillNotClose InputStream in) throws IOException;
}
```
This is the central piece of the `Codec` implementation handling the actual serialization/deserialization
with a pair of complementary functions.

#### Codec filter
[CodecFilter](src/main/java/sabirove/codec/filter/CodecFilter.java) is a pair of complementary 
`OutputStream/InputStream` wrappers in the single package that are used to apply extra filtering on top of
the target `java.io` streams (e.g. to apply compression/decompression or some sort of additional encoding/decoding, like Base64).
```java
public abstract class CodecFilter {
    public abstract OutputStream filter(@WillNotClose OutputStream out) throws IOException;
    public abstract InputStream filter(@WillNotClose InputStream in) throws IOException;
}
```
`CodecFilter`s can be composed with one another into the `filter chain`:
```java
public abstract class CodecFilter {
    public final CodecFilter chain(CodecFilter next);
    public static CodecFilter chain(CodecFilter... filters);
}
```
The invocation like `filter1.chain(filter2)` (or equivalent `CodecFilter.chain(filter1, filter2)`) will
yield the following filtering scheme:
- Encoding: `... -> CodecFilter1 -> CodecFilter2 -> OutputStream`
- Decoding: `... <- CodecFilter1 <- CodecFilter2 <- InputStream`


#### Codec API

[Codec](src/main/java/sabirove/codec/Codec.java) utility features two complementing pairs of APIs:

- **Single value oriented**

```java
    public final class Codec<T> {
        ...
        public byte[] encode(T value);
        public T decode(byte[] in);
    }
```
Used to encode/decode single values against the plain byte arrays: useful for single value operations or when it's
inconvenient to work against the `java.io` stream API.   
**Note**: the underlying implementation is suboptimal when it comes to consecutive IO on the large quantities of values,
because of the extra object allocations required per single operation: use stream-oriented API for such occasions if possible. 

- **Value stream oriented**

```java
    public final class Codec<T> {
        ...
        public EncoderStream<T> wrap(@WillNotClose OutputStream os);
        public DecoderStream<T> wrap(@WillNotClose InputStream is);
    }
```
These return [EncoderStream](src/main/java/sabirove/codec/EncoderStream.java)/[DecoderStream](src/main/java/sabirove/codec/DecoderStream.java) 
which are a pair of light `java.io` stream wrappers used to execute reads and writes against `<T>` values in a straightforward fashion:

```java
    public final class EncoderStream<T> implements AutoCloseable, Flushable {
        ...
        public void write(T input);
    }
    
    public final class DecoderStream<T> implements AutoCloseable {
        ...
        public T read();
    }
```


#### Obtaining a Codec

Codecs can be obtained by using the builder API: `Codec.withFunction(..)` 


#### Predefined components

[CodecFunctions](src/main/java/sabirove/codec/function/CodecFunctions.java):
- `CodecFunctions.javaSerializing(..)`: IO on pojo types with standard java serialization (works for any `Serializable` type)
- `CodecFunctions.binarySerializing(..)`: IO on pojo types with ad-hoc binary serialization
- `CodecFunctions.binary(..)`: IO on plain byte arrays of arbitrary size
- `CodecFunctions.binaryChunked(..)`: IO on plain byte arrays of fixed size
- `CodecFunctions.string(..)`: IO on strings

[CodecFilters](src/main/java/sabirove/codec/filter/CodecFilters.java):
- `CodecFilters.compressWithDeflate()`: apply `Deflate` compress/decompress on top of the target streams
- `CodecFilters.compressWithGzip()`: apply `Gzip` compress/decompress on top of the target streams
- `CodecFilters.encodeWithBase64()`: apply `Base64` encode/decode on top of the target streams
- `CodecFilters.encodeWithBase64Url()`: apply `Base64URL` encode/decode on top of the target streams
- `CodecFilters.encodeWithBase64Mime()`: apply `Base64MIME` encode/decode on top of the target streams
- `CodecFilters.encryptWithAes()`: apply `AES` encrypt/decrypt on top of the target streams


#### Java serialization function

Standard java serialization function works for any Serializable object. Use for applications where serialization 
performance and footprint size is not a concern. To lower the footprint apply compression or better yet
use binary serialization codec which yields much better performance and smallest possible footprint.


#### Binary serialization function
 
Ad-hoc binary IO function based on the custom `java.io` stream wrappers 
([StateInputBuffer](src/main/java/sabirove/codec/util/StateInputStream.java) /
 [StateOutputBuffer](src/main/java/sabirove/codec/util/StateOutputStream.java))
allowing to conveniently read/write most of the standard java types. Can serve as the base to implement ad-hoc
binary serialization. Supports IO with LEB128 variable-length encoded `int` and `long` values and uses unsigned
variable-length encoding to write enum ordinals and length values for the contiguous data types
(e.g. lists, arrays, strings) which helps to yield smallest footprint possible.
  
Limitations: 
  1. reading and writing of the fields should be carried out in the exact same order.
  2. writing null values is prohibited.
  3. no type information is stored and validated whatsoever.


#### AES encryption filter

Based on the strong AES encryption implemented on top of the the standard `java.security`/`java.crypto` components.  
Uses the streaming AES encryption method with sensible configuration and cryptographically strong random number generators.

Specs:
- Galois/Counter transformation mode (AES/GCM/NoPadding): streaming AES with data integrity validation
- 128 bit secret key: optimal/recommended key length
- 96 bit initialization vector: recommended/optimal, random value is generated per each encryption
- 128 bit authentication tag


#### Noteworthy application examples

Assemble the `Codec` with the following `filter chain`: serialization -> encryption -> base64/url encode:
```java
    final Codec<State> codec =
            Codec.withFunction(CodecFunctions.binarySerializing(State::write, State::read))
                    .withBuffer(CodecBufferSpec.withDefaultSize())
                    .withFilterChain(
                            CodecFilters.encryptWithAes(),
                            CodecFilters.encodeWithBase64Url()
                    ).build();

```
Such `Codec` can be used to encode some *sensitive* _state_ object into the encrypted/encoded string
that could be used to safely pass around on the web by means of, for instance, request query parameters.
This can be useful when implementing the two-way negotiation process (like user registration/confirmation 
or OAuth consent flows) which allows to achieve two major goals:
- stateless operation: no need to keep the state of the user request between the two consecutive steps of the process
- validating the authenticity of the following "return" request: payload can be properly decrypted only if it wasn't 
tampered with and only with your private key

*Hint*: binary serialization would help to keep the footprint small if using the query parameters.
