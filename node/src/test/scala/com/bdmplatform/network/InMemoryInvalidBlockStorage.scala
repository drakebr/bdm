package com.bdmplatform.network

import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.lang.ValidationError
import com.bdmplatform.transaction.TxValidationError.GenericError

class InMemoryInvalidBlockStorage extends InvalidBlockStorage {

  var s: Set[ByteStr] = Set.empty[ByteStr]

  override def add(blockId: ByteStr, validationError: ValidationError): Unit = s += blockId

  override def find(blockId: ByteStr): Option[ValidationError] = {
    if (s.contains(blockId)) Some(GenericError("Unknown")) else None
  }

}
