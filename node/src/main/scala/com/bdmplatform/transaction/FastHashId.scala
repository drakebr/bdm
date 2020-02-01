package com.bdmplatform.transaction

import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.crypto
import monix.eval.Coeval

trait FastHashId extends ProvenTransaction {
  val id: Coeval[ByteStr] = Coeval.evalOnce(FastHashId.create(this.bodyBytes()))
}

object FastHashId {
  def create(bodyBytes: Array[Byte]): ByteStr = {
    ByteStr(crypto.fastHash(bodyBytes))
  }
}
