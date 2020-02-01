package com.bdmplatform.transaction.lease

import com.google.common.primitives.{Bytes, Longs}
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.crypto
import com.bdmplatform.lang.ValidationError
import com.bdmplatform.transaction.Asset.Bdm
import com.bdmplatform.transaction.TxValidationError._
import com.bdmplatform.transaction.{Asset, ProvenTransaction, VersionedTransaction}
import monix.eval.Coeval
import play.api.libs.json.{JsObject, Json}

trait LeaseCancelTransaction extends ProvenTransaction with VersionedTransaction {
  def chainByte: Option[Byte]
  def leaseId: ByteStr
  def fee: Long
  override val assetFee: (Asset, Long) = (Bdm, fee)

  override val json: Coeval[JsObject] = Coeval.evalOnce(
    jsonBase() ++ Json.obj(
      "chainId"   -> chainByte,
      "version"   -> version,
      "fee"       -> fee,
      "timestamp" -> timestamp,
      "leaseId"   -> leaseId.base58
    ))
  protected val bytesBase = Coeval.evalOnce(Bytes.concat(sender, Longs.toByteArray(fee), Longs.toByteArray(timestamp), leaseId.arr))

}

object LeaseCancelTransaction {

  val typeId: Byte = 9

  def validateLeaseCancelParams(tx: LeaseCancelTransaction): Either[ValidationError, Unit] = {
    validateLeaseCancelParams(tx.leaseId, tx.fee)
  }

  def validateLeaseCancelParams(leaseId: ByteStr, fee: Long): Either[ValidationError, Unit] =
    if (leaseId.arr.length != crypto.DigestSize) {
      Left(GenericError("Lease transaction id is invalid"))
    } else if (fee <= 0) {
      Left(InsufficientFee())
    } else Right(())
}
