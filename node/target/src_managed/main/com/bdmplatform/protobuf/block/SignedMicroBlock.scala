// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package com.bdmplatform.protobuf.block

@SerialVersionUID(0L)
final case class SignedMicroBlock(
    microBlock: _root_.scala.Option[com.bdmplatform.protobuf.block.MicroBlock] = None,
    signature: _root_.com.google.protobuf.ByteString = _root_.com.google.protobuf.ByteString.EMPTY
    ) extends scalapb.GeneratedMessage with scalapb.Message[SignedMicroBlock] with scalapb.lenses.Updatable[SignedMicroBlock] {
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      if (microBlock.isDefined) {
        val __value = microBlock.get
        __size += 1 + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__value.serializedSize) + __value.serializedSize
      };
      
      {
        val __value = signature
        if (__value != _root_.com.google.protobuf.ByteString.EMPTY) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeBytesSize(2, __value)
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
      microBlock.foreach { __v =>
        val __m = __v
        _output__.writeTag(1, 2)
        _output__.writeUInt32NoTag(__m.serializedSize)
        __m.writeTo(_output__)
      };
      {
        val __v = signature
        if (__v != _root_.com.google.protobuf.ByteString.EMPTY) {
          _output__.writeBytes(2, __v)
        }
      };
    }
    def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): com.bdmplatform.protobuf.block.SignedMicroBlock = {
      var __microBlock = this.microBlock
      var __signature = this.signature
      var _done__ = false
      while (!_done__) {
        val _tag__ = _input__.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 10 =>
            __microBlock = Option(_root_.scalapb.LiteParser.readMessage(_input__, __microBlock.getOrElse(com.bdmplatform.protobuf.block.MicroBlock.defaultInstance)))
          case 18 =>
            __signature = _input__.readBytes()
          case tag => _input__.skipField(tag)
        }
      }
      com.bdmplatform.protobuf.block.SignedMicroBlock(
          microBlock = __microBlock,
          signature = __signature
      )
    }
    def getMicroBlock: com.bdmplatform.protobuf.block.MicroBlock = microBlock.getOrElse(com.bdmplatform.protobuf.block.MicroBlock.defaultInstance)
    def clearMicroBlock: SignedMicroBlock = copy(microBlock = None)
    def withMicroBlock(__v: com.bdmplatform.protobuf.block.MicroBlock): SignedMicroBlock = copy(microBlock = Option(__v))
    def withSignature(__v: _root_.com.google.protobuf.ByteString): SignedMicroBlock = copy(signature = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => microBlock.orNull
        case 2 => {
          val __t = signature
          if (__t != _root_.com.google.protobuf.ByteString.EMPTY) __t else null
        }
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => microBlock.map(_.toPMessage).getOrElse(_root_.scalapb.descriptors.PEmpty)
        case 2 => _root_.scalapb.descriptors.PByteString(signature)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToUnicodeString(this)
    def companion = com.bdmplatform.protobuf.block.SignedMicroBlock
}

object SignedMicroBlock extends scalapb.GeneratedMessageCompanion[com.bdmplatform.protobuf.block.SignedMicroBlock] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[com.bdmplatform.protobuf.block.SignedMicroBlock] = this
  def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, _root_.scala.Any]): com.bdmplatform.protobuf.block.SignedMicroBlock = {
    _root_.scala.Predef.require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), "FieldDescriptor does not match message type.")
    val __fields = javaDescriptor.getFields
    com.bdmplatform.protobuf.block.SignedMicroBlock(
      __fieldsMap.get(__fields.get(0)).asInstanceOf[_root_.scala.Option[com.bdmplatform.protobuf.block.MicroBlock]],
      __fieldsMap.getOrElse(__fields.get(1), _root_.com.google.protobuf.ByteString.EMPTY).asInstanceOf[_root_.com.google.protobuf.ByteString]
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[com.bdmplatform.protobuf.block.SignedMicroBlock] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), "FieldDescriptor does not match message type.")
      com.bdmplatform.protobuf.block.SignedMicroBlock(
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).flatMap(_.as[_root_.scala.Option[com.bdmplatform.protobuf.block.MicroBlock]]),
        __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.com.google.protobuf.ByteString]).getOrElse(_root_.com.google.protobuf.ByteString.EMPTY)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = BlockProto.javaDescriptor.getMessageTypes.get(2)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = BlockProto.scalaDescriptor.messages(2)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = {
    var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null
    (__number: @_root_.scala.unchecked) match {
      case 1 => __out = com.bdmplatform.protobuf.block.MicroBlock
    }
    __out
  }
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__fieldNumber)
  lazy val defaultInstance = com.bdmplatform.protobuf.block.SignedMicroBlock(
  )
  implicit class SignedMicroBlockLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, com.bdmplatform.protobuf.block.SignedMicroBlock]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, com.bdmplatform.protobuf.block.SignedMicroBlock](_l) {
    def microBlock: _root_.scalapb.lenses.Lens[UpperPB, com.bdmplatform.protobuf.block.MicroBlock] = field(_.getMicroBlock)((c_, f_) => c_.copy(microBlock = Option(f_)))
    def optionalMicroBlock: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Option[com.bdmplatform.protobuf.block.MicroBlock]] = field(_.microBlock)((c_, f_) => c_.copy(microBlock = f_))
    def signature: _root_.scalapb.lenses.Lens[UpperPB, _root_.com.google.protobuf.ByteString] = field(_.signature)((c_, f_) => c_.copy(signature = f_))
  }
  final val MICRO_BLOCK_FIELD_NUMBER = 1
  final val SIGNATURE_FIELD_NUMBER = 2
  def of(
    microBlock: _root_.scala.Option[com.bdmplatform.protobuf.block.MicroBlock],
    signature: _root_.com.google.protobuf.ByteString
  ): _root_.com.bdmplatform.protobuf.block.SignedMicroBlock = _root_.com.bdmplatform.protobuf.block.SignedMicroBlock(
    microBlock,
    signature
  )
}
