package com.bdmplatform.state

import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.history._
import com.bdmplatform.state.diffs._
import com.bdmplatform.transaction.GenesisTransaction
import com.bdmplatform.transaction.transfer._
import com.bdmplatform.{NoShrink, TransactionGen}
import org.scalacheck.Gen
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.{ScalaCheckPropertyChecks => PropertyChecks}

class NgStateTest extends PropSpec with PropertyChecks with Matchers with TransactionGen with NoShrink {

  def preconditionsAndPayments(amt: Int): Gen[(GenesisTransaction, Seq[TransferTransactionV1])] =
    for {
      master    <- accountGen
      recipient <- accountGen
      ts        <- positiveIntGen
      genesis: GenesisTransaction = GenesisTransaction.create(master, ENOUGH_AMT, ts).explicitGet()
      payments: Seq[TransferTransactionV1] <- Gen.listOfN(amt, bdmTransferGeneratorP(master, recipient))
    } yield (genesis, payments)

  property("can forge correctly signed blocks") {
    forAll(preconditionsAndPayments(10)) {
      case (genesis, payments) =>
        val (block, microBlocks) = chainBaseAndMicro(randomSig, genesis, payments.map(t => Seq(t)))

        val ng = new NgState(block, Diff.empty, 0L, 0L, Set.empty, None, Map.empty)
        microBlocks.foreach(m => ng.append(m, Diff.empty, 0L, 0L, 0L))

        ng.totalDiffOf(microBlocks.last.totalResBlockSig)
        microBlocks.foreach { m =>
          val (forged, _, _, _, _) = ng.totalDiffOf(m.totalResBlockSig).get
          forged.signaturesValid() shouldBe 'right
        }
        Seq(microBlocks(4)).map(x => ng.totalDiffOf(x.totalResBlockSig))
    }
  }

  property("can resolve best liquid block") {
    forAll(preconditionsAndPayments(5)) {
      case (genesis, payments) =>
        val (block, microBlocks) = chainBaseAndMicro(randomSig, genesis, payments.map(t => Seq(t)))

        val ng = new NgState(block, Diff.empty, 0L, 0L, Set.empty, None, Map.empty)
        microBlocks.foreach(m => ng.append(m, Diff.empty, 0L, 0L, 0L))

        ng.bestLiquidBlock.uniqueId shouldBe microBlocks.last.totalResBlockSig

        new NgState(block, Diff.empty, 0L, 0L, Set.empty, Some(0), Map.empty).bestLiquidBlock.uniqueId shouldBe block.uniqueId
    }
  }

  property("can resolve best last block") {
    forAll(preconditionsAndPayments(5)) {
      case (genesis, payments) =>
        val (block, microBlocks) = chainBaseAndMicro(randomSig, genesis, payments.map(t => Seq(t)))

        val ng = new NgState(block, Diff.empty, 0L, 0L, Set.empty, None, Map.empty)

        microBlocks.foldLeft(1000) {
          case (thisTime, m) =>
            ng.append(m, Diff.empty, 0L, 0L, thisTime)
            thisTime + 50
        }

        ng.bestLastBlockInfo(0).blockId shouldBe block.uniqueId
        ng.bestLastBlockInfo(1001).blockId shouldBe microBlocks.head.totalResBlockSig
        ng.bestLastBlockInfo(1051).blockId shouldBe microBlocks.tail.head.totalResBlockSig
        ng.bestLastBlockInfo(2000).blockId shouldBe microBlocks.last.totalResBlockSig

        new NgState(block, Diff.empty, 0L, 0L, Set.empty, Some(0), Map.empty).bestLiquidBlock.uniqueId shouldBe block.uniqueId
    }
  }

  property("calculates carry fee correctly") {
    forAll(preconditionsAndPayments(5)) {
      case (genesis, payments) =>
        val (block, microBlocks) = chainBaseAndMicro(randomSig, genesis, payments.map(t => Seq(t)))

        val ng = new NgState(block, Diff.empty, 0L, 0L, Set.empty, None, Map.empty)
        microBlocks.foreach(m => ng.append(m, Diff.empty, 1L, 0L, 0L))

        ng.totalDiffOf(block.uniqueId).map(_._3) shouldBe Some(0L)
        microBlocks.zipWithIndex.foreach {
          case (m, i) =>
            val u = ng.totalDiffOf(m.totalResBlockSig).map(_._3)
            u shouldBe Some(i + 1)
        }
        ng.carryFee shouldBe microBlocks.size
    }
  }
}
