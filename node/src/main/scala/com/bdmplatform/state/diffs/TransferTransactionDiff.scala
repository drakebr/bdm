package com.bdmplatform.state.diffs

import cats.implicits._
import com.bdmplatform.account.Address
import com.bdmplatform.features.BlockchainFeatures
import com.bdmplatform.features.FeatureProvider._
import com.bdmplatform.lang.ValidationError
import com.bdmplatform.state._
import com.bdmplatform.transaction.Asset.{IssuedAsset, Bdm}
import com.bdmplatform.transaction.TxValidationError
import com.bdmplatform.transaction.TxValidationError.GenericError
import com.bdmplatform.transaction.transfer._

import scala.util.{Right, Try}

object TransferTransactionDiff {
  def apply(blockchain: Blockchain, height: Int, blockTime: Long)(tx: TransferTransaction): Either[ValidationError, Diff] = {
    val sender = Address.fromPublicKey(tx.sender)

    val isSmartAsset = tx.feeAssetId match {
      case Bdm => false
      case asset @ IssuedAsset(_) =>
        blockchain
          .assetDescription(asset)
          .flatMap(_.script)
          .isDefined
    }

    for {
      recipient <- blockchain.resolveAlias(tx.recipient)
      _         <- Either.cond(!isSmartAsset, (), GenericError("Smart assets can't participate in TransferTransactions as a fee"))

      _ <- validateOverflow(blockchain, tx)
      portfolios = (tx.assetId match {
        case Bdm =>
          Map(sender -> Portfolio(-tx.amount, LeaseBalance.empty, Map.empty)).combine(
            Map(recipient -> Portfolio(tx.amount, LeaseBalance.empty, Map.empty))
          )
        case asset @ IssuedAsset(_) =>
          Map(sender -> Portfolio(0, LeaseBalance.empty, Map(asset -> -tx.amount))).combine(
            Map(recipient -> Portfolio(0, LeaseBalance.empty, Map(asset -> tx.amount)))
          )
      }).combine(
        tx.feeAssetId match {
          case Bdm => Map(sender -> Portfolio(-tx.fee, LeaseBalance.empty, Map.empty))
          case asset @ IssuedAsset(_) =>
            val senderPf = Map(sender -> Portfolio(0, LeaseBalance.empty, Map(asset -> -tx.fee)))
            if (height >= Sponsorship.sponsoredFeesSwitchHeight(blockchain)) {
              val sponsorPf = blockchain
                .assetDescription(asset)
                .collect {
                  case desc if desc.sponsorship > 0 =>
                    val feeInBdm = Sponsorship.toBdm(tx.fee, desc.sponsorship)
                    Map(desc.issuer.toAddress -> Portfolio(-feeInBdm, LeaseBalance.empty, Map(asset -> tx.fee)))
                }
                .getOrElse(Map.empty)
              senderPf.combine(sponsorPf)
            } else senderPf
        }
      )
      assetIssued    = tx.assetId.fold(true)(blockchain.assetDescription(_).isDefined)
      feeAssetIssued = tx.feeAssetId.fold(true)(blockchain.assetDescription(_).isDefined)
      _ <- Either.cond(
        blockTime <= blockchain.settings.functionalitySettings.allowUnissuedAssetsUntil || (assetIssued && feeAssetIssued),
        (),
        GenericError(s"Unissued assets are not allowed after allowUnissuedAssetsUntil=${blockchain.settings.functionalitySettings.allowUnissuedAssetsUntil}")
      )
    } yield
      Diff(
        height,
        tx,
        portfolios,
        scriptsRun = DiffsCommon.countScriptRuns(blockchain, tx)
      )
  }

  private def validateOverflow(blockchain: Blockchain, tx: TransferTransaction) = {
    if (blockchain.isFeatureActivated(BlockchainFeatures.Ride4DApps, blockchain.height)) {
      Right(()) // lets transaction validates itself
    } else {
      Try(Math.addExact(tx.fee, tx.amount))
        .fold(
          _ => TxValidationError.OverflowError.asLeft[Unit],
          _ => ().asRight[ValidationError]
        )
    }
  }
}
