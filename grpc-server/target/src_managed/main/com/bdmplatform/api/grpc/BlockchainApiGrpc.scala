package com.bdmplatform.api.grpc

object BlockchainApiGrpc {
  val METHOD_GET_ACTIVATION_STATUS: _root_.io.grpc.MethodDescriptor[com.bdmplatform.api.grpc.ActivationStatusRequest, com.bdmplatform.api.grpc.ActivationStatusResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("bdm.node.grpc.BlockchainApi", "GetActivationStatus"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.ActivationStatusRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.ActivationStatusResponse])
      .build()
  
  val METHOD_GET_BASE_TARGET: _root_.io.grpc.MethodDescriptor[com.google.protobuf.empty.Empty, com.bdmplatform.api.grpc.BaseTargetResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("bdm.node.grpc.BlockchainApi", "GetBaseTarget"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.BaseTargetResponse])
      .build()
  
  val METHOD_GET_CUMULATIVE_SCORE: _root_.io.grpc.MethodDescriptor[com.google.protobuf.empty.Empty, com.bdmplatform.api.grpc.ScoreResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("bdm.node.grpc.BlockchainApi", "GetCumulativeScore"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.ScoreResponse])
      .build()
  
  val SERVICE: _root_.io.grpc.ServiceDescriptor =
    _root_.io.grpc.ServiceDescriptor.newBuilder("bdm.node.grpc.BlockchainApi")
      .setSchemaDescriptor(new _root_.scalapb.grpc.ConcreteProtoFileDescriptorSupplier(com.bdmplatform.api.grpc.BlockchainApiProto.javaDescriptor))
      .addMethod(METHOD_GET_ACTIVATION_STATUS)
      .addMethod(METHOD_GET_BASE_TARGET)
      .addMethod(METHOD_GET_CUMULATIVE_SCORE)
      .build()
  
  trait BlockchainApi extends _root_.scalapb.grpc.AbstractService {
    override def serviceCompanion = BlockchainApi
    def getActivationStatus(request: com.bdmplatform.api.grpc.ActivationStatusRequest): scala.concurrent.Future[com.bdmplatform.api.grpc.ActivationStatusResponse]
    def getBaseTarget(request: com.google.protobuf.empty.Empty): scala.concurrent.Future[com.bdmplatform.api.grpc.BaseTargetResponse]
    def getCumulativeScore(request: com.google.protobuf.empty.Empty): scala.concurrent.Future[com.bdmplatform.api.grpc.ScoreResponse]
  }
  
  object BlockchainApi extends _root_.scalapb.grpc.ServiceCompanion[BlockchainApi] {
    implicit def serviceCompanion: _root_.scalapb.grpc.ServiceCompanion[BlockchainApi] = this
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = com.bdmplatform.api.grpc.BlockchainApiProto.javaDescriptor.getServices().get(0)
  }
  
  trait BlockchainApiBlockingClient {
    def serviceCompanion = BlockchainApi
    def getActivationStatus(request: com.bdmplatform.api.grpc.ActivationStatusRequest): com.bdmplatform.api.grpc.ActivationStatusResponse
    def getBaseTarget(request: com.google.protobuf.empty.Empty): com.bdmplatform.api.grpc.BaseTargetResponse
    def getCumulativeScore(request: com.google.protobuf.empty.Empty): com.bdmplatform.api.grpc.ScoreResponse
  }
  
  class BlockchainApiBlockingStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[BlockchainApiBlockingStub](channel, options) with BlockchainApiBlockingClient {
    override def getActivationStatus(request: com.bdmplatform.api.grpc.ActivationStatusRequest): com.bdmplatform.api.grpc.ActivationStatusResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_GET_ACTIVATION_STATUS, options, request)
    }
    
    override def getBaseTarget(request: com.google.protobuf.empty.Empty): com.bdmplatform.api.grpc.BaseTargetResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_GET_BASE_TARGET, options, request)
    }
    
    override def getCumulativeScore(request: com.google.protobuf.empty.Empty): com.bdmplatform.api.grpc.ScoreResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_GET_CUMULATIVE_SCORE, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): BlockchainApiBlockingStub = new BlockchainApiBlockingStub(channel, options)
  }
  
  class BlockchainApiStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[BlockchainApiStub](channel, options) with BlockchainApi {
    override def getActivationStatus(request: com.bdmplatform.api.grpc.ActivationStatusRequest): scala.concurrent.Future[com.bdmplatform.api.grpc.ActivationStatusResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_GET_ACTIVATION_STATUS, options, request)
    }
    
    override def getBaseTarget(request: com.google.protobuf.empty.Empty): scala.concurrent.Future[com.bdmplatform.api.grpc.BaseTargetResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_GET_BASE_TARGET, options, request)
    }
    
    override def getCumulativeScore(request: com.google.protobuf.empty.Empty): scala.concurrent.Future[com.bdmplatform.api.grpc.ScoreResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_GET_CUMULATIVE_SCORE, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): BlockchainApiStub = new BlockchainApiStub(channel, options)
  }
  
  def bindService(serviceImpl: BlockchainApi, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition =
    _root_.io.grpc.ServerServiceDefinition.builder(SERVICE)
    .addMethod(
      METHOD_GET_ACTIVATION_STATUS,
      _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[com.bdmplatform.api.grpc.ActivationStatusRequest, com.bdmplatform.api.grpc.ActivationStatusResponse] {
        override def invoke(request: com.bdmplatform.api.grpc.ActivationStatusRequest, observer: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.ActivationStatusResponse]): Unit =
          serviceImpl.getActivationStatus(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
            executionContext)
      }))
    .addMethod(
      METHOD_GET_BASE_TARGET,
      _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[com.google.protobuf.empty.Empty, com.bdmplatform.api.grpc.BaseTargetResponse] {
        override def invoke(request: com.google.protobuf.empty.Empty, observer: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.BaseTargetResponse]): Unit =
          serviceImpl.getBaseTarget(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
            executionContext)
      }))
    .addMethod(
      METHOD_GET_CUMULATIVE_SCORE,
      _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[com.google.protobuf.empty.Empty, com.bdmplatform.api.grpc.ScoreResponse] {
        override def invoke(request: com.google.protobuf.empty.Empty, observer: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.ScoreResponse]): Unit =
          serviceImpl.getCumulativeScore(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
            executionContext)
      }))
    .build()
  
  def blockingStub(channel: _root_.io.grpc.Channel): BlockchainApiBlockingStub = new BlockchainApiBlockingStub(channel)
  
  def stub(channel: _root_.io.grpc.Channel): BlockchainApiStub = new BlockchainApiStub(channel)
  
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = com.bdmplatform.api.grpc.BlockchainApiProto.javaDescriptor.getServices().get(0)
  
}