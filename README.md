[![Build Status](https://travis-ci.org/sabirove/codec.svg?branch=master)](https://travis-ci.org/sabirove/codec)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Codec utility

- Small library with no external dependencies (apart from `com.google.code.findbugs:jsr305` for API annotations)
- Plain `java.io` API based
- Composable design
- A bunch of out-of-the-box implementations
- Solid base for ad-hoc binary serialization
- Streaming AES encryption with data integrity validation
- Leveraging `UncheckedIOException` to keep the core API clean


#### Showcase
Given an arbitrary pojo:
```java
class Person implements Serializable {
        final String name;
        final int age;
        //constructor, equals & hashCode...
}
```

Create and use the `Codec` like so:
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
Now `bytes` array contains data corresponding to the two `Person` objects serialized then Gzip compressed
and finally base64/url encoded.  
Reversing read (decode) operation is straightforward granted we use the same `Codec` instance (or similarly build one)
that was used to write the data.


#### Overview
- [About](#about)
- [Codec function](#codec-function)
- [Codec filter](#codec-filter)
- [Codec API](#codec-api)
- [IO buffering](#io-buffering)
- [Serialization functions](#serialization-functions)
- [AES encryption filter](#aes-encryption-filter)
- [Noteworthy application examples](#noteworthy-application-examples)


#### About
`Codec` is a bidirectional IO function suitable to encode/decode single values and streams of values of the specific
type operating on top of the `java.io` streams.  
In this context, *encoding* an object of type `<T>` means *serializing* it against an `OutputStream`
and so *decoding* such object means *deserializing* it against an `InputStream` holding the *encoded* contents.

Component-wise, 
[Codec](src/main/java/sabirove/codec/Codec.java) = [CodecFunction](src/main/java/sabirove/codec/function/CodecFunction.java) +
 [CodecFilter](src/main/java/sabirove/codec/filter/CodecFilter.java)  

while `Codec` operations can be described as following: 
- Encoding: `T -> CodecFunction<T> -> CodecFilter1 -> CodecFilter2 -> ... CodecFilterN -> OutputStream`
- Decoding: `T <- CodecFunction<T> <- CodecFilter1 <- CodecFilter2 <- ... CodecFilterN <- InputStream`


#### Codec function
[CodecFunction](src/main/java/sabirove/codec/function/CodecFunction.java) is a bidirectional serialization function
that writes and reads back the `<T>` values against the `java.io` streams with a pair of complementary functions:
```java
    public abstract void write(T value, OutputStream out) throws IOException;
    public abstract T read(InputStream in) throws IOException;
```
The number of predefined `CodecFunction` implementations can be obtained with 
[CodecFunctions](src/main/java/sabirove/codec/function/CodecFunctions.java) factory, they are:
- `CodecFunctions.javaSerializing(..)`: IO on pojo types with standard java serialization (works for any `Serializable` type)
- `CodecFunctions.binarySerializing(..)`: IO on pojo types with ad-hoc binary serialization
- `CodecFunctions.binary(..)`: IO on plain byte arrays of arbitrary size
- `CodecFunctions.binaryChunked(..)`: IO on plain byte arrays of fixed size
- `CodecFunctions.string(..)`: IO on strings



#### Codec filter
[CodecFilter](src/main/java/sabirove/codec/filter/CodecFilter.java) is a pair of complementary 
`OutputStream/InputStream` wrappers in the single package that are used to apply extra filtering on top of
the target `java.io` streams (e.g. to apply compression/decompression or some sort of additional encoding/decoding, like Base64).
```java
    public abstract OutputStream filter(OutputStream out) throws IOException;
    public abstract InputStream filter(InputStream in) throws IOException;
```
`CodecFilter`s can be composed with one another into the `filter chain`:
```java
    public final CodecFilter chain(CodecFilter next);
    public static CodecFilter chain(CodecFilter... filters);
```
Invocation like `filter1.chain(filter2)` (or equivalent `CodecFilter.chain(filter1, filter2)`) yields the following
filtering scheme:
- Encoding: `... -> CodecFilter1 -> CodecFilter2 -> OutputStream`
- Decoding: `... <- CodecFilter1 <- CodecFilter2 <- InputStream`

The number of predefined `CodecFilter` implementations can be obtained with 
[CodecFilters](src/main/java/sabirove/codec/filter/CodecFilters.java) factory, they are:
- `CodecFilters.compressWithDeflate()`: apply `Deflate` compress/decompress on top of the target streams
- `CodecFilters.compressWithGzip()`: apply `Gzip` compress/decompress on top of the target streams
- `CodecFilters.encodeWithBase64()`: apply `Base64` encode/decode on top of the target streams
- `CodecFilters.encodeWithBase64Url()`: apply `Base64URL` encode/decode on top of the target streams
- `CodecFilters.encodeWithBase64Mime()`: apply `Base64MIME` encode/decode on top of the target streams
- `CodecFilters.encryptWithAes()`: apply `AES` encrypt/decrypt on top of the target streams

#### Codec API

[Codec](src/main/java/sabirove/codec/Codec.java) utility features two kinds of API:

- **Single value oriented**

```java
    public byte[] encode(T value);
    public T decode(byte[] in);
```
Used to encode/decode values against plain byte arrays: useful for single value operations or when it's not convenient
to work against the `java.io` stream API.   
**Note**: the underlying implementation is suboptimal when it comes to IO on the large quantities of values because of
the extra object allocations required per single operation: use stream-oriented API for such occasions if possible. 

- **Value stream oriented**

```java
    public EncoderStream<T> wrap(OutputStream os);
    public DecoderStream<T> wrap(InputStream is);
```
These wrap provided `java.io` streams with
[EncoderStream](src/main/java/sabirove/codec/EncoderStream.java)/[DecoderStream](src/main/java/sabirove/codec/DecoderStream.java) 
which are a pair of light wrappers used to execute reads and writes against the underlying streams in a straightforward fashion:

```java
    public final class EncoderStream<T> implements AutoCloseable, Flushable {
        public void write(T input);
    }
    
    public final class DecoderStream<T> implements AutoCloseable {
        public T read();
    }
```
Codecs can be obtained (built) by using the builder API: `Codec.withFunction(..)`

#### IO buffering

IO buffering is addressed with special kind of `CodecFilter`: [CodecBufferSpec](src/main/java/sabirove/codec/filter/CodecBufferSpec.java),
which is used as an *edge* filter applying the `BufferedInputStream/BufferedOutputStream` wrappers on top of the
provided streams.  
`CodecBufferSpec` can be instructed with specific target types of `InputStream/OutputStream` to exclude from buffer
application by means of `CodecBufferSpec.withXXXStreamExclusions(..)` APIs.  

Obtain with factory `CodecBufferSpec.ofSize(..)` specifying the input/output buffer sizes (in bytes) or opt in for
default sized one with `CodecBufferSpec.ofDefaultSize()`.

*NOTE*:
This filter is intended for use with Codec builder API mentioned above and is not meant to be used as the standalone
filter within the filter chain. 
 
#### Serialization functions

Two of the most notable `CodecFunction` implementations provided out-of-the-box are:

##### Java serialization function
Based on the standard java serialization that works for any Serializable object: use for applications where
serialization performance and footprint size is not of a major concern.  
To lower the footprint apply compression or better yet use binary serialization function which yields much
better performance and smallest possible footprint.  

Obtain with: `CodecFunctions.javaSerializing(..)` providing the target `Class`.

##### Binary serialization function 
This one is based on the pair of custom `java.io` stream wrappers:
[StateInputBuffer](src/main/java/sabirove/codec/util/StateInputStream.java) /
[StateOutputBuffer](src/main/java/sabirove/codec/util/StateOutputStream.java)
which provide convenient API to read and write most of the standard Java types with relative ease,
including collections, maps, arrays, strings, enums as well as some `java.time` and `java.math` data types.  
Supports IO with LEB128 variable-length encoded `int` and `long` values and uses unsigned variable-length
ints to serialize enum ordinals and length values for the contiguous data types (e.g. collections, arrays, strings)
which helps to yield the smallest footprint possible.  
Can serve as the base for ad-hoc binary serialization implementation.

Obtain with: `CodecFunctions.binarySerializing(..)` providing a pair of functions for reading and writing the 
target type, e.g such `CodecFunction` for aforementioned `Person` pojo would look like:
```java
    CodecFunction<Person> personCodecFunction = CodecFunctions.binarySerializing(
            (sos, person) -> sos.putString(person.name).putInt(person.age),
            sis -> new Person(sis.getString(), sis.getInt())
    );

```
Limitations: 
  1. reading and writing of the fields should be carried out in the exact same order.
  2. writing null values is prohibited.
  3. no type information is stored and validated whatsoever.


#### AES encryption filter

Encryption filter that is based on the strong streaming AES encryption method with data integrity validation
and cryptographically strong random number generators.
Sensibly configured. Implemented on top of the the standard library (`java.security`/`java.crypto`) components.

Obtain with `CodecFilters.encryptWithAes(..)` providing the `16 byte` secret key (or opt in for the random generated one).

Specs:
- Galois/Counter transformation mode (AES/GCM/NoPadding): streaming AES with data integrity validation
- 128 bit secret key: optimal key length
- 96 bit initialization vector: optimal length, random value is generated per each encryption
- 128 bit authentication tag


#### Noteworthy application examples

Assemble the `Codec` with the following `filter chain`: serialization -> encryption -> base64/url encode:
```java
    final Codec<State> codec =
            Codec.withFunction(CodecFunctions.binarySerializing(State::write, State::read))
                    .withBuffer(CodecBufferSpec.withDefaultSize())
                    .withFilterChain(
                            CodecFilters.encryptWithAes(mySecretKey),
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

*Hint*: binary serialization helps keeping the footprint small which is useful when working with query parameters.
