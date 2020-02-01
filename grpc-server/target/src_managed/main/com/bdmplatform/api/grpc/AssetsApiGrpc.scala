package com.bdmplatform.api.grpc

object AssetsApiGrpc {
  val METHOD_GET_INFO: _root_.io.grpc.MethodDescriptor[com.bdmplatform.api.grpc.AssetRequest, com.bdmplatform.api.grpc.AssetInfoResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("bdm.node.grpc.AssetsApi", "GetInfo"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.AssetRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.AssetInfoResponse])
      .build()
  
  val SERVICE: _root_.io.grpc.ServiceDescriptor =
    _root_.io.grpc.ServiceDescriptor.newBuilder("bdm.node.grpc.AssetsApi")
      .setSchemaDescriptor(new _root_.scalapb.grpc.ConcreteProtoFileDescriptorSupplier(com.bdmplatform.api.grpc.AssetsApiProto.javaDescriptor))
      .addMethod(METHOD_GET_INFO)
      .build()
  
  trait AssetsApi extends _root_.scalapb.grpc.AbstractService {
    override def serviceCompanion = AssetsApi
    def getInfo(request: com.bdmplatform.api.grpc.AssetRequest): scala.concurrent.Future[com.bdmplatform.api.grpc.AssetInfoResponse]
  }
  
  object AssetsApi extends _root_.scalapb.grpc.ServiceCompanion[AssetsApi] {
    implicit def serviceCompanion: _root_.scalapb.grpc.ServiceCompanion[AssetsApi] = this
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = com.bdmplatform.api.grpc.AssetsApiProto.javaDescriptor.getServices().get(0)
  }
  
  trait AssetsApiBlockingClient {
    def serviceCompanion = AssetsApi
    def getInfo(request: com.bdmplatform.api.grpc.AssetRequest): com.bdmplatform.api.grpc.AssetInfoResponse
  }
  
  class AssetsApiBlockingStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[AssetsApiBlockingStub](channel, options) with AssetsApiBlockingClient {
    override def getInfo(request: com.bdmplatform.api.grpc.AssetRequest): com.bdmplatform.api.grpc.AssetInfoResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_GET_INFO, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): AssetsApiBlockingStub = new AssetsApiBlockingStub(channel, options)
  }
  
  class AssetsApiStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[AssetsApiStub](channel, options) with AssetsApi {
    override def getInfo(request: com.bdmplatform.api.grpc.AssetRequest): scala.concurrent.Future[com.bdmplatform.api.grpc.AssetInfoResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_GET_INFO, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): AssetsApiStub = new AssetsApiStub(channel, options)
  }
  
  def bindService(serviceImpl: AssetsApi, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition =
    _root_.io.grpc.ServerServiceDefinition.builder(SERVICE)
    .addMethod(
      METHOD_GET_INFO,
      _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[com.bdmplatform.api.grpc.AssetRequest, com.bdmplatform.api.grpc.AssetInfoResponse] {
        override def invoke(request: com.bdmplatform.api.grpc.AssetRequest, observer: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.AssetInfoResponse]): Unit =
          serviceImpl.getInfo(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
            executionContext)
      }))
    .build()
  
  def blockingStub(channel: _root_.io.grpc.Channel): AssetsApiBlockingStub = new AssetsApiBlockingStub(channel)
  
  def stub(channel: _root_.io.grpc.Channel): AssetsApiStub = new AssetsApiStub(channel)
  
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = com.bdmplatform.api.grpc.AssetsApiProto.javaDescriptor.getServices().get(0)
  
}