[![Build Status](https://travis-ci.org/sabirove/codec.svg?branch=master)](https://travis-ci.org/sabirove/codec)
[![Coverage Status](https://coveralls.io/repos/github/sabirove/codec/badge.svg)](https://coveralls.io/github/sabirove/codec)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

## Codec utility

- Small library with no external dependencies
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

Create and use a `Codec` like so:
```java
    @Test
    void testCodec() {
        final Codec<Person> personCodec =
                CodecBuilder.withFunction(CodecFunctions.javaSerializing(Person.class))
                        .withBuffer(CodecBufferSpec.ofDefaultSize())
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
Now `bytes` array contains two `Person` objects serialized then Gzip compressed and finally base64/url encoded.  
Reversing read (decode) operation is straightforward granted we use the same `Codec` instance (or similarly build one)
that was used to write the data.


#### Overview
- [About](#about)
- [Codec function](#codec-function)
- [Codec filter](#codec-filter)
- [API](#api)
- [Buffering](#buffering)
- [Serialization functions](#serialization-functions)
- [AES encryption filter](#aes-encryption-filter)
- [Noteworthy application examples](#noteworthy-application-examples)


#### About
[Codec](src/main/java/com/github/sabirove/codec/Codec.java) is a bidirectional IO function suitable to encode and decode single values or streams of values of the specific
type operating on top of the `java.io` streams.  
*Encoding* an object of type `<T>` means *serializing* it against an `OutputStream` while  *decoding* is a complementary
operation of *deserializing* such object against an `InputStream` holding the encoded contents.

`Codec` consists of [CodecFunction](#codec-function) and zero to many [CodecFilter](#codec-filter)s (including an optional 
[CodecBufferSpec](#buffering)) forming the following processing pipeline:
* Encoding: `T -> CodecFunction<T> -> CodecFilter1 -> CodecFilter2 -> ... CodecFilterN -> BufferedOutputStream (optional) -> outbound OutputStream`  
* Decoding: `T <- CodecFunction<T> <- CodecFilter1 <- CodecFilter2 <- ... CodecFilterN <- BufferedInputStream (optional) <- inbound InputStream`

In the simplest case `Codec` consists of just a [CodecFunction](#codec-function) and is used to serialize and 
deserialize objects without any extra byte stream transformations.  
`Codec` instances are obtained using the builder API: `CodecBuilder.withFunction(..)`

#### Codec function
[CodecFunction](src/main/java/com/github/sabirove/codec/function/CodecFunction.java) is a bidirectional serialization function
working against `java.io` streams to write and read back `<T>` values with a pair of complementary functions:
```java
    public abstract void write(T value, OutputStream out) throws IOException;
    public abstract T read(InputStream in) throws IOException;
```
The number of predefined `CodecFunction` implementations can be obtained with 
[CodecFunctions](src/main/java/com/github/sabirove/codec/function/CodecFunctions.java) factory, they are:
- `CodecFunctions.javaSerializing(..)`: IO on pojo types with standard java serialization (works for any `Serializable` type)
- `CodecFunctions.binarySerializing(..)`: IO on pojo types with ad-hoc binary serialization
- `CodecFunctions.binary(..)`: IO on plain byte arrays of arbitrary size
- `CodecFunctions.binaryChunked(..)`: IO on plain byte arrays of fixed size (with optional size "strictness")
- `CodecFunctions.string(..)`: IO on strings

#### Codec filter
[CodecFilter](src/main/java/com/github/sabirove/codec/filter/CodecFilter.java) is a pair of complementary 
`OutputStream/InputStream` wrappers in the single package that is used to apply extra filtering on top of
the `java.io` streams (e.g. apply compression/decompression or some sort of additional encoding/decoding).
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
- Encoding: `... -> CodecFilter1 -> CodecFilter2 -> outbound OutputStream`
- Decoding: `... <- CodecFilter1 <- CodecFilter2 <- inbound InputStream`

The number of predefined `CodecFilter` implementations can be obtained with 
[CodecFilters](src/main/java/com/github/sabirove/codec/filter/CodecFilters.java) factory, they are:
- `CodecFilters.compressWithDeflate()`: apply `Deflate` compress/decompress
- `CodecFilters.compressWithGzip()`: apply `Gzip` compress/decompress
- `CodecFilters.encodeWithBase64()`: apply `Base64` encode/decode
- `CodecFilters.encodeWithBase64Url()`: apply `Base64URL` encode/decode
- `CodecFilters.encodeWithBase64Mime()`: apply `Base64MIME` encode/decode
- `CodecFilters.encryptWithAes()`: apply `AES` encrypt/decrypt

#### API

[Codec](src/main/java/com/github/sabirove/codec/Codec.java) features two flavours of API:

- **Operating on single values**

```java
    public byte[] encode(T value);
    public T decode(byte[] in);
```
Used to encode/decode values against plain byte arrays: useful for single value operations or when it's not convenient
to work with `java.io` stream API.   
**Note**: default implementation is suboptimal when it comes to IO on large quantities of values because of
the extra object allocations per single operation: use stream-oriented API for such occasions if possible. 

- **Operating on streams of values**

```java
    public EncoderStream<T> wrap(OutputStream os);
    public DecoderStream<T> wrap(InputStream is);
```
Where [EncoderStream](src/main/java/com/github/sabirove/codec/EncoderStream.java)/[DecoderStream](src/main/java/com/github/sabirove/codec/DecoderStream.java) 
are a pair of light `java.io` stream wrappers used to execute reads and writes against the underlying streams in a straightforward fashion:

```java
    public final class EncoderStream<T> implements AutoCloseable, Flushable {
        public void write(T input);
    }
    
    public final class DecoderStream<T> implements AutoCloseable {
        public T read();
    }
```

#### Buffering

IO buffering is addressed with [CodecBufferSpec](src/main/java/com.github.sabirove/codec/filter/CodecBufferSpec.java) 
which is a special kind of `CodecFilter` placed on the *outer edge* of the filter chain to apply
`BufferedInputStream/BufferedOutputStream` wrappers on top of the inbound/outbound streams.  
Obtain with factory `CodecBufferSpec.ofSize(..)` specifying the input/output buffer sizes in bytes or opt in for the
default sized one with `CodecBufferSpec.ofDefaultSize()`.  
Certain kinds of `Input/Output -Stream` types can be excluded
from buffering application by means of `.withXXXStreamExclusions(..)` or `.addXXXStreamExclusions(..)` instance calls.

#### Serialization functions

Two of the most notable `CodecFunction` implementations provided out-of-the-box are:

##### Java serialization function
Standard java serialization function that works for any Serializable object. Use for applications where
serialization performance and footprint size is not of a major concern.  
To lower the footprint apply compression or better yet use binary serialization function which yields much
better performance and smallest possible footprint.  

Obtain with: `CodecFunctions.javaSerializing(..)` providing the target `Class`.

##### Binary serialization function 
Ad-hoc binary serialization function based on a pair of custom `java.io` stream wrappers
[StateInputStream](src/main/java/com.github.sabirove/codec/util/StateInputStream.java) /
[StateOutputStream](src/main/java/com.github.sabirove/codec/util/StateOutputStream.java) providing convenient API
to read and write most of the standard Java types including collections, maps, arrays, strings, enums and then some.  
Supports IO with LEB128 variable-length encoded `int` and `long` values: unsigned variable-length
ints are used to serialize enum ordinals and length values for contiguous data types (e.g. collections, arrays)
helping to yield tiny serialization footprint.

Obtain with: `CodecFunctions.binarySerializing(..)` providing a pair of functions for reading and writing the 
target type.   
For example, such `CodecFunction` implementation for aforementioned `Person` pojo would look like:
```java
    CodecFunction<Person> personCodecFunction = 
        CodecFunctions.binarySerializing(
                (out, person) -> out.putString(person.name).putInt(person.age),
                in -> new Person(in.getString(), in.getInt())
        );           
```
Limitations: 
  1. writing and reading of the fields should be carried out in the exact same order
  2. null values are not supported
  3. no type information is stored and validated whatsoever


#### AES encryption filter

Encryption filter based on a solid AES encryption method with data integrity validation
and cryptographically strong random number generators.
Sensibly configured. Implemented on top of the the standard (`java.security`/`java.crypto`) components.

Obtain with `CodecFilters.encryptWithAes(..)` providing the `16 byte` secret key (or opt in for a random generated one).

Specs:
- Galois/Counter transformation mode (AES/GCM/NoPadding): streaming AES with data integrity validation
- 128 bit secret key: optimal key length
- 96 bit initialization vector (salt): optimal length, random value is generated per each encryption
- 128 bit authentication tag

Limitations:  
  When using this filter authentication tag is computed on the whole stream and is flushed at the very end
  when the underlying `CipherOutputStream` is closed.   
  Thus in order to validate the tag the whole encoded payload should be fully read back (decoded) and then the 
  input stream should be closed triggering the underlying `CipherInputStream` to read and validate the tag 
  from whatever leftover bytes in the stream. 

#### Noteworthy application examples

Assemble the `Codec` with the following `filter chain`: serialization -> encryption -> base64/url encode:
```java
    final Codec<State> codec =
            CodecBuilder.withFunction(CodecFunctions.binarySerializing(State::write, State::read))
                    .withBuffer(CodecBufferSpec.ofDefaultSize())
                    .withFilterChain(
                            CodecFilters.encryptWithAes(mySecretKey),
                            CodecFilters.encodeWithBase64Url()
                    ).build();

```
Such `Codec` can be used to encode some *sensitive* _state_ into a encrypted/encoded string that could be safely
passed around on the web by means of, for instance, request query parameters.
This can be useful when implementing a two-way negotiation process (like user registration/confirmation 
or OAuth consent flows) allowing to achieve two major goals:
- stateless operation: no need to keep the state of the user request between the two consecutive steps of the process
- validating authenticity of the following "return" request: payload can be properly decrypted only with private
 key used for encryption and so long as it wasn't tampered with.
