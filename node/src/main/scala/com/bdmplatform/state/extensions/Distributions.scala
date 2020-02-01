package com.bdmplatform.state.extensions

import cats.kernel.Monoid
import com.bdmplatform.account.Address
import com.bdmplatform.lang.ValidationError
import com.bdmplatform.state.{AssetDistribution, AssetDistributionPage, Portfolio}
import com.bdmplatform.transaction.Asset.IssuedAsset
import com.bdmplatform.transaction.assets.IssueTransaction
import com.bdmplatform.utils.Paged
import monix.reactive.Observable

trait Distributions {
  def portfolio(a: Address): Portfolio

  def assetDistribution(asset: IssuedAsset): AssetDistribution
  def assetDistributionAtHeight(asset: IssuedAsset,
                                height: Int,
                                count: Int,
                                fromAddress: Option[Address]): Either[ValidationError, AssetDistributionPage]

  def bdmDistribution(height: Int): Either[ValidationError, Map[Address, Long]]

  def nftObservable(address: Address, from: Option[IssuedAsset]): Observable[IssueTransaction]
}

object Distributions {
  def apply[T](value: T)(implicit ev: T => Distributions): Distributions = value

  trait Prov[T] {
    def distributions(value: T): Distributions
  }

  case object Empty extends Distributions {
    override def portfolio(a: Address): Portfolio = Portfolio.empty

    override def assetDistribution(asset: IssuedAsset): AssetDistribution = AssetDistribution @@ Map.empty[Address, Long]

    override def assetDistributionAtHeight(asset: IssuedAsset, height: Int, count: Int, fromAddress: Option[Address]): Either[ValidationError, AssetDistributionPage] =
      Right(AssetDistributionPage(Paged[Address, AssetDistribution](false, None, Monoid.empty[AssetDistribution])))

    override def bdmDistribution(height: Int): Either[ValidationError, Map[Address, Long]] = Right(Map.empty)

    override def nftObservable(address: Address, from: Option[IssuedAsset]): Observable[IssueTransaction] = Observable.empty
  }
}
