// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package com.bdmplatform.protobuf.transaction

@SerialVersionUID(0L)
final case class PaymentTransactionData(
    recipientAddress: _root_.com.google.protobuf.ByteString = _root_.com.google.protobuf.ByteString.EMPTY,
    amount: _root_.scala.Long = 0L
    ) extends scalapb.GeneratedMessage with scalapb.Message[PaymentTransactionData] with scalapb.lenses.Updatable[PaymentTransactionData] {
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      
      {
        val __value = recipientAddress
        if (__value != _root_.com.google.protobuf.ByteString.EMPTY) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeBytesSize(1, __value)
        }
      };
      
      {
        val __value = amount
        if (__value != 0L) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(2, __value)
        }
      };
      __size
    }
    final override def serializedSize: _root_.scala.Int = {
      var read = __serializedSizeCachedValue
      if (read == 0) {
        read = __computeSerializedValue()
        __serializedSizeCachedValue = read
      }
      read
    }
    def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): _root_.scala.Unit = {
      {
        val __v = recipientAddress
        if (__v != _root_.com.google.protobuf.ByteString.EMPTY) {
          _output__.writeBytes(1, __v)
        }
      };
      {
        val __v = amount
        if (__v != 0L) {
          _output__.writeInt64(2, __v)
        }
      };
    }
    def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): com.bdmplatform.protobuf.transaction.PaymentTransactionData = {
      var __recipientAddress = this.recipientAddress
      var __amount = this.amount
      var _done__ = false
      while (!_done__) {
        val _tag__ = _input__.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 10 =>
            __recipientAddress = _input__.readBytes()
          case 16 =>
            __amount = _input__.readInt64()
          case tag => _input__.skipField(tag)
        }
      }
      com.bdmplatform.protobuf.transaction.PaymentTransactionData(
          recipientAddress = __recipientAddress,
          amount = __amount
      )
    }
    def withRecipientAddress(__v: _root_.com.google.protobuf.ByteString): PaymentTransactionData = copy(recipientAddress = __v)
    def withAmount(__v: _root_.scala.Long): PaymentTransactionData = copy(amount = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => {
          val __t = recipientAddress
          if (__t != _root_.com.google.protobuf.ByteString.EMPTY) __t else null
        }
        case 2 => {
          val __t = amount
          if (__t != 0L) __t else null
        }
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PByteString(recipientAddress)
        case 2 => _root_.scalapb.descriptors.PLong(amount)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)
    def companion = com.bdmplatform.protobuf.transaction.PaymentTransactionData
}

object PaymentTransactionData extends scalapb.GeneratedMessageCompanion[com.bdmplatform.protobuf.transaction.PaymentTransactionData] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[com.bdmplatform.protobuf.transaction.PaymentTransactionData] = this
  def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, _root_.scala.Any]): com.bdmplatform.protobuf.transaction.PaymentTransactionData = {
    _root_.scala.Predef.require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), "FieldDescriptor does not match message type.")
    val __fields = javaDescriptor.getFields
    com.bdmplatform.protobuf.transaction.PaymentTransactionData(
      __fieldsMap.getOrElse(__fields.get(0), _root_.com.google.protobuf.ByteString.EMPTY).asInstanceOf[_root_.com.google.protobuf.ByteString],
      __fieldsMap.getOrElse(__fields.get(1), 0L).asInstanceOf[_root_.scala.Long]
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[com.bdmplatform.protobuf.transaction.PaymentTransactionData] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), "FieldDescriptor does not match message type.")
      com.bdmplatform.protobuf.transaction.PaymentTransactionData(
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.com.google.protobuf.ByteString]).getOrElse(_root_.com.google.protobuf.ByteString.EMPTY),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.Long]).getOrElse(0L)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = TransactionProto.javaDescriptor.getMessageTypes.get(3)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = TransactionProto.scalaDescriptor.messages(3)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__number)
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = com.bdmplatform.protobuf.transaction.PaymentTransactionData(
  )
  implicit class PaymentTransactionDataLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, com.bdmplatform.protobuf.transaction.PaymentTransactionData]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, com.bdmplatform.protobuf.transaction.PaymentTransactionData](_l) {
    def recipientAddress: _root_.scalapb.lenses.Lens[UpperPB, _root_.com.google.protobuf.ByteString] = field(_.recipientAddress)((c_, f_) => c_.copy(recipientAddress = f_))
    def amount: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Long] = field(_.amount)((c_, f_) => c_.copy(amount = f_))
  }
  final val RECIPIENT_ADDRESS_FIELD_NUMBER = 1
  final val AMOUNT_FIELD_NUMBER = 2
  def of(
    recipientAddress: _root_.com.google.protobuf.ByteString,
    amount: _root_.scala.Long
  ): _root_.com.bdmplatform.protobuf.transaction.PaymentTransactionData = _root_.com.bdmplatform.protobuf.transaction.PaymentTransactionData(
    recipientAddress,
    amount
  )
}
