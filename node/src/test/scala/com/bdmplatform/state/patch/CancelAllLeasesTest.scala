package com.bdmplatform.state.patch

import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.lagonaki.mocks.TestBlock
import com.bdmplatform.settings.TestFunctionalitySettings
import com.bdmplatform.state.diffs._
import com.bdmplatform.transaction.GenesisTransaction
import com.bdmplatform.transaction.lease.{LeaseCancelTransactionV1, LeaseTransaction}
import com.bdmplatform.{NoShrink, TransactionGen}
import org.scalacheck.Gen
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.{ScalaCheckPropertyChecks => PropertyChecks}

class CancelAllLeasesTest extends PropSpec with PropertyChecks with Matchers with TransactionGen with NoShrink {

  private val settings =
    TestFunctionalitySettings.Enabled.copy(resetEffectiveBalancesAtHeight = 5, lastTimeBasedForkParameter = Long.MaxValue / 2)

  property("CancelAllLeases cancels all active leases and its effects including those in the block") {
    val setupAndLeaseInResetBlock: Gen[(GenesisTransaction, GenesisTransaction, LeaseTransaction, LeaseCancelTransactionV1, LeaseTransaction, Long)] =
      for {
        master        <- accountGen
        recipient     <- accountGen suchThat (_ != master)
        otherAccount  <- accountGen
        otherAccount2 <- accountGen
        ts            <- Gen.choose(0, settings.lastTimeBasedForkParameter)
        genesis: GenesisTransaction  = GenesisTransaction.create(master, ENOUGH_AMT, ts).explicitGet()
        genesis2: GenesisTransaction = GenesisTransaction.create(otherAccount, ENOUGH_AMT, ts).explicitGet()
        (lease, _) <- leaseAndCancelGeneratorP(master, recipient, ts)
        fee2       <- smallFeeGen
        unleaseOther = LeaseCancelTransactionV1.selfSigned(otherAccount, lease.id(), fee2, ts + 1).explicitGet()
        (lease2, _) <- leaseAndCancelGeneratorP(master, otherAccount2, ts)
      } yield (genesis, genesis2, lease, unleaseOther, lease2, ts)

    forAll(setupAndLeaseInResetBlock) {
      case (genesis, genesis2, lease, unleaseOther, lease2, blockTime) =>
        assertDiffAndState(
          Seq(
            TestBlock.create(blockTime, Seq(genesis, genesis2, lease, unleaseOther)),
            TestBlock.create(Seq.empty),
            TestBlock.create(Seq.empty),
            TestBlock.create(Seq.empty)
          ),
          TestBlock.create(Seq(lease2)),
          settings
        ) {
          case (_, newState) =>
            newState.allActiveLeases shouldBe empty
//          newState.accountPortfolios.map(_._2.leaseInfo).foreach(_ shouldBe LeaseInfo.empty)
        }
    }
  }
}
