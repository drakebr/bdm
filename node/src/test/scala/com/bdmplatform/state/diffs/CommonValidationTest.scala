package com.bdmplatform.state.diffs

import com.bdmplatform.account.AddressScheme
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.db.WithState
import com.bdmplatform.features.{BlockchainFeature, BlockchainFeatures}
import com.bdmplatform.lagonaki.mocks.TestBlock
import com.bdmplatform.lang.ValidationError
import com.bdmplatform.lang.script.v1.ExprScript
import com.bdmplatform.lang.v1.compiler.Terms._
import com.bdmplatform.mining.MiningConstraint
import com.bdmplatform.settings.{Constants, FunctionalitySettings, TestFunctionalitySettings}
import com.bdmplatform.transaction.Asset.{IssuedAsset, Bdm}
import com.bdmplatform.transaction.assets.{IssueTransactionV1, IssueTransactionV2, SponsorFeeTransaction}
import com.bdmplatform.transaction.smart.SetScriptTransaction
import com.bdmplatform.transaction.transfer._
import com.bdmplatform.transaction.{GenesisTransaction, Transaction}
import com.bdmplatform.{NoShrink, TransactionGen}
import org.scalacheck.Gen
import org.scalatest.{Assertion, Matchers, PropSpec}
import org.scalatestplus.scalacheck.{ScalaCheckPropertyChecks => PropertyChecks}

class CommonValidationTest extends PropSpec with PropertyChecks with Matchers with TransactionGen with WithState with NoShrink {

  property("disallows double spending") {
    val preconditionsAndPayment: Gen[(GenesisTransaction, TransferTransactionV1)] = for {
      master    <- accountGen
      recipient <- otherAccountGen(candidate = master)
      ts        <- positiveIntGen
      genesis: GenesisTransaction = GenesisTransaction.create(master, ENOUGH_AMT, ts).explicitGet()
      transfer: TransferTransactionV1 <- bdmTransferGeneratorP(master, recipient)
    } yield (genesis, transfer)

    forAll(preconditionsAndPayment) {
      case (genesis, transfer) =>
        assertDiffEi(Seq(TestBlock.create(Seq(genesis, transfer))), TestBlock.create(Seq(transfer))) { blockDiffEi =>
          blockDiffEi should produce("AlreadyInTheState")
        }

        assertDiffEi(Seq(TestBlock.create(Seq(genesis))), TestBlock.create(Seq(transfer, transfer))) { blockDiffEi =>
          blockDiffEi should produce("AlreadyInTheState")
        }
    }
  }

  private def sponsoredTransactionsCheckFeeTest(feeInAssets: Boolean, feeAmount: Long)(f: Either[ValidationError, Unit] => Assertion): Unit = {
    val settings = createSettings(BlockchainFeatures.FeeSponsorship -> 0)
    val gen      = sponsorAndSetScriptGen(sponsorship = true, smartToken = false, smartAccount = false, feeInAssets, feeAmount)
    forAll(gen) {
      case (genesisBlock, transferTx) =>
        withLevelDBWriter(settings) { blockchain =>
          val BlockDiffer.Result(preconditionDiff, preconditionFees, totalFee, _) =
            BlockDiffer.fromBlock(blockchain, None, genesisBlock, MiningConstraint.Unlimited).explicitGet()
          blockchain.append(preconditionDiff, preconditionFees, totalFee, None, genesisBlock)

          f(FeeValidation(blockchain, 1, transferTx))
        }
    }
  }

