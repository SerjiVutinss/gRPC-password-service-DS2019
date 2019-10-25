package ie.gmit.serji;

import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;

import com.google.protobuf.Int32Value;
import ie.gmit.serji.password.Passwords;
import ie.gmit.serji.passwordservice.HashInput;
import ie.gmit.serji.passwordservice.HashOutput;
import ie.gmit.serji.passwordservice.PasswordServiceGrpc;
import ie.gmit.serji.passwordservice.ValidateInput;
import io.grpc.stub.StreamObserver;

/**
 * Implementation of the generated abstract PasswordServiceImplBase class.
 * <p>
 * Contains the methods which will be called by the client.
 */
public class PasswordServiceImpl extends PasswordServiceGrpc.PasswordServiceImplBase {

    public PasswordServiceImpl() {
    }

    /**
     * Implementation of base hash() method to be called by a client.
     * <p>
     * Uses values in HashInput request object to create and respond to the client with a
     * a HashOutput object.
     * <p>
     * RespondsWith - HashOutput object with the supplied userId, hash of supplied password
     * and salt used to generate the hash.
     *
     * @param request          HashInput object containing the userId and the password.
     * @param responseObserver provided by the client.
     * @see HashInput   Type of request
     * @see HashOutput  Type of response
     */
    @Override
    public void hash(HashInput request, StreamObserver<HashOutput> responseObserver) {

        // Generate a salt for this user.
        byte[] salt = Passwords.getNextSalt();
        // get the request data and pass to Password hash method.
        byte[] hashedPassword = Passwords.hash(request.getPassword().toCharArray(), salt);

        // Create the output HashOutput object, setting all fields.
        HashOutput response = HashOutput.newBuilder()
                .setUserId(request.getUserId())
                .setHashedPassword(ByteString.copyFrom(hashedPassword))
                .setSalt(ByteString.copyFrom(salt))
                .build();
        // Send the HashOutput as a response to the client.
        responseObserver.onNext(response);
        // Notify the client that the operation has completed.
        responseObserver.onCompleted();
    }

    /**
     * Implementation of the base validate() method to be called by a client.
     * <p>
     * Determines whether the salt and password properties in the ValidateInput object can
     * be used to generate a hash matching the hashedPassword property.
     * <p>
     * RespondsWith - a BoolValue wrapper object containing the result of the operation - true
     * if supplied salt and password can generate the hashedPassword value, else false
     *
     * @param request          - ValidateInput object containing a password, salt and hash
     * @param responseObserver - provided by the client
     * @see ValidateInput
     * @see BoolValue
     */
    @Override
    public void validate(ValidateInput request, StreamObserver<BoolValue> responseObserver) {

        PasswordServer.logger.info("\nReceived Validate Request: ");
        PasswordServer.logger.info("\tPassword: " + request.getPassword());
        PasswordServer.logger.info("\tHash: " + request.getHashedPassword());
        PasswordServer.logger.info("\tSalt: " + request.getSalt());

        // Check whether the provided password and salt can be used to generate a hash which
        // matches the supplied hashedPassword field's value.
        boolean isValidated = Passwords.isExpectedPassword(
                request.getPassword().toCharArray(),
                request.getSalt().toByteArray(),
                request.getHashedPassword().toByteArray()
        );
        PasswordServer.logger.info("\nValidated: " + isValidated);
        // Create an BoolValue wrapper object from the result of the above operation.
        BoolValue isValid = BoolValue.newBuilder().setValue(isValidated).build();
        // Send the BoolValue as a response to the client.
        responseObserver.onNext(isValid);
        // Notify the client that the operation has completed.
        responseObserver.onCompleted();
    }

    /**
     * Implementation of the base generatePassword() method to be called by a client.
     * <p>
     * Generates a random password.
     * <p>
     * RespondsWith - a HashInput object ready to be passed into the hash() method.  This object
     * has its userId field set to the userId passed into this method and its password field set
     * to the randomly generated password.
     *
     * @param request          Int32Value representing a userId
     * @param responseObserver provided by the client
     * @see Int32Value
     * @see HashInput
     */
    @Override
    public void generatePassword(Int32Value request, StreamObserver<HashInput> responseObserver) {
        // Generate a random password with length 64.
        String password = Passwords.generateRandomPassword(64);
        // Create a HashInput object to send as a response to the client, setting all fields.
        HashInput hashInput = HashInput.newBuilder()
                .setUserId(request.getValue())
                .setPassword(password)
                .build();

        // Send the HashInput object as a response to the client.
        responseObserver.onNext(hashInput);
        // Notify the client that the operation has completed.
        responseObserver.onCompleted();
    }
}