package ie.gmit.serji.client;

import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;

import com.google.protobuf.Int32Value;
import ie.gmit.serji.passwordservice.HashInput;
import ie.gmit.serji.passwordservice.HashOutput;
import ie.gmit.serji.passwordservice.PasswordServiceGrpc;
import ie.gmit.serji.passwordservice.ValidateInput;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PasswordClient {

    private static final String HOST = "localhost";
    private static final int PORT = 8080; // server port to connect to


    private static TestUser _theUser;


    public static void main(String[] args) throws Exception {
        PasswordClient client = new PasswordClient(HOST, PORT);

        // test User object for demonstration
        _theUser = new TestUser();
        _theUser.userId = 1;

        try {
            Int32Value id = Int32Value.newBuilder().setValue(_theUser.userId).build();
            logger.info("Client begin requesting generated password");
            client.generatePassword(id);
        } finally {
            // keep process alive to receive async response
            Thread.currentThread().join();
        }

//        try {
//            client.validate(validateInput);
//        } finally {
//            Thread.currentThread().join();
//        }

    }

    private static final Logger logger = Logger.getLogger(PasswordClient.class.getName());
    private final ManagedChannel channel;
    private final PasswordServiceGrpc.PasswordServiceStub asyncPasswordService;
    private final PasswordServiceGrpc.PasswordServiceBlockingStub syncPasswordService;

    public PasswordClient(String host, int port) {
        channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
        syncPasswordService = PasswordServiceGrpc.newBlockingStub(channel);
        asyncPasswordService = PasswordServiceGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    // asynchronous call to hash()
    public void hash(HashInput input) {
        StreamObserver<HashOutput> responseObserver = new StreamObserver<HashOutput>() {
            @Override
            public void onNext(HashOutput output) {

                logger.info("Received hash: " + output);

                _theUser.hashedPassword = output.getHashedPassword().toByteArray();
                _theUser.salt = output.getSalt().toByteArray();
            }

            @Override
            public void onError(Throwable throwable) {
                Status status = Status.fromThrowable(throwable);
                logger.log(Level.WARNING, "RPC Error: {0}", status);
            }

            @Override
            public void onCompleted() {
                logger.info("Finished receiving HashOutput");
                // create ValidateInput object
                ValidateInput validateInput = ValidateInput.newBuilder()
                        .setPassword(_theUser.password)
                        .setHashedPassword(ByteString.copyFrom(_theUser.hashedPassword))
                        .setSalt(ByteString.copyFrom(_theUser.salt))
                        .build();

                validate(validateInput);
            }
        };

        try {
            logger.info("Requesting HashOutput");
            asyncPasswordService.hash(input, responseObserver);
            logger.info("Returned from requesting HashOutput");
        } catch (
                StatusRuntimeException ex) {
            logger.log(Level.WARNING, "RPC failed: {0}", ex.getStatus());
            return;
        }
    }

    // async call to validate()
    public void validate(ValidateInput input) {
        StreamObserver<BoolValue> responseObserver = new StreamObserver<BoolValue>() {
            @Override
            public void onNext(BoolValue output) {
                logger.info("Received BoolValue: " + output.getValue());
            }

            @Override
            public void onError(Throwable throwable) {
                Status status = Status.fromThrowable(throwable);
                logger.log(Level.WARNING, "RPC Error: {0}", status);
            }

            @Override
            public void onCompleted() {
                logger.info("Finished receiving BoolValue");
                // End program
                System.exit(0);
            }
        };

        try {
            logger.info("Requesting BoolValue");
            asyncPasswordService.validate(input, responseObserver);
            logger.info("Returned from requesting BoolValue");
        } catch (
                StatusRuntimeException ex) {
            logger.log(Level.WARNING, "RPC failed: {0}", ex.getStatus());
            return;
        }
    }

    // async call to generatePassword()
    public void generatePassword(Int32Value userId) {
        StreamObserver<HashInput> responseObserver = new StreamObserver<HashInput>() {

            @Override
            public void onNext(HashInput value) {
//                 access to userId is possible here but not needed right now
//                _theUser.userId = value.getUserId();
                logger.info("Got HashInput with password: " + value.getPassword() + " from generatePassword()");
                _theUser.password = value.getPassword();
            }

            @Override
            public void onError(Throwable throwable) {
                Status status = Status.fromThrowable(throwable);
                logger.log(Level.WARNING, "RPC Error: {0}", status);
            }

            @Override
            public void onCompleted() {
                logger.info("Finished receiving HashInput from generatePassword()");
                HashInput input = HashInput.newBuilder()
                        .setUserId(_theUser.userId)
                        .setPassword(_theUser.password)
                        .build();
                hash(input);
            }
        };

        try {
            logger.info("Requesting Password");
            asyncPasswordService.generatePassword(userId, responseObserver);
            logger.info("Returned from requesting Password");
        } catch (
                StatusRuntimeException ex) {
            logger.log(Level.WARNING, "RPC failed: {0}", ex.getStatus());
            return;
        }
    }
}
