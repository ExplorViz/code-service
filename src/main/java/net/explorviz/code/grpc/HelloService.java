package net.explorviz.code.grpc;

import com.google.protobuf.Empty;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import net.explorviz.code.proto.Greeter;
import net.explorviz.code.proto.HelloRequest;

@GrpcService
public class HelloService implements Greeter {

  // grpcurl -plaintext -d '{"name": "test"}' localhost:9000 helloworld.Greeter.SayHello

  @Override
  public Uni<Empty> sayHello(final HelloRequest request) {
    System.out.println("Client says: " + request.getName());
    return Uni.createFrom().item(() -> Empty.newBuilder().build());
  }

}
