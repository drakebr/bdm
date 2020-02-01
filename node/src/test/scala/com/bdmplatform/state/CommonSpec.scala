package com.bdmplatform.state

import com.bdmplatform.account.Address
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.crypto.SignatureLength
import com.bdmplatform.db.WithDomain
import com.bdmplatform.lagonaki.mocks.TestBlock
import com.bdmplatform.transaction.Asset.IssuedAsset
import com.bdmplatform.transaction.GenesisTransaction
import com.bdmplatform.{NoShrink, TestTime, TransactionGen}
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.scalacheck.{ScalaCheckPropertyChecks => PropertyChecks}

class CommonSpec extends FreeSpec with Matchers with WithDomain with TransactionGen with PropertyChecks with NoShrink {
  private val time          = new TestTime
  private def nextTs        = time.getTimestamp()
  private val AssetIdLength = 32

  private def genesisBlock(genesisTs: Long, address: Address, initialBalance: Long) = TestBlock.create(
    genesisTs,
    ByteStr(Array.fill[Byte](SignatureLength)(0)),
    Seq(GenesisTransaction.create(address, initialBalance, genesisTs).explicitGet())
  )

  "Common Conditions" - {
    "Zero balance of absent asset" in forAll(accountGen, positiveLongGen, byteArrayGen(AssetIdLength)) {
      case (sender, initialBalance, assetId) =>
        withDomain() { d =>
          d.appendBlock(genesisBlock(nextTs, sender, initialBalance))
          d.portfolio(sender).balanceOf(IssuedAsset(ByteStr(assetId))) shouldEqual 0L
        }
    }
  }
}
