package com.bdmplatform.protobuf.transaction
import com.google.common.primitives.Bytes
import com.google.protobuf.ByteString
import com.bdmplatform.account._
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.crypto
import com.bdmplatform.lang.ValidationError
import com.bdmplatform.transaction.TxValidationError.GenericError
import _root_.com.bdmplatform.transaction.TxValidationError.GenericError


import com.bdmplatform.account.{AddressScheme, PublicKey}
import com.bdmplatform.protobuf.order.AssetPair
import com.bdmplatform.transaction.assets.exchange.{OrderV1, OrderV2}
import _root_.com.bdmplatform.transaction.assets.exchange.{OrderV1, OrderV2}
import com.bdmplatform.{transaction => vt}



import _root_.com.bdmplatform.account.{Address, AddressScheme}
//import com.bdmplatform.common.state.ByteStr.{Address, toByteArray}
//import _root_.com.bdmplatform.common.state.ByteStr.{Address, toByteArray}
import _root_.com.bdmplatform.common.state.ByteStr.toByteArray
import _root_.com.bdmplatform.common.state.ByteStr.{fromByteArray, toByteArray, fromBytes, fromLong}

import _root_.com.bdmplatform.common.state.ByteStr
import com.bdmplatform.common.state._
import _root_.com.bdmplatform.common.state._


import com.google.protobuf.ByteString
import com.bdmplatform.account.{Address, AddressScheme, PublicKey}
import com.bdmplatform.lang.script.ScriptReader
import com.bdmplatform.protobuf.Amount
import _root_.com.bdmplatform.protobuf.transaction.Transaction._
import com.bdmplatform.protobuf.transaction.Transaction._
import com.bdmplatform.protobuf.transaction.Transaction.Data
import _root_.com.bdmplatform.protobuf.transaction.Transaction.Data
import com.bdmplatform.protobuf.transaction.{Script => PBScript}
import com.bdmplatform.serialization.Deser
import com.bdmplatform.state.{BinaryDataEntry, BooleanDataEntry, IntegerDataEntry, StringDataEntry}
import com.bdmplatform.transaction.Asset.{IssuedAsset, Bdm}
import com.bdmplatform.transaction.transfer.MassTransferTransaction
import com.bdmplatform.transaction.transfer.MassTransferTransaction.ParsedTransfer
import com.bdmplatform.transaction.{Proofs, TxValidationError}
import com.bdmplatform.common.utils.EitherExt2




object PBRecipients {
  //==========
import _root_.com.bdmplatform.account.{Address, AddressScheme}
//import com.bdmplatform.common.state.ByteStr.{Address, toByteArray}
//import _root_.com.bdmplatform.common.state.ByteStr.{Address, toByteArray}
import _root_.com.bdmplatform.common.state.ByteStr.toByteArray
import _root_.com.bdmplatform.common.state.ByteStr.{fromByteArray, toByteArray, fromBytes, fromLong}

import _root_.com.bdmplatform.common.state.ByteStr
import com.bdmplatform.common.state._
import _root_.com.bdmplatform.common.state._
  //=========

  def create(addressOrAlias: AddressOrAlias): Recipient = addressOrAlias match {
//    case a: Address => Recipient().withAddress(ByteString.copyFrom(a.bytes.arr.slice(2, a.bytes.arr.length - Address.ChecksumLength)))

    case a: Address => Recipient().withPublicKeyHash(ByteString.copyFrom(a.bytes.arr.slice(2, a.bytes.arr.length - Address.ChecksumLength)))
 
    case a: Alias   => Recipient().withAlias(a.name)
    case _          => sys.error("Should not happen " + addressOrAlias)
  }

  def toAddress(bytes: ByteStr): Either[ValidationError, Address] = bytes.length match {
    case Address.HashLength => // Compressed address
      val withHeader = Bytes.concat(Array(Address.AddressVersion, AddressScheme.current.chainId), bytes)
      val checksum   = Address.calcCheckSum(withHeader)
      Address.fromBytes(Bytes.concat(withHeader, checksum))

    case Address.AddressLength => // Regular address
      Address.fromBytes(bytes)

    case crypto.KeyLength => // Public key
      Right(PublicKey(bytes).toAddress)

    case _ =>
      Left(GenericError(s"Invalid address length: ${bytes.length}"))
  }

  def toAddress(r: Recipient): Either[ValidationError, Address] = r.recipient match {
    case Recipient.Recipient.Address(bytes) => toAddress(bytes.toByteArray)
    case _                                  => Left(GenericError(s"Not an address: $r"))
  }

  def toAlias(r: Recipient): Either[ValidationError, Alias] = r.recipient match {
    case Recipient.Recipient.Alias(alias) => Alias.create(alias)
    case _                                => Left(GenericError(s"Not an alias: $r"))
  }

  def toAddressOrAlias(r: Recipient): Either[ValidationError, AddressOrAlias] = {
    if (r.recipient.isAddress) toAddress(r)
    else if (r.recipient.isAlias) toAlias(r)
    else Left(GenericError(s"Not an address or alias: $r"))
  }

//========================
//
//  implicit def toByteArray(bs: ByteStr): Array[Byte] = {
//    bs.arr
//  }
//
//========================

}
