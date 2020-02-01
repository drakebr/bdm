package com.bdmplatform.protobuf.transaction
import com.google.protobuf.ByteString
import com.bdmplatform.protobuf.Amount
import com.bdmplatform.transaction.Asset
import com.bdmplatform.transaction.Asset.{IssuedAsset, Bdm}

object PBAmounts {
  def toPBAssetId(asset: Asset): ByteString = asset match {
    case Asset.IssuedAsset(id) =>
      ByteString.copyFrom(id)

    case Asset.Bdm =>
      ByteString.EMPTY
  }

  def toVanillaAssetId(byteStr: ByteString): Asset = {
    if (byteStr.isEmpty) Bdm
    else IssuedAsset(byteStr.toByteArray)
  }

  def fromAssetAndAmount(asset: Asset, amount: Long): Amount =
    Amount(toPBAssetId(asset), amount)

  def toAssetAndAmount(value: Amount): (Asset, Long) =
    (toVanillaAssetId(value.assetId), value.amount)
}
