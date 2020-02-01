package com.bdmplatform.api.grpc

object TransactionsApiGrpc {
  val METHOD_GET_TRANSACTIONS: _root_.io.grpc.MethodDescriptor[com.bdmplatform.api.grpc.TransactionsRequest, com.bdmplatform.api.grpc.TransactionResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("bdm.node.grpc.TransactionsApi", "GetTransactions"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.TransactionsRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.TransactionResponse])
      .build()
  
  val METHOD_GET_STATE_CHANGES: _root_.io.grpc.MethodDescriptor[com.bdmplatform.api.grpc.TransactionsRequest, com.bdmplatform.protobuf.transaction.InvokeScriptResult] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("bdm.node.grpc.TransactionsApi", "GetStateChanges"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.TransactionsRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.protobuf.transaction.InvokeScriptResult])
      .build()
  
  val METHOD_GET_STATUSES: _root_.io.grpc.MethodDescriptor[com.bdmplatform.api.grpc.TransactionsByIdRequest, com.bdmplatform.api.grpc.TransactionStatus] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("bdm.node.grpc.TransactionsApi", "GetStatuses"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.TransactionsByIdRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.TransactionStatus])
      .build()
  
  val METHOD_GET_UNCONFIRMED: _root_.io.grpc.MethodDescriptor[com.bdmplatform.api.grpc.TransactionsRequest, com.bdmplatform.api.grpc.TransactionResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("bdm.node.grpc.TransactionsApi", "GetUnconfirmed"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.TransactionsRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.TransactionResponse])
      .build()
  
  val METHOD_SIGN: _root_.io.grpc.MethodDescriptor[com.bdmplatform.api.grpc.SignRequest, com.bdmplatform.protobuf.transaction.SignedTransaction] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("bdm.node.grpc.TransactionsApi", "Sign"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.SignRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.protobuf.transaction.SignedTransaction])
      .build()
  
  val METHOD_BROADCAST: _root_.io.grpc.MethodDescriptor[com.bdmplatform.protobuf.transaction.SignedTransaction, com.bdmplatform.protobuf.transaction.SignedTransaction] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("bdm.node.grpc.TransactionsApi", "Broadcast"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.protobuf.transaction.SignedTransaction])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.protobuf.transaction.SignedTransaction])
      .build()
  
  val SERVICE: _root_.io.grpc.ServiceDescriptor =
    _root_.io.grpc.ServiceDescriptor.newBuilder("bdm.node.grpc.TransactionsApi")
      .setSchemaDescriptor(new _root_.scalapb.grpc.ConcreteProtoFileDescriptorSupplier(com.bdmplatform.api.grpc.TransactionsApiProto.javaDescriptor))
      .addMethod(METHOD_GET_TRANSACTIONS)
      .addMethod(METHOD_GET_STATE_CHANGES)
      .addMethod(METHOD_GET_STATUSES)
      .addMethod(METHOD_GET_UNCONFIRMED)
      .addMethod(METHOD_SIGN)
      .addMethod(METHOD_BROADCAST)
      .build()
  
  trait TransactionsApi extends _root_.scalapb.grpc.AbstractService {
    override def serviceCompanion = TransactionsApi
    def getTransactions(request: com.bdmplatform.api.grpc.TransactionsRequest, responseObserver: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.TransactionResponse]): Unit
    def getStateChanges(request: com.bdmplatform.api.grpc.TransactionsRequest, responseObserver: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.protobuf.transaction.InvokeScriptResult]): Unit
    def getStatuses(request: com.bdmplatform.api.grpc.TransactionsByIdRequest, responseObserver: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.TransactionStatus]): Unit
    def getUnconfirmed(request: com.bdmplatform.api.grpc.TransactionsRequest, responseObserver: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.TransactionResponse]): Unit
    def sign(request: com.bdmplatform.api.grpc.SignRequest): scala.concurrent.Future[com.bdmplatform.protobuf.transaction.SignedTransaction]
    def broadcast(request: com.bdmplatform.protobuf.transaction.SignedTransaction): scala.concurrent.Future[com.bdmplatform.protobuf.transaction.SignedTransaction]
  }
  
  object TransactionsApi extends _root_.scalapb.grpc.ServiceCompanion[TransactionsApi] {
    implicit def serviceCompanion: _root_.scalapb.grpc.ServiceCompanion[TransactionsApi] = this
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = com.bdmplatform.api.grpc.TransactionsApiProto.javaDescriptor.getServices().get(0)
  }
  
  trait TransactionsApiBlockingClient {
    def serviceCompanion = TransactionsApi
    def getTransactions(request: com.bdmplatform.api.grpc.TransactionsRequest): scala.collection.Iterator[com.bdmplatform.api.grpc.TransactionResponse]
    def getStateChanges(request: com.bdmplatform.api.grpc.TransactionsRequest): scala.collection.Iterator[com.bdmplatform.protobuf.transaction.InvokeScriptResult]
    def getStatuses(request: com.bdmplatform.api.grpc.TransactionsByIdRequest): scala.collection.Iterator[com.bdmplatform.api.grpc.TransactionStatus]
    def getUnconfirmed(request: com.bdmplatform.api.grpc.TransactionsRequest): scala.collection.Iterator[com.bdmplatform.api.grpc.TransactionResponse]
    def sign(request: com.bdmplatform.api.grpc.SignRequest): com.bdmplatform.protobuf.transaction.SignedTransaction
    def broadcast(request: com.bdmplatform.protobuf.transaction.SignedTransaction): com.bdmplatform.protobuf.transaction.SignedTransaction
  }
  
  class TransactionsApiBlockingStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[TransactionsApiBlockingStub](channel, options) with TransactionsApiBlockingClient {
    override def getTransactions(request: com.bdmplatform.api.grpc.TransactionsRequest): scala.collection.Iterator[com.bdmplatform.api.grpc.TransactionResponse] = {
      _root_.scalapb.grpc.ClientCalls.blockingServerStreamingCall(channel, METHOD_GET_TRANSACTIONS, options, request)
    }
    
    override def getStateChanges(request: com.bdmplatform.api.grpc.TransactionsRequest): scala.collection.Iterator[com.bdmplatform.protobuf.transaction.InvokeScriptResult] = {
      _root_.scalapb.grpc.ClientCalls.blockingServerStreamingCall(channel, METHOD_GET_STATE_CHANGES, options, request)
    }
    
    override def getStatuses(request: com.bdmplatform.api.grpc.TransactionsByIdRequest): scala.collection.Iterator[com.bdmplatform.api.grpc.TransactionStatus] = {
      _root_.scalapb.grpc.ClientCalls.blockingServerStreamingCall(channel, METHOD_GET_STATUSES, options, request)
    }
    
    override def getUnconfirmed(request: com.bdmplatform.api.grpc.TransactionsRequest): scala.collection.Iterator[com.bdmplatform.api.grpc.TransactionResponse] = {
      _root_.scalapb.grpc.ClientCalls.blockingServerStreamingCall(channel, METHOD_GET_UNCONFIRMED, options, request)
    }
    
    override def sign(request: com.bdmplatform.api.grpc.SignRequest): com.bdmplatform.protobuf.transaction.SignedTransaction = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_SIGN, options, request)
    }
    
    override def broadcast(request: com.bdmplatform.protobuf.transaction.SignedTransaction): com.bdmplatform.protobuf.transaction.SignedTransaction = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_BROADCAST, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): TransactionsApiBlockingStub = new TransactionsApiBlockingStub(channel, options)
  }
  
  class TransactionsApiStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[TransactionsApiStub](channel, options) with TransactionsApi {
    override def getTransactions(request: com.bdmplatform.api.grpc.TransactionsRequest, responseObserver: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.TransactionResponse]): Unit = {
      _root_.scalapb.grpc.ClientCalls.asyncServerStreamingCall(channel, METHOD_GET_TRANSACTIONS, options, request, responseObserver)
    }
    
    override def getStateChanges(request: com.bdmplatform.api.grpc.TransactionsRequest, responseObserver: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.protobuf.transaction.InvokeScriptResult]): Unit = {
      _root_.scalapb.grpc.ClientCalls.asyncServerStreamingCall(channel, METHOD_GET_STATE_CHANGES, options, request, responseObserver)
    }
    
    override def getStatuses(request: com.bdmplatform.api.grpc.TransactionsByIdRequest, responseObserver: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.TransactionStatus]): Unit = {
      _root_.scalapb.grpc.ClientCalls.asyncServerStreamingCall(channel, METHOD_GET_STATUSES, options, request, responseObserver)
    }
    
    override def getUnconfirmed(request: com.bdmplatform.api.grpc.TransactionsRequest, responseObserver: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.TransactionResponse]): Unit = {
      _root_.scalapb.grpc.ClientCalls.asyncServerStreamingCall(channel, METHOD_GET_UNCONFIRMED, options, request, responseObserver)
    }
    
    override def sign(request: com.bdmplatform.api.grpc.SignRequest): scala.concurrent.Future[com.bdmplatform.protobuf.transaction.SignedTransaction] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_SIGN, options, request)
    }
    
    override def broadcast(request: com.bdmplatform.protobuf.transaction.SignedTransaction): scala.concurrent.Future[com.bdmplatform.protobuf.transaction.SignedTransaction] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_BROADCAST, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): TransactionsApiStub = new TransactionsApiStub(channel, options)
  }
  
  def bindService(serviceImpl: TransactionsApi, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition =
    _root_.io.grpc.ServerServiceDefinition.builder(SERVICE)
    .addMethod(
      METHOD_GET_TRANSACTIONS,
      _root_.io.grpc.stub.ServerCalls.asyncServerStreamingCall(new _root_.io.grpc.stub.ServerCalls.ServerStreamingMethod[com.bdmplatform.api.grpc.TransactionsRequest, com.bdmplatform.api.grpc.TransactionResponse] {
        override def invoke(request: com.bdmplatform.api.grpc.TransactionsRequest, observer: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.TransactionResponse]): Unit =
          serviceImpl.getTransactions(request, observer)
      }))
    .addMethod(
      METHOD_GET_STATE_CHANGES,
      _root_.io.grpc.stub.ServerCalls.asyncServerStreamingCall(new _root_.io.grpc.stub.ServerCalls.ServerStreamingMethod[com.bdmplatform.api.grpc.TransactionsRequest, com.bdmplatform.protobuf.transaction.InvokeScriptResult] {
        override def invoke(request: com.bdmplatform.api.grpc.TransactionsRequest, observer: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.protobuf.transaction.InvokeScriptResult]): Unit =
          serviceImpl.getStateChanges(request, observer)
      }))
    .addMethod(
      METHOD_GET_STATUSES,
      _root_.io.grpc.stub.ServerCalls.asyncServerStreamingCall(new _root_.io.grpc.stub.ServerCalls.ServerStreamingMethod[com.bdmplatform.api.grpc.TransactionsByIdRequest, com.bdmplatform.api.grpc.TransactionStatus] {
        override def invoke(request: com.bdmplatform.api.grpc.TransactionsByIdRequest, observer: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.TransactionStatus]): Unit =
          serviceImpl.getStatuses(request, observer)
      }))
    .addMethod(
      METHOD_GET_UNCONFIRMED,
      _root_.io.grpc.stub.ServerCalls.asyncServerStreamingCall(new _root_.io.grpc.stub.ServerCalls.ServerStreamingMethod[com.bdmplatform.api.grpc.TransactionsRequest, com.bdmplatform.api.grpc.TransactionResponse] {
        override def invoke(request: com.bdmplatform.api.grpc.TransactionsRequest, observer: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.TransactionResponse]): Unit =
          serviceImpl.getUnconfirmed(request, observer)
      }))
    .addMethod(
      METHOD_SIGN,
      _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[com.bdmplatform.api.grpc.SignRequest, com.bdmplatform.protobuf.transaction.SignedTransaction] {
        override def invoke(request: com.bdmplatform.api.grpc.SignRequest, observer: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.protobuf.transaction.SignedTransaction]): Unit =
          serviceImpl.sign(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
            executionContext)
      }))
    .addMethod(
      METHOD_BROADCAST,
      _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[com.bdmplatform.protobuf.transaction.SignedTransaction, com.bdmplatform.protobuf.transaction.SignedTransaction] {
        override def invoke(request: com.bdmplatform.protobuf.transaction.SignedTransaction, observer: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.protobuf.transaction.SignedTransaction]): Unit =
          serviceImpl.broadcast(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
            executionContext)
      }))
    .build()
  
  def blockingStub(channel: _root_.io.grpc.Channel): TransactionsApiBlockingStub = new TransactionsApiBlockingStub(channel)
  
  def stub(channel: _root_.io.grpc.Channel): TransactionsApiStub = new TransactionsApiStub(channel)
  
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = com.bdmplatform.api.grpc.TransactionsApiProto.javaDescriptor.getServices().get(0)
  
}