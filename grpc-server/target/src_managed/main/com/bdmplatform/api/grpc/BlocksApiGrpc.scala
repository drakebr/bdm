package com.bdmplatform.api.grpc

object BlocksApiGrpc {
  val METHOD_GET_BLOCK: _root_.io.grpc.MethodDescriptor[com.bdmplatform.api.grpc.BlockRequest, com.bdmplatform.api.grpc.BlockWithHeight] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("bdm.node.grpc.BlocksApi", "GetBlock"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.BlockRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.BlockWithHeight])
      .build()
  
  val METHOD_GET_BLOCK_RANGE: _root_.io.grpc.MethodDescriptor[com.bdmplatform.api.grpc.BlockRangeRequest, com.bdmplatform.api.grpc.BlockWithHeight] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("bdm.node.grpc.BlocksApi", "GetBlockRange"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.BlockRangeRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.BlockWithHeight])
      .build()
  
  val METHOD_GET_CURRENT_HEIGHT: _root_.io.grpc.MethodDescriptor[com.google.protobuf.empty.Empty, com.google.protobuf.wrappers.UInt32Value] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("bdm.node.grpc.BlocksApi", "GetCurrentHeight"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.empty.Empty])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.wrappers.UInt32Value])
      .build()
  
  val SERVICE: _root_.io.grpc.ServiceDescriptor =
    _root_.io.grpc.ServiceDescriptor.newBuilder("bdm.node.grpc.BlocksApi")
      .setSchemaDescriptor(new _root_.scalapb.grpc.ConcreteProtoFileDescriptorSupplier(com.bdmplatform.api.grpc.BlocksApiProto.javaDescriptor))
      .addMethod(METHOD_GET_BLOCK)
      .addMethod(METHOD_GET_BLOCK_RANGE)
      .addMethod(METHOD_GET_CURRENT_HEIGHT)
      .build()
  
  trait BlocksApi extends _root_.scalapb.grpc.AbstractService {
    override def serviceCompanion = BlocksApi
    def getBlock(request: com.bdmplatform.api.grpc.BlockRequest): scala.concurrent.Future[com.bdmplatform.api.grpc.BlockWithHeight]
    def getBlockRange(request: com.bdmplatform.api.grpc.BlockRangeRequest, responseObserver: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.BlockWithHeight]): Unit
    def getCurrentHeight(request: com.google.protobuf.empty.Empty): scala.concurrent.Future[com.google.protobuf.wrappers.UInt32Value]
  }
  
  object BlocksApi extends _root_.scalapb.grpc.ServiceCompanion[BlocksApi] {
    implicit def serviceCompanion: _root_.scalapb.grpc.ServiceCompanion[BlocksApi] = this
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = com.bdmplatform.api.grpc.BlocksApiProto.javaDescriptor.getServices().get(0)
  }
  
  trait BlocksApiBlockingClient {
    def serviceCompanion = BlocksApi
    def getBlock(request: com.bdmplatform.api.grpc.BlockRequest): com.bdmplatform.api.grpc.BlockWithHeight
    def getBlockRange(request: com.bdmplatform.api.grpc.BlockRangeRequest): scala.collection.Iterator[com.bdmplatform.api.grpc.BlockWithHeight]
    def getCurrentHeight(request: com.google.protobuf.empty.Empty): com.google.protobuf.wrappers.UInt32Value
  }
  
  class BlocksApiBlockingStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[BlocksApiBlockingStub](channel, options) with BlocksApiBlockingClient {
    override def getBlock(request: com.bdmplatform.api.grpc.BlockRequest): com.bdmplatform.api.grpc.BlockWithHeight = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_GET_BLOCK, options, request)
    }
    
    override def getBlockRange(request: com.bdmplatform.api.grpc.BlockRangeRequest): scala.collection.Iterator[com.bdmplatform.api.grpc.BlockWithHeight] = {
      _root_.scalapb.grpc.ClientCalls.blockingServerStreamingCall(channel, METHOD_GET_BLOCK_RANGE, options, request)
    }
    
    override def getCurrentHeight(request: com.google.protobuf.empty.Empty): com.google.protobuf.wrappers.UInt32Value = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_GET_CURRENT_HEIGHT, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): BlocksApiBlockingStub = new BlocksApiBlockingStub(channel, options)
  }
  
  class BlocksApiStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[BlocksApiStub](channel, options) with BlocksApi {
    override def getBlock(request: com.bdmplatform.api.grpc.BlockRequest): scala.concurrent.Future[com.bdmplatform.api.grpc.BlockWithHeight] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_GET_BLOCK, options, request)
    }
    
    override def getBlockRange(request: com.bdmplatform.api.grpc.BlockRangeRequest, responseObserver: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.BlockWithHeight]): Unit = {
      _root_.scalapb.grpc.ClientCalls.asyncServerStreamingCall(channel, METHOD_GET_BLOCK_RANGE, options, request, responseObserver)
    }
    
    override def getCurrentHeight(request: com.google.protobuf.empty.Empty): scala.concurrent.Future[com.google.protobuf.wrappers.UInt32Value] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_GET_CURRENT_HEIGHT, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): BlocksApiStub = new BlocksApiStub(channel, options)
  }
  
  def bindService(serviceImpl: BlocksApi, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition =
    _root_.io.grpc.ServerServiceDefinition.builder(SERVICE)
    .addMethod(
      METHOD_GET_BLOCK,
      _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[com.bdmplatform.api.grpc.BlockRequest, com.bdmplatform.api.grpc.BlockWithHeight] {
        override def invoke(request: com.bdmplatform.api.grpc.BlockRequest, observer: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.BlockWithHeight]): Unit =
          serviceImpl.getBlock(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
            executionContext)
      }))
    .addMethod(
      METHOD_GET_BLOCK_RANGE,
      _root_.io.grpc.stub.ServerCalls.asyncServerStreamingCall(new _root_.io.grpc.stub.ServerCalls.ServerStreamingMethod[com.bdmplatform.api.grpc.BlockRangeRequest, com.bdmplatform.api.grpc.BlockWithHeight] {
        override def invoke(request: com.bdmplatform.api.grpc.BlockRangeRequest, observer: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.BlockWithHeight]): Unit =
          serviceImpl.getBlockRange(request, observer)
      }))
    .addMethod(
      METHOD_GET_CURRENT_HEIGHT,
      _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[com.google.protobuf.empty.Empty, com.google.protobuf.wrappers.UInt32Value] {
        override def invoke(request: com.google.protobuf.empty.Empty, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.wrappers.UInt32Value]): Unit =
          serviceImpl.getCurrentHeight(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
            executionContext)
      }))
    .build()
  
  def blockingStub(channel: _root_.io.grpc.Channel): BlocksApiBlockingStub = new BlocksApiBlockingStub(channel)
  
  def stub(channel: _root_.io.grpc.Channel): BlocksApiStub = new BlocksApiStub(channel)
  
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = com.bdmplatform.api.grpc.BlocksApiProto.javaDescriptor.getServices().get(0)
  
}