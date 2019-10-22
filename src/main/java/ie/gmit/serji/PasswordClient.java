package ie.gmit.serji;

import com.google.protobuf.BoolValue;
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

    public static void main(String[] args) throws Exception {
        PasswordClient client = new PasswordClient(HOST, PORT);

        HashInput input = HashInput.newBuilder()
                .setUserId(1)
                .setPassword("HelloWorld")
                .build();

        try {
            client.hash(input);
        } finally {
            // keep process alive to receive async response
            Thread.currentThread().join();
        }
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

    // async call to hash()
    public void hash(HashInput input) {
        StreamObserver<HashOutput> responseObserver = new StreamObserver<HashOutput>() {
            @Override
            public void onNext(HashOutput output) {
                logger.info("Received HashOutput: " + output);
            }

            @Override
            public void onError(Throwable throwable) {
                Status status = Status.fromThrowable(throwable);
                logger.log(Level.WARNING, "RPC Error: {0}", status);
            }

            @Override
            public void onCompleted() {
                logger.info("Finished receiving HashOutput");
                // End program
                System.exit(0);
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
                logger.info("Received BoolValue: " + output);
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