  property("checkFee for sponsored transactions sunny") {
    sponsoredTransactionsCheckFeeTest(feeInAssets = true, feeAmount = 10)(_ shouldBe 'right)
  }

  property("checkFee for sponsored transactions fails if the fee is not enough") {
    sponsoredTransactionsCheckFeeTest(feeInAssets = true, feeAmount = 1)(_ should produce("does not exceed minimal value of"))
  }

  private def smartAccountCheckFeeTest(feeInAssets: Boolean, feeAmount: Long)(f: Either[ValidationError, Unit] => Assertion): Unit = {
    val settings = createSettings(BlockchainFeatures.SmartAccounts -> 0)
    val gen      = sponsorAndSetScriptGen(sponsorship = false, smartToken = false, smartAccount = true, feeInAssets, feeAmount)
    forAll(gen) {
      case (genesisBlock, transferTx) =>
        withLevelDBWriter(settings) { blockchain =>
          val BlockDiffer.Result(preconditionDiff, preconditionFees, totalFee, _) =
            BlockDiffer.fromBlock(blockchain, None, genesisBlock, MiningConstraint.Unlimited).explicitGet()
          blockchain.append(preconditionDiff, preconditionFees, totalFee, None, genesisBlock)

          f(FeeValidation(blockchain, 1, transferTx))
        }
    }
  }

  property("checkFee for smart accounts sunny") {
    smartAccountCheckFeeTest(feeInAssets = false, feeAmount = 400000)(_ shouldBe 'right)
  }

  private def sponsorAndSetScriptGen(sponsorship: Boolean, smartToken: Boolean, smartAccount: Boolean, feeInAssets: Boolean, feeAmount: Long) =
    for {
      richAcc      <- accountGen
      recipientAcc <- accountGen
      ts = System.currentTimeMillis()
    } yield {
      val script = ExprScript(TRUE).explicitGet()

      val genesisTx = GenesisTransaction.create(richAcc, ENOUGH_AMT, ts).explicitGet()

      val issueTx =
        if (smartToken)
          IssueTransactionV2
            .selfSigned(
              AddressScheme.current.chainId,
              richAcc,
              "test".getBytes("UTF-8"),
              "desc".getBytes("UTF-8"),
              Long.MaxValue,
              2,
              reissuable = false,
              Some(script),
              Constants.UnitsInWave,
              ts
            )
            .explicitGet()
        else
          IssueTransactionV1
            .selfSigned(richAcc, "test".getBytes("UTF-8"), "desc".getBytes("UTF-8"), Long.MaxValue, 2, reissuable = false, Constants.UnitsInWave, ts)
            .explicitGet()

      val transferBdmTx = TransferTransactionV1
        .selfSigned(Bdm, richAcc, recipientAcc, 10 * Constants.UnitsInWave, ts, Bdm, 1 * Constants.UnitsInWave, Array.emptyByteArray)
        .explicitGet()

      val transferAssetTx = TransferTransactionV1
        .selfSigned(
          IssuedAsset(issueTx.id()),
          richAcc,
          recipientAcc,
          100,
          ts,
          Bdm,
          if (smartToken) { 1 * Constants.UnitsInWave + ScriptExtraFee } else { 1 * Constants.UnitsInWave },
          Array.emptyByteArray
        )
        .explicitGet()

      val sponsorTx =
        if (sponsorship)
          Seq(
            SponsorFeeTransaction
              .selfSigned(richAcc, IssuedAsset(issueTx.id()), Some(10), if (smartToken) {
                Constants.UnitsInWave + ScriptExtraFee
              } else {
                Constants.UnitsInWave
              }, ts)
              .explicitGet()
          )
        else Seq.empty

      val setScriptTx =
        if (smartAccount)
          Seq(
            SetScriptTransaction
              .selfSigned(
                recipientAcc,
                Some(script),
                1 * Constants.UnitsInWave,
                ts
              )
              .explicitGet()
          )
        else Seq.empty

      val transferBackTx = TransferTransactionV1
        .selfSigned(
          IssuedAsset(issueTx.id()),
          recipientAcc,
          richAcc,
          1,
          ts,
          if (feeInAssets) IssuedAsset(issueTx.id()) else Bdm,
          feeAmount,
          Array.emptyByteArray
        )
        .explicitGet()

      (TestBlock.create(Vector[Transaction](genesisTx, issueTx, transferBdmTx, transferAssetTx) ++ sponsorTx ++ setScriptTx), transferBackTx)
    }

  private def createSettings(preActivatedFeatures: (BlockchainFeature, Int)*): FunctionalitySettings =
    TestFunctionalitySettings.Enabled
      .copy(
        preActivatedFeatures = preActivatedFeatures.map { case (k, v) => k.id -> v }(collection.breakOut),
        blocksForFeatureActivation = 1,
        featureCheckBlocksPeriod = 1
      )

  private def smartTokensCheckFeeTest(feeInAssets: Boolean, feeAmount: Long)(f: Either[ValidationError, Unit] => Assertion): Unit = {
    val settings = createSettings(BlockchainFeatures.SmartAccounts -> 0, BlockchainFeatures.SmartAssets -> 0)
    val gen      = sponsorAndSetScriptGen(sponsorship = false, smartToken = true, smartAccount = false, feeInAssets, feeAmount)
    forAll(gen) {
      case (genesisBlock, transferTx) =>
        withLevelDBWriter(settings) { blockchain =>
          val BlockDiffer.Result(preconditionDiff, preconditionFees, totalFee, _) =
            BlockDiffer.fromBlock(blockchain, None, genesisBlock, MiningConstraint.Unlimited).explicitGet()
          blockchain.append(preconditionDiff, preconditionFees, totalFee, None, genesisBlock)

          f(FeeValidation(blockchain, 1, transferTx))
        }
    }
  }

  property("checkFee for smart tokens sunny") {
    smartTokensCheckFeeTest(feeInAssets = false, feeAmount = 1)(_ shouldBe 'right)
  }
}
