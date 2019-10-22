package ie.gmit.serji;

import com.google.protobuf.BoolValue;
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
//        super.hash(request, responseObserver);
        HashOutput response = HashOutput.newBuilder()
                .setUserId(request.getUserId())
                .setHashedPassword("WORKING")
                .setSalt("SALT").build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void validate(ValidateInput request, StreamObserver<BoolValue> responseObserver) {
//        super.validate(request, responseObserver);
        boolean isValidated = true;
        com.google.protobuf.BoolValue isValid = BoolValue.newBuilder().setValue(isValidated).build();
        responseObserver.onNext(isValid);
    }
}
