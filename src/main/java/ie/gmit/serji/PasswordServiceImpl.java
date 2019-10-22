package ie.gmit.serji;

import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;

import ie.gmit.serji.password.Passwords;
import ie.gmit.serji.passwordservice.HashInput;
import ie.gmit.serji.passwordservice.HashOutput;
import ie.gmit.serji.passwordservice.PasswordServiceGrpc;
import ie.gmit.serji.passwordservice.ValidateInput;
import io.grpc.stub.StreamObserver;

public class PasswordServiceImpl extends PasswordServiceGrpc.PasswordServiceImplBase {

    public PasswordServiceImpl() {

    }

    @Override
    public void hash(HashInput request, StreamObserver<HashOutput> responseObserver) {

        // generate a salt for this user
        byte[] salt = Passwords.getNextSalt();
        // get the request data and pass to Password hash method
        byte[] hashedPassword = Passwords.hash(request.getPassword().toCharArray(), salt);

        // create the output HashOutput object
        HashOutput response = HashOutput.newBuilder()
                .setUserId(request.getUserId())
                .setHashedPassword(ByteString.copyFrom(hashedPassword))
                .setSalt(ByteString.copyFrom(salt))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void validate(ValidateInput request, StreamObserver<BoolValue> responseObserver) {
//        super.validate(request, responseObserver);

        PasswordServer.logger.info("Received Validate Request: ");
        PasswordServer.logger.info("\tPassword: " + request.getPassword());
        PasswordServer.logger.info("\tHash: " + request.getHashedPassword());
        PasswordServer.logger.info("\tSalt: " + request.getSalt());


        boolean isValidated = Passwords.isExpectedPassword(
                request.getPassword().toCharArray(),
                request.getSalt().toByteArray(),
                request.getHashedPassword().toByteArray()
        );
        System.out.println(isValidated);
        BoolValue isValid = BoolValue.newBuilder().setValue(isValidated).build();

        responseObserver.onNext(isValid);
        responseObserver.onCompleted();
    }
}
