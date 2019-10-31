## Distributed Systems Project 2019 Part 1 - gRPC Password Service

A Password Service that provides password hashing and verification services written in Java using packages from 
https://github.com/grpc/grpc-java.

This service exposes three methods:

* hash: Used to generate a hash of a user’s password. Takes a password as input, returns the hash of the password, 
along with the salt used to generate the hash.
* validate: Used to validate a user-entered password by comparing it to the stored hash. Takes a password, a hashed 
password and a salt as input. Uses the salt to hash the input password and compares the resulting hash to the hashed 
password.
* generatePassword: Used to generate a random password for a user.  Takes an integer representing a user ID as input 
and returns that user ID along with a randomly generated password 64 characters long.

***
Utility methods for generating and verifying cryptographically secure salted hashes of
passwords in Java have been used from: https://gist.github.com/john-french/9c94d88f34b2a4ccbe55af6afb083674

***
##### Note on types:
* `int32` type has been used for User ID values.
* `bytes` have been used in the .proto file for both the hashed password and the salt. In the Java service, 
byte arrays are used.
* validate returns a type `com.google.protobuf.BoolValue`.
* generatePassword takes a type `com.google.protobuf.Int32Value`
