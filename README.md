[![Build Status](https://travis-ci.org/sabirove/codec.svg?branch=master)](https://travis-ci.org/sabirove/codec)
[![Coverage Status](https://coveralls.io/repos/github/sabirove/codec/badge.svg)](https://coveralls.io/github/sabirove/codec)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.sabirove/codec/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.sabirove/codec)

## Codec utility

#### Features
- Small library with no external dependencies
- Plain `java.io` API based
- Composable design
- A bunch of out-of-the-box implementations
- Leveraging `UncheckedIOException` to keep the core API clean

#### Use cases
- Ad-hoc binary serialization with convenient API
- Simple and convenient AES encrypt/decrypt with data integrity validation
- Arbitrary bidirectional IO transformations in a single package (e.g. compress/decompress, encode/decode)
- Complex IO chains (e.g serialize <-> compress <-> encrypt <-> encode) in a single package

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
                                CodecFilters.encryptWithAes(mySecretKey),
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
Now `bytes` array contains two `Person` objects serialized then Gzip compressed, AES encrypted and finally base64/url encoded.  
Reversing read (decode) operation is straightforward granted we use the same `Codec` instance (or similarly build one)
that was used to write the data.

#### Overview
- [About](#about)
- [Codec function](#codec-function)
- [Codec filter](#codec-filter)
- [Buffering](#buffering)
- [API](#api)
- [Serialization functions](#serialization-functions)
- [AES encryption filter](#aes-encryption-filter)


#### About
[Codec](src/main/java/com/github/sabirove/codec/Codec.java) is a bidirectional IO function suitable to encode/decode single values or streams of values of the specific
type operating on top of the `java.io` streams.  
*Encoding* an object of type `<T>` means *serializing* it against an `OutputStream` while  *decoding* is a complementary
*deserialization* operation against an `InputStream` holding the encoded contents.

`Codec` consists of [CodecFunction](#codec-function) and zero to many [CodecFilter](#codec-filter)s (including optional 
[CodecBufferSpec](#buffering)) forming the following processing pipeline:
* Encode: `T -> CodecFunction<T> -> CodecFilter1 -> CodecFilter2 -> ... CodecFilterN -> BufferedOutputStream (optional) -> outbound OutputStream`  
* Decode: `T <- CodecFunction<T> <- CodecFilter1 <- CodecFilter2 <- ... CodecFilterN <- BufferedInputStream (optional) <- inbound InputStream`

In the simplest case `Codec` consists of just a [CodecFunction](#codec-function) and is used to serialize and 
deserialize objects without any extra byte stream transformations.  
`Codec` instances are obtained using the builder API: `CodecBuilder.withFunction(..)`

#### Codec function
[CodecFunction](src/main/java/com/github/sabirove/codec/function/CodecFunction.java) is a bidirectional serialization
function working against `java.io` streams to write and read back target `<T>` values:
```java
    public abstract void write(T value, OutputStream out) throws IOException;
    public abstract T read(InputStream in) throws IOException;
```
 [CodecFunctions](src/main/java/com/github/sabirove/codec/function/CodecFunctions.java) factory can be used 
 to obtain a number of out of the box implementations:
- `CodecFunctions.javaSerializing(..)`: IO on pojo types with standard java serialization (works for any `Serializable` type)
- `CodecFunctions.binarySerializing(..)`: IO on pojo types with ad-hoc binary serialization
- `CodecFunctions.binary(..)`: IO on plain byte arrays of arbitrary size
- `CodecFunctions.binaryChunked(..)`: IO on plain byte arrays of fixed size (with optional size "strictness")
- `CodecFunctions.string(..)`: IO on strings

#### Codec filter
[CodecFilter](src/main/java/com/github/sabirove/codec/filter/CodecFilter.java) represents a pair of complementary 
`OutputStream/InputStream` wrappers used to apply extra filtering on top of the `java.io` streams
 (e.g. compress/decompress or encode/decode).  
 In other words, it is `java.io.FilterInputStream`/`java.io.FilterOutputStream` in a single package.
```java
    public abstract OutputStream filter(OutputStream out) throws IOException;
    public abstract InputStream filter(InputStream in) throws IOException;
```
`CodecFilter`s can be chained (composed) one after another forming a `filter chain`:
```java
    public final CodecFilter chain(CodecFilter next);
    public static CodecFilter chain(CodecFilter... filters);
```
Invocation like `filter1.chain(filter2)` (or equivalent `CodecFilter.chain(filter1, filter2)`) yields the following
filtering scheme:
- Encode: `... -> CodecFilter1 -> CodecFilter2 -> outbound OutputStream`
- Decode: `... <- CodecFilter1 <- CodecFilter2 <- inbound InputStream`

[CodecFilters](src/main/java/com/github/sabirove/codec/filter/CodecFilters.java) factory can be used 
to obtain a number of out of the box implementations:
- `CodecFilters.compressWithDeflate()`: apply `Deflate` compress/decompress
- `CodecFilters.compressWithGzip()`: apply `Gzip` compress/decompress
- `CodecFilters.encodeWithBase64()`: apply `Base64` encode/decode
- `CodecFilters.encodeWithBase64Url()`: apply `Base64URL` encode/decode
- `CodecFilters.encodeWithBase64Mime()`: apply `Base64MIME` encode/decode
- `CodecFilters.encryptWithAes()`: apply `AES` encrypt/decrypt

#### Buffering

IO buffering is handled with [CodecBufferSpec](src/main/java/com/github/sabirove/codec/filter/CodecBufferSpec.java) 
which is a special kind of `CodecFilter` placed on the *outer edge* of the filter chain to apply
`BufferedInputStream/BufferedOutputStream` wrappers on top of the inbound/outbound streams.  
Obtain with `CodecBufferSpec.ofSize(..)` specifying the buffer sizes in bytes or opt in for the
default sized one using `CodecBufferSpec.ofDefaultSize()`.  
Certain types of `java.io` streams can be excluded by means of `withXXXStreamExclusions(..)` 
or `addXXXStreamExclusions(..)` instance calls.

#### API

##### Operating on single values

Based around plain byte arrays which is useful for single value operations.   
**Note**: default implementation is suboptimal when it comes to IO on large quantities of values because of
the extra object allocations per single operation: use stream-oriented API for such occasions if possible.
```java
    public byte[] encode(T value);
    public T decode(byte[] in);
```

##### Operating on streams of values
Based around `java.io` streams and is useful when dealing with large quantities of values.
```java
    public EncoderStream<T> wrap(OutputStream os);
    public DecoderStream<T> wrap(InputStream is);
```
[EncoderStream](src/main/java/com/github/sabirove/codec/EncoderStream.java)/[DecoderStream](src/main/java/com/github/sabirove/codec/DecoderStream.java) 
are a pair of light `java.io` stream wrappers handling reads and writes:

```java
    public final class EncoderStream<T> implements AutoCloseable, Flushable {
        public void write(T input);
    }
    
    public final class DecoderStream<T> implements AutoCloseable {
        public T read();
    }
```

#### Serialization functions

##### Java serialization function
Standard java serialization function that works for any Serializable object. Use for applications where
serialization performance and footprint size is not a concern.  
To lower the footprint apply compression or better still use binary serialization function which yields much
better performance and smallest possible footprint.  

Obtain with: `CodecFunctions.javaSerializing(..)` providing the target `Class`.

##### Binary serialization function 
Ad-hoc binary serialization function comprised of a pair of custom `java.io` stream wrappers
[StateInputStream](src/main/java/com/github/sabirove/codec/util/StateInputStream.java) /
[StateOutputStream](src/main/java/com/github/sabirove/codec/util/StateOutputStream.java) featuring convenient API
to read and write most of the standard Java types including collections, maps, arrays, strings and enums.  
Supports LEB128 variable-length encoded `int` and `long` values.  
**Note**: unsigned variable-length ints are used internally to serialize enum ordinals and length values 
for contiguous data types (e.g. collections, arrays) helping to yield tiny serialization footprint.

Obtain with: `CodecFunctions.binarySerializing(..)` providing a pair of functions for reading and writing the 
target type.   
E.g. such function implementation for aforementioned `Person` pojo would look like this:
```java
    CodecFunction<Person> personCodecFunction = 
        CodecFunctions.binarySerializing(
                (out, person) -> out.putString(person.name).putInt(person.age),
                in -> new Person(in.getString(), in.getInt())
        );           
```
Limitations: 
  1. field reads and writes should be carried out in the exact same order
  2. null values are not supported


#### AES encryption filter

Encryption filter based on a solid AES encryption method with data integrity validation
and cryptographically strong random number generators.
Sensibly configured. Implemented on top of the standard library (`java.security`/`java.crypto`).

Obtain with `CodecFilters.encryptWithAes(..)` providing the `16 byte` secret key (or opt in for a random generated one).

Specs:
- Galois/Counter transformation mode (AES/GCM/NoPadding): streaming AES with data integrity validation
- 128 bit secret key: optimal key length
- 96 bit initialization vector (salt): optimal length, random value is generated per each encryption
- 128 bit authentication tag

Limitations:  
When using this filter authentication tag is computed on the fly against the whole stream and is flushed
at the very end when the underlying `CipherOutputStream` is closed. So in order to validate the tag the whole payload 
should be fully read back (decoded) and then the input stream should be closed triggering the underlying 
`CipherInputStream` to read and validate the tag from whatever leftover bytes in the stream. 
