package ie.gmit.serji;

import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;

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

        _theUser = new TestUser();
        _theUser.userId = 1;
        _theUser.password = "HelloWorld";


        HashInput input = HashInput.newBuilder()
                .setUserId(_theUser.userId)
                .setPassword(_theUser.password)
                .build();

        try {
            client.hash(input);
        } finally {
            // keep process alive to receive async response
            Thread.currentThread().join();
        }

//        // create ValidateInput object
//        ValidateInput validateInput = ValidateInput.newBuilder()
//                .setPassword(_theUser.password)
//                .setHashedPassword("[B@752e50b8")
//                .setSalt("[B@1bdc903c")
//                .build();
//
//        try {
//            client.validate(validateInput);
//        } finally {
//            Thread.currentThread().join();
//        }

    }

    private static final Logger logger = Logger.getLogger(PasswordClient.class.getName());
    private final ManagedChannel channel;
    private final PasswordServiceGrpc.PasswordServiceStub asyncInventoryService;
    private final PasswordServiceGrpc.PasswordServiceBlockingStub syncInventoryService;

    public PasswordClient(String host, int port) {
        channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
        syncInventoryService = PasswordServiceGrpc.newBlockingStub(channel);
        asyncInventoryService = PasswordServiceGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    // asynchronous call to hash()
    public void hash(HashInput input) {
        StreamObserver<HashOutput> responseObserver = new StreamObserver<HashOutput>() {
            @Override
            public void onNext(HashOutput output) {

                logger.info("Received HashOutput: " + output);

                _theUser.hashedPassword = output.getHashedPassword().toByteArray();
                _theUser.salt = output.getSalt().toByteArray();

                logger.info("Received HashedBytes: " + _theUser.hashedPassword);
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
                // End program
//                System.exit(0);
            }
        };

        try {
            logger.info("Requesting HashOutput");
            asyncInventoryService.hash(input, responseObserver);
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
            asyncInventoryService.validate(input, responseObserver);
            logger.info("Returned from requesting BoolValue");
        } catch (
                StatusRuntimeException ex) {
            logger.log(Level.WARNING, "RPC failed: {0}", ex.getStatus());
            return;
        }
    }

}
