package com.bdmplatform.api.grpc

object AccountsApiGrpc {
  val METHOD_GET_BALANCES: _root_.io.grpc.MethodDescriptor[com.bdmplatform.api.grpc.BalancesRequest, com.bdmplatform.api.grpc.BalanceResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("bdm.node.grpc.AccountsApi", "GetBalances"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.BalancesRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.BalanceResponse])
      .build()
  
  val METHOD_GET_SCRIPT: _root_.io.grpc.MethodDescriptor[com.bdmplatform.api.grpc.AccountRequest, com.bdmplatform.api.grpc.ScriptData] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("bdm.node.grpc.AccountsApi", "GetScript"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.AccountRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.ScriptData])
      .build()
  
  val METHOD_GET_ACTIVE_LEASES: _root_.io.grpc.MethodDescriptor[com.bdmplatform.api.grpc.AccountRequest, com.bdmplatform.api.grpc.TransactionResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("bdm.node.grpc.AccountsApi", "GetActiveLeases"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.AccountRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.TransactionResponse])
      .build()
  
  val METHOD_GET_DATA_ENTRIES: _root_.io.grpc.MethodDescriptor[com.bdmplatform.api.grpc.DataRequest, com.bdmplatform.api.grpc.DataEntryResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("bdm.node.grpc.AccountsApi", "GetDataEntries"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.DataRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.bdmplatform.api.grpc.DataEntryResponse])
      .build()
  
  val METHOD_RESOLVE_ALIAS: _root_.io.grpc.MethodDescriptor[com.google.protobuf.wrappers.StringValue, com.google.protobuf.wrappers.BytesValue] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("bdm.node.grpc.AccountsApi", "ResolveAlias"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.wrappers.StringValue])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[com.google.protobuf.wrappers.BytesValue])
      .build()
  
  val SERVICE: _root_.io.grpc.ServiceDescriptor =
    _root_.io.grpc.ServiceDescriptor.newBuilder("bdm.node.grpc.AccountsApi")
      .setSchemaDescriptor(new _root_.scalapb.grpc.ConcreteProtoFileDescriptorSupplier(com.bdmplatform.api.grpc.AccountsApiProto.javaDescriptor))
      .addMethod(METHOD_GET_BALANCES)
      .addMethod(METHOD_GET_SCRIPT)
      .addMethod(METHOD_GET_ACTIVE_LEASES)
      .addMethod(METHOD_GET_DATA_ENTRIES)
      .addMethod(METHOD_RESOLVE_ALIAS)
      .build()
  
  trait AccountsApi extends _root_.scalapb.grpc.AbstractService {
    override def serviceCompanion = AccountsApi
    def getBalances(request: com.bdmplatform.api.grpc.BalancesRequest, responseObserver: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.BalanceResponse]): Unit
    def getScript(request: com.bdmplatform.api.grpc.AccountRequest): scala.concurrent.Future[com.bdmplatform.api.grpc.ScriptData]
    def getActiveLeases(request: com.bdmplatform.api.grpc.AccountRequest, responseObserver: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.TransactionResponse]): Unit
    def getDataEntries(request: com.bdmplatform.api.grpc.DataRequest, responseObserver: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.DataEntryResponse]): Unit
    def resolveAlias(request: com.google.protobuf.wrappers.StringValue): scala.concurrent.Future[com.google.protobuf.wrappers.BytesValue]
  }
  
  object AccountsApi extends _root_.scalapb.grpc.ServiceCompanion[AccountsApi] {
    implicit def serviceCompanion: _root_.scalapb.grpc.ServiceCompanion[AccountsApi] = this
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = com.bdmplatform.api.grpc.AccountsApiProto.javaDescriptor.getServices().get(0)
  }
  
  trait AccountsApiBlockingClient {
    def serviceCompanion = AccountsApi
    def getBalances(request: com.bdmplatform.api.grpc.BalancesRequest): scala.collection.Iterator[com.bdmplatform.api.grpc.BalanceResponse]
    def getScript(request: com.bdmplatform.api.grpc.AccountRequest): com.bdmplatform.api.grpc.ScriptData
    def getActiveLeases(request: com.bdmplatform.api.grpc.AccountRequest): scala.collection.Iterator[com.bdmplatform.api.grpc.TransactionResponse]
    def getDataEntries(request: com.bdmplatform.api.grpc.DataRequest): scala.collection.Iterator[com.bdmplatform.api.grpc.DataEntryResponse]
    def resolveAlias(request: com.google.protobuf.wrappers.StringValue): com.google.protobuf.wrappers.BytesValue
  }
  
  class AccountsApiBlockingStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[AccountsApiBlockingStub](channel, options) with AccountsApiBlockingClient {
    override def getBalances(request: com.bdmplatform.api.grpc.BalancesRequest): scala.collection.Iterator[com.bdmplatform.api.grpc.BalanceResponse] = {
      _root_.scalapb.grpc.ClientCalls.blockingServerStreamingCall(channel, METHOD_GET_BALANCES, options, request)
    }
    
    override def getScript(request: com.bdmplatform.api.grpc.AccountRequest): com.bdmplatform.api.grpc.ScriptData = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_GET_SCRIPT, options, request)
    }
    
    override def getActiveLeases(request: com.bdmplatform.api.grpc.AccountRequest): scala.collection.Iterator[com.bdmplatform.api.grpc.TransactionResponse] = {
      _root_.scalapb.grpc.ClientCalls.blockingServerStreamingCall(channel, METHOD_GET_ACTIVE_LEASES, options, request)
    }
    
    override def getDataEntries(request: com.bdmplatform.api.grpc.DataRequest): scala.collection.Iterator[com.bdmplatform.api.grpc.DataEntryResponse] = {
      _root_.scalapb.grpc.ClientCalls.blockingServerStreamingCall(channel, METHOD_GET_DATA_ENTRIES, options, request)
    }
    
    override def resolveAlias(request: com.google.protobuf.wrappers.StringValue): com.google.protobuf.wrappers.BytesValue = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_RESOLVE_ALIAS, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): AccountsApiBlockingStub = new AccountsApiBlockingStub(channel, options)
  }
  
  class AccountsApiStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[AccountsApiStub](channel, options) with AccountsApi {
    override def getBalances(request: com.bdmplatform.api.grpc.BalancesRequest, responseObserver: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.BalanceResponse]): Unit = {
      _root_.scalapb.grpc.ClientCalls.asyncServerStreamingCall(channel, METHOD_GET_BALANCES, options, request, responseObserver)
    }
    
    override def getScript(request: com.bdmplatform.api.grpc.AccountRequest): scala.concurrent.Future[com.bdmplatform.api.grpc.ScriptData] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_GET_SCRIPT, options, request)
    }
    
    override def getActiveLeases(request: com.bdmplatform.api.grpc.AccountRequest, responseObserver: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.TransactionResponse]): Unit = {
      _root_.scalapb.grpc.ClientCalls.asyncServerStreamingCall(channel, METHOD_GET_ACTIVE_LEASES, options, request, responseObserver)
    }
    
    override def getDataEntries(request: com.bdmplatform.api.grpc.DataRequest, responseObserver: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.DataEntryResponse]): Unit = {
      _root_.scalapb.grpc.ClientCalls.asyncServerStreamingCall(channel, METHOD_GET_DATA_ENTRIES, options, request, responseObserver)
    }
    
    override def resolveAlias(request: com.google.protobuf.wrappers.StringValue): scala.concurrent.Future[com.google.protobuf.wrappers.BytesValue] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_RESOLVE_ALIAS, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): AccountsApiStub = new AccountsApiStub(channel, options)
  }
  
  def bindService(serviceImpl: AccountsApi, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition =
    _root_.io.grpc.ServerServiceDefinition.builder(SERVICE)
    .addMethod(
      METHOD_GET_BALANCES,
      _root_.io.grpc.stub.ServerCalls.asyncServerStreamingCall(new _root_.io.grpc.stub.ServerCalls.ServerStreamingMethod[com.bdmplatform.api.grpc.BalancesRequest, com.bdmplatform.api.grpc.BalanceResponse] {
        override def invoke(request: com.bdmplatform.api.grpc.BalancesRequest, observer: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.BalanceResponse]): Unit =
          serviceImpl.getBalances(request, observer)
      }))
    .addMethod(
      METHOD_GET_SCRIPT,
      _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[com.bdmplatform.api.grpc.AccountRequest, com.bdmplatform.api.grpc.ScriptData] {
        override def invoke(request: com.bdmplatform.api.grpc.AccountRequest, observer: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.ScriptData]): Unit =
          serviceImpl.getScript(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
            executionContext)
      }))
    .addMethod(
      METHOD_GET_ACTIVE_LEASES,
      _root_.io.grpc.stub.ServerCalls.asyncServerStreamingCall(new _root_.io.grpc.stub.ServerCalls.ServerStreamingMethod[com.bdmplatform.api.grpc.AccountRequest, com.bdmplatform.api.grpc.TransactionResponse] {
        override def invoke(request: com.bdmplatform.api.grpc.AccountRequest, observer: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.TransactionResponse]): Unit =
          serviceImpl.getActiveLeases(request, observer)
      }))
    .addMethod(
      METHOD_GET_DATA_ENTRIES,
      _root_.io.grpc.stub.ServerCalls.asyncServerStreamingCall(new _root_.io.grpc.stub.ServerCalls.ServerStreamingMethod[com.bdmplatform.api.grpc.DataRequest, com.bdmplatform.api.grpc.DataEntryResponse] {
        override def invoke(request: com.bdmplatform.api.grpc.DataRequest, observer: _root_.io.grpc.stub.StreamObserver[com.bdmplatform.api.grpc.DataEntryResponse]): Unit =
          serviceImpl.getDataEntries(request, observer)
      }))
    .addMethod(
      METHOD_RESOLVE_ALIAS,
      _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[com.google.protobuf.wrappers.StringValue, com.google.protobuf.wrappers.BytesValue] {
        override def invoke(request: com.google.protobuf.wrappers.StringValue, observer: _root_.io.grpc.stub.StreamObserver[com.google.protobuf.wrappers.BytesValue]): Unit =
          serviceImpl.resolveAlias(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
            executionContext)
      }))
    .build()
  
  def blockingStub(channel: _root_.io.grpc.Channel): AccountsApiBlockingStub = new AccountsApiBlockingStub(channel)
  
  def stub(channel: _root_.io.grpc.Channel): AccountsApiStub = new AccountsApiStub(channel)
  
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = com.bdmplatform.api.grpc.AccountsApiProto.javaDescriptor.getServices().get(0)
  
}