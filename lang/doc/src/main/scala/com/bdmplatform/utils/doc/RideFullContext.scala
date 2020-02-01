package com.bdmplatform.utils.doc

import cats.Id
import cats.implicits._
import com.bdmplatform.lang.Global
import com.bdmplatform.lang.directives.DirectiveSet
import com.bdmplatform.lang.v1.CTX
import com.bdmplatform.lang.v1.evaluator.ctx.impl.bdm.BdmContext
import com.bdmplatform.lang.v1.evaluator.ctx.impl.{CryptoContext, PureContext}
import com.bdmplatform.lang.v1.traits.Environment.InputEntity
import com.bdmplatform.lang.v1.traits.domain.{BlockInfo, Recipient, ScriptAssetInfo, Tx}
import com.bdmplatform.lang.v1.traits.{DataType, Environment}

object RideFullContext {
  private val dummyEnv = new Environment[Id] {
    override def height: Long = ???
    override def chainId: Byte = 66
    override def inputEntity: InputEntity = ???
    override def transactionById(id: Array[Byte]): Option[Tx] = ???
    override def transferTransactionById(id: Array[Byte]): Option[Tx] = ???
    override def transactionHeightById(id: Array[Byte]): Option[Long] = ???
    override def assetInfoById(id: Array[Byte]): Option[ScriptAssetInfo] = ???
    override def lastBlockOpt(): Option[BlockInfo] = ???
    override def blockInfoByHeight(height: Int): Option[BlockInfo] = ???
    override def data(addressOrAlias: Recipient, key: String, dataType: DataType): Option[Any] = ???
    override def accountBalanceOf(addressOrAlias: Recipient, assetId: Option[Array[Byte]]): Either[String, Long] = ???
    override def resolveAlias(name: String): Either[String, Recipient.Address] = ???
    override def tthis: Recipient.Address = ???
  }

  def build(
    ds:       DirectiveSet,
    bdmEnv: Environment[Id] = dummyEnv
  ): CTX[Id] = {
    val bdmCtx = BdmContext.build[Id](ds, bdmEnv)
    val cryptoCtx = CryptoContext.build(Global, ds.stdLibVersion)
    val pureCtx = PureContext.build(Global, ds.stdLibVersion)
    pureCtx |+| cryptoCtx |+| bdmCtx
  }
}
