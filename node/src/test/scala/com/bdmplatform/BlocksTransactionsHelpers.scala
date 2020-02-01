package com.bdmplatform
import com.bdmplatform.account.{Address, AddressOrAlias, KeyPair}
import com.bdmplatform.block.{Block, MicroBlock, SignerData}
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.common.utils._
import com.bdmplatform.consensus.nxt.NxtLikeConsensusBlockData
import com.bdmplatform.history.DefaultBaseTarget
import com.bdmplatform.lang.script.Script
import com.bdmplatform.lang.v1.compiler.Terms.FUNCTION_CALL
import com.bdmplatform.state.StringDataEntry
import com.bdmplatform.transaction.Asset.{IssuedAsset, Bdm}
import com.bdmplatform.transaction.assets.{IssueTransaction, IssueTransactionV1}
import com.bdmplatform.transaction.lease.{LeaseCancelTransactionV1, LeaseTransactionV1}
import com.bdmplatform.transaction.smart.{InvokeScriptTransaction, SetScriptTransaction}
import com.bdmplatform.transaction.transfer.{TransferTransactionV1, TransferTransactionV2}
import com.bdmplatform.transaction.{DataTransaction, Transaction}
import org.scalacheck.Gen

trait BlocksTransactionsHelpers { self: TransactionGen =>
  object QuickTX {
    val FeeAmount = 400000

    def transfer(
        from: KeyPair,
        to: AddressOrAlias = accountGen.sample.get,
        amount: Long = smallFeeGen.sample.get,
        timestamp: Gen[Long] = timestampGen
    ): Gen[Transaction] =
      for {
        timestamp <- timestamp
      } yield TransferTransactionV1.selfSigned(Bdm, from, to, amount, timestamp, Bdm, FeeAmount, Array.empty).explicitGet()

    def transferV2(
        from: KeyPair,
        to: AddressOrAlias = accountGen.sample.get,
        amount: Long = smallFeeGen.sample.get,
        timestamp: Gen[Long] = timestampGen
    ): Gen[Transaction] =
      for {
        timestamp <- timestamp
      } yield TransferTransactionV2.selfSigned(Bdm, from, to, amount, timestamp, Bdm, FeeAmount, Array.empty).explicitGet()

    def transferAsset(
        asset: IssuedAsset,
        from: KeyPair,
        to: AddressOrAlias = accountGen.sample.get,
        amount: Long = smallFeeGen.sample.get,
        timestamp: Gen[Long] = timestampGen
    ): Gen[Transaction] =
      for {
        timestamp <- timestamp
      } yield TransferTransactionV1.selfSigned(asset, from, to, amount, timestamp, Bdm, FeeAmount, Array.empty).explicitGet()

    def lease(
        from: KeyPair,
        to: AddressOrAlias = accountGen.sample.get,
        amount: Long = smallFeeGen.sample.get,
        timestamp: Gen[Long] = timestampGen
    ): Gen[LeaseTransactionV1] =
      for {
        timestamp <- timestamp
      } yield LeaseTransactionV1.selfSigned(from, amount, FeeAmount, timestamp, to).explicitGet()

    def leaseCancel(from: KeyPair, leaseId: ByteStr, timestamp: Gen[Long] = timestampGen): Gen[LeaseCancelTransactionV1] =
      for {
        timestamp <- timestamp
      } yield LeaseCancelTransactionV1.selfSigned(from, leaseId, FeeAmount, timestamp).explicitGet()

    def data(from: KeyPair, dataKey: String, timestamp: Gen[Long] = timestampGen): Gen[DataTransaction] =
      for {
        timestamp <- timestamp
      } yield DataTransaction.selfSigned(from, List(StringDataEntry(dataKey, Gen.numStr.sample.get)), FeeAmount, timestamp).explicitGet()

    def nftIssue(from: KeyPair, timestamp: Gen[Long] = timestampGen): Gen[IssueTransaction] =
      for {
        timestamp <- timestamp
      } yield IssueTransactionV1.selfSigned(from, "test".getBytes(), "".getBytes(), 1, 0, reissuable = false, 100000000L, timestamp).explicitGet()

    def setScript(from: KeyPair, script: Script, timestamp: Gen[Long] = timestampGen): Gen[SetScriptTransaction] =
      for {
        timestamp <- timestamp
      } yield SetScriptTransaction.selfSigned(from, Some(script), FeeAmount, timestamp).explicitGet()

    def invokeScript(
        from: KeyPair,
        dapp: Address,
        call: FUNCTION_CALL,
        payments: Seq[InvokeScriptTransaction.Payment] = Nil,
        timestamp: Gen[Long] = timestampGen
    ): Gen[InvokeScriptTransaction] =
      for {
        timestamp <- timestamp
      } yield InvokeScriptTransaction.selfSigned(from, dapp, Some(call), payments, FeeAmount * 2, Bdm, timestamp).explicitGet()
  }

  object UnsafeBlocks {
    def unsafeChainBaseAndMicro(
        totalRefTo: ByteStr,
        base: Seq[Transaction],
        micros: Seq[Seq[Transaction]],
        signer: KeyPair,
        version: Byte,
        timestamp: Long
    ): (Block, Seq[MicroBlock]) = {
      val block = unsafeBlock(totalRefTo, base, signer, version, timestamp)
      val microBlocks = micros
        .foldLeft((block, Seq.empty[MicroBlock])) {
          case ((lastTotal, allMicros), txs) =>
            val (newTotal, micro) = unsafeMicro(totalRefTo, lastTotal, txs, signer, version, timestamp)
            (newTotal, allMicros :+ micro)
        }
        ._2
      (block, microBlocks)
    }

    def unsafeMicro(totalRefTo: ByteStr, prevTotal: Block, txs: Seq[Transaction], signer: KeyPair, version: Byte, ts: Long): (Block, MicroBlock) = {
      val newTotalBlock = unsafeBlock(totalRefTo, prevTotal.transactionData ++ txs, signer, version, ts)
      val unsigned      = new MicroBlock(version, signer, txs, prevTotal.uniqueId, newTotalBlock.uniqueId, ByteStr.empty)
      val signature     = crypto.sign(signer, unsigned.bytes())
      val signed        = unsigned.copy(signature = ByteStr(signature))
      (newTotalBlock, signed)
    }

    def unsafeBlock(
        reference: ByteStr,
        txs: Seq[Transaction],
        signer: KeyPair,
        version: Byte,
        timestamp: Long,
        bTarget: Long = DefaultBaseTarget
    ): Block = {
      val unsigned = Block(
        version = version,
        timestamp = timestamp,
        reference = reference,
        consensusData = NxtLikeConsensusBlockData(
          baseTarget = bTarget,
          generationSignature = com.bdmplatform.history.generationSignature
        ),
        transactionData = txs,
        signerData = SignerData(
          generator = signer,
          signature = ByteStr.empty
        ),
        featureVotes = Set.empty,
        rewardVote = -1L
      )

      unsigned.copy(signerData = SignerData(signer, ByteStr(crypto.sign(signer, unsigned.bytes()))))
    }
  }
}
