package com.bdmplatform.api.http

import akka.http.scaladsl.server.Directive1
import com.bdmplatform.api.http.ApiError.{BlockDoesNotExist, InvalidSignature}
import com.bdmplatform.block.Block
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.state.Blockchain
import com.bdmplatform.transaction.TransactionParsers

trait CommonApiFunctions { this: ApiRoute =>
  protected[api] def withBlock(blockchain: Blockchain, encodedSignature: String): Directive1[Block] =
    if (encodedSignature.length > TransactionParsers.SignatureStringLength) complete(InvalidSignature)
    else {
      ByteStr
        .decodeBase58(encodedSignature)
        .toOption
        .toRight(InvalidSignature)
        .flatMap(s => blockchain.blockById(s).toRight(BlockDoesNotExist)) match {
        case Right(b) => provide(b)
        case Left(e)  => complete(e)
      }
    }
}
