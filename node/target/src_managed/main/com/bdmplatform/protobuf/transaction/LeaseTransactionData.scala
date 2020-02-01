// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package com.bdmplatform.protobuf.transaction

@SerialVersionUID(0L)
final case class LeaseTransactionData(
    recipient: _root_.scala.Option[com.bdmplatform.protobuf.transaction.Recipient] = None,
    amount: _root_.scala.Long = 0L
    ) extends scalapb.GeneratedMessage with scalapb.Message[LeaseTransactionData] with scalapb.lenses.Updatable[LeaseTransactionData] {
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      if (recipient.isDefined) {
        val __value = recipient.get
        __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
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
      recipient.foreach { __v =>
        val __m = __v
        _output__.writeTag(1, 2)
        _output__.writeUInt32NoTag(__m.serializedSize)
        __m.writeTo(_output__)
      };
      {
        val __v = amount
        if (__v != 0L) {
          _output__.writeInt64(2, __v)
        }
      };
    }
    def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): com.bdmplatform.protobuf.transaction.LeaseTransactionData = {
      var __recipient = this.recipient
      var __amount = this.amount
      var _done__ = false
      while (!_done__) {
        val _tag__ = _input__.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 10 =>
            __recipient = Option(_root_.scalapb.LiteParser.readMessage(_input__, __recipient.getOrElse(com.bdmplatform.protobuf.transaction.Recipient.defaultInstance)))
          case 16 =>
            __amount = _input__.readInt64()
          case tag => _input__.skipField(tag)
        }
      }
      com.bdmplatform.protobuf.transaction.LeaseTransactionData(
          recipient = __recipient,
          amount = __amount
      )
    }
    def getRecipient: com.bdmplatform.protobuf.transaction.Recipient = recipient.getOrElse(com.bdmplatform.protobuf.transaction.Recipient.defaultInstance)
    def clearRecipient: LeaseTransactionData = copy(recipient = None)
    def withRecipient(__v: com.bdmplatform.protobuf.transaction.Recipient): LeaseTransactionData = copy(recipient = Option(__v))
    def withAmount(__v: _root_.scala.Long): LeaseTransactionData = copy(amount = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => recipient.orNull
        case 2 => {
          val __t = amount
          if (__t != 0L) __t else null
        }
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => recipient.map(_.toPMessage).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 2 => _root_.scalapb.descriptors.PLong(amount)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)
    def companion = com.bdmplatform.protobuf.transaction.LeaseTransactionData
}

object LeaseTransactionData extends scalapb.GeneratedMessageCompanion[com.bdmplatform.protobuf.transaction.LeaseTransactionData] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[com.bdmplatform.protobuf.transaction.LeaseTransactionData] = this
  def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, _root_.scala.Any]): com.bdmplatform.protobuf.transaction.LeaseTransactionData = {
    _root_.scala.Predef.require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), "FieldDescriptor does not match message type.")
    val __fields = javaDescriptor.getFields
    com.bdmplatform.protobuf.transaction.LeaseTransactionData(
      __fieldsMap.get(__fields.get(0)).asInstanceOf[_root_.scala.Option[com.bdmplatform.protobuf.transaction.Recipient]],
      __fieldsMap.getOrElse(__fields.get(1), 0L).asInstanceOf[_root_.scala.Long]
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[com.bdmplatform.protobuf.transaction.LeaseTransactionData] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), "FieldDescriptor does not match message type.")
      com.bdmplatform.protobuf.transaction.LeaseTransactionData(
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).flatMap(_.as[_root_.scala.Option[com.bdmplatform.protobuf.transaction.Recipient]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.Long]).getOrElse(0L)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = TransactionProto.javaDescriptor.getMessageTypes.get(9)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = TransactionProto.scalaDescriptor.messages(9)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 1 => __out = com.bdmplatform.protobuf.transaction.Recipient
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = com.bdmplatform.protobuf.transaction.LeaseTransactionData(
  )
  implicit class LeaseTransactionDataLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, com.bdmplatform.protobuf.transaction.LeaseTransactionData]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, com.bdmplatform.protobuf.transaction.LeaseTransactionData](_l) {
    def recipient: _root_.scalapb.lenses.Lens[UpperPB, com.bdmplatform.protobuf.transaction.Recipient] = field(_.getRecipient)((c_, f_) => c_.copy(recipient = Option(f_)))
    def optionalRecipient: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Option[com.bdmplatform.protobuf.transaction.Recipient]] = field(_.recipient)((c_, f_) => c_.copy(recipient = f_))
    def amount: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Long] = field(_.amount)((c_, f_) => c_.copy(amount = f_))
  }
  final val RECIPIENT_FIELD_NUMBER = 1
  final val AMOUNT_FIELD_NUMBER = 2
  def of(
    recipient: _root_.scala.Option[com.bdmplatform.protobuf.transaction.Recipient],
    amount: _root_.scala.Long
  ): _root_.com.bdmplatform.protobuf.transaction.LeaseTransactionData = _root_.com.bdmplatform.protobuf.transaction.LeaseTransactionData(
    recipient,
    amount
  )
}
