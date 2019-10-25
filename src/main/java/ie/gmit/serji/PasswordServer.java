package ie.gmit.serji;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * PasswordServer application class.
 *
 * Contains main method for starting the gRPC PasswordService Server.
 */
public class PasswordServer {

    // io.grpc.Server
    private Server grpcServer;
    // Create a Logger - will be helpful to debug server and clients
    public static final Logger logger = Logger.getLogger(PasswordServer.class.getName());
    // Set a port that the server will run on
    private static final int PORT = 8080;

    // Entry point for Server application
    public static void main(String[] args) throws IOException, InterruptedException {
        // Instance of the containing class
        final PasswordServer passwordServer = new PasswordServer();
        // Start the Server.
        passwordServer.start();
        // Keep the Server running indefinitely.
        passwordServer.blockUntilShutdown();
    }

    /**
     * Start the PasswordServer
     *
     * @throws IOException From ServerBuilder start() method.
     */
    private void start() throws IOException {
        // Build and start the gRPC Server
        grpcServer = ServerBuilder.forPort(PORT)
                .addService(new PasswordServiceImpl())
                .build()
                .start();
        logger.info("Password server started, listening on " + PORT);

    }

    private void stop() {
        if (grpcServer != null) {
            grpcServer.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (grpcServer != null) {
            grpcServer.awaitTermination();
        }
    }
}
