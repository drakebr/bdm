package com.bdmplatform.lang.v1.traits.domain

import com.bdmplatform.common.state.ByteStr

case class APair(amountAsset: Option[ByteStr], priceAsset: Option[ByteStr])
