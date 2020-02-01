package com.bdmplatform.transaction

import com.google.common.primitives.{Bytes, Longs}
import com.bdmplatform.account.Alias
import com.bdmplatform.serialization.Deser
import com.bdmplatform.transaction.Asset.Bdm
import monix.eval.Coeval
import play.api.libs.json.{JsObject, Json}

trait CreateAliasTransaction extends ProvenTransaction with VersionedTransaction {
  def alias: Alias
  def fee: Long
  def timestamp: Long

  override val assetFee: (Asset, Long) = (Bdm, fee)

  override val json: Coeval[JsObject] = Coeval.evalOnce(
    jsonBase() ++ Json.obj(
      "version"   -> version,
      "alias"     -> alias.name,
      "fee"       -> fee,
      "timestamp" -> timestamp
    ))

  val baseBytes: Coeval[Array[Byte]] = Coeval.evalOnce {
    Bytes.concat(
      sender,
      Deser.serializeArray(alias.bytes.arr),
      Longs.toByteArray(fee),
      Longs.toByteArray(timestamp)
    )
  }
}

object CreateAliasTransaction {
  val typeId: Byte = 10
}
