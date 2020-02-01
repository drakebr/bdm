package com.bdmplatform.consensus.nxt

import com.bdmplatform.account.{KeyPair, Address}
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.consensus.TransactionsOrdering
import com.bdmplatform.transaction.Asset
import com.bdmplatform.transaction.Asset.Bdm
import com.bdmplatform.transaction.transfer._
import org.scalatest.{Assertions, Matchers, PropSpec}

import scala.util.Random

class TransactionsOrderingSpecification extends PropSpec with Assertions with Matchers {

  property("TransactionsOrdering.InBlock should sort correctly") {
    val correctSeq = Seq(
      TransferTransactionV1
        .selfSigned(
          Bdm,
          KeyPair(Array.fill(32)(0: Byte)),
          Address.fromString("3MydsP4UeQdGwBq7yDbMvf9MzfB2pxFoUKU").explicitGet(),
          100000,
          1,
          Bdm,
          125L,
          Array.empty
        )
        .right
        .get,
      TransferTransactionV1
        .selfSigned(Bdm,
                    KeyPair(Array.fill(32)(0: Byte)),
                    Address.fromString("3MydsP4UeQdGwBq7yDbMvf9MzfB2pxFoUKU").explicitGet(),
                    100000,
                    2,
                    Bdm,
                    124L,
                    Array.empty)
        .right
        .get,
      TransferTransactionV1
        .selfSigned(Bdm,
                    KeyPair(Array.fill(32)(0: Byte)),
                    Address.fromString("3MydsP4UeQdGwBq7yDbMvf9MzfB2pxFoUKU").explicitGet(),
                    100000,
                    1,
                    Bdm,
                    124L,
                    Array.empty)
        .right
        .get,
      TransferTransactionV1
        .selfSigned(
          Bdm,
          KeyPair(Array.fill(32)(0: Byte)),
          Address.fromString("3MydsP4UeQdGwBq7yDbMvf9MzfB2pxFoUKU").explicitGet(),
          100000,
          2,
          Asset.fromCompatId(Some(ByteStr.empty)),
          124L,
          Array.empty
        )
        .right
        .get,
      TransferTransactionV1
        .selfSigned(
          Bdm,
          KeyPair(Array.fill(32)(0: Byte)),
          Address.fromString("3MydsP4UeQdGwBq7yDbMvf9MzfB2pxFoUKU").explicitGet(),
          100000,
          1,
          Asset.fromCompatId(Some(ByteStr.empty)),
          124L,
          Array.empty
        )
        .right
        .get
    )

    val sorted = Random.shuffle(correctSeq).sorted(TransactionsOrdering.InBlock)

    sorted shouldBe correctSeq
  }

  property("TransactionsOrdering.InUTXPool should sort correctly") {
    val correctSeq = Seq(
      TransferTransactionV1
        .selfSigned(
          Bdm,
          KeyPair(Array.fill(32)(0: Byte)),
          Address.fromString("3MydsP4UeQdGwBq7yDbMvf9MzfB2pxFoUKU").explicitGet(),
          100000,
          1,
          Bdm,
          124L,
          Array.empty
        )
        .right
        .get,
      TransferTransactionV1
        .selfSigned(
          Bdm,
          KeyPair(Array.fill(32)(0: Byte)),
          Address.fromString("3MydsP4UeQdGwBq7yDbMvf9MzfB2pxFoUKU").explicitGet(),
          100000,
          1,
          Bdm,
          123L,
          Array.empty
        )
        .right
        .get,
      TransferTransactionV1
        .selfSigned(
          Bdm,
          KeyPair(Array.fill(32)(0: Byte)),
          Address.fromString("3MydsP4UeQdGwBq7yDbMvf9MzfB2pxFoUKU").explicitGet(),
          100000,
          2,
          Bdm,
          123L,
          Array.empty
        )
        .right
        .get,
      TransferTransactionV1
        .selfSigned(
          Bdm,
          KeyPair(Array.fill(32)(0: Byte)),
          Address.fromString("3MydsP4UeQdGwBq7yDbMvf9MzfB2pxFoUKU").explicitGet(),
          100000,
          1,
          Asset.fromCompatId(Some(ByteStr.empty)),
          124L,
          Array.empty
        )
        .right
        .get,
      TransferTransactionV1
        .selfSigned(
          Bdm,
          KeyPair(Array.fill(32)(0: Byte)),
          Address.fromString("3MydsP4UeQdGwBq7yDbMvf9MzfB2pxFoUKU").explicitGet(),
          100000,
          2,
          Asset.fromCompatId(Some(ByteStr.empty)),
          124L,
          Array.empty
        )
        .right
        .get
    )

    val sorted = Random.shuffle(correctSeq).sorted(TransactionsOrdering.InUTXPool)

    sorted shouldBe correctSeq
  }

  property("TransactionsOrdering.InBlock should sort txs by decreasing block timestamp") {
    val correctSeq = Seq(
      TransferTransactionV1
        .selfSigned(
          Bdm,
          KeyPair(Array.fill(32)(0: Byte)),
          Address.fromString("3MydsP4UeQdGwBq7yDbMvf9MzfB2pxFoUKU").explicitGet(),
          100000,
          124L,
          Bdm,
          1,
          Array()
        )
        .right
        .get,
      TransferTransactionV1
        .selfSigned(
          Bdm,
          KeyPair(Array.fill(32)(0: Byte)),
          Address.fromString("3MydsP4UeQdGwBq7yDbMvf9MzfB2pxFoUKU").explicitGet(),
          100000,
          123L,
          Bdm,
          1,
          Array()
        )
        .right
        .get
    )

    Random.shuffle(correctSeq).sorted(TransactionsOrdering.InBlock) shouldBe correctSeq
  }

  property("TransactionsOrdering.InUTXPool should sort txs by ascending block timestamp") {
    val correctSeq = Seq(
      TransferTransactionV1
        .selfSigned(
          Bdm,
          KeyPair(Array.fill(32)(0: Byte)),
          Address.fromString("3MydsP4UeQdGwBq7yDbMvf9MzfB2pxFoUKU").explicitGet(),
          100000,
          123L,
          Bdm,
          1,
          Array()
        )
        .right
        .get,
      TransferTransactionV1
        .selfSigned(
          Bdm,
          KeyPair(Array.fill(32)(0: Byte)),
          Address.fromString("3MydsP4UeQdGwBq7yDbMvf9MzfB2pxFoUKU").explicitGet(),
          100000,
          124L,
          Bdm,
          1,
          Array()
        )
        .right
        .get
    )
    Random.shuffle(correctSeq).sorted(TransactionsOrdering.InUTXPool) shouldBe correctSeq
  }
}
