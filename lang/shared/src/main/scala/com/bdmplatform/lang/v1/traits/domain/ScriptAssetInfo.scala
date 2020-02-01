package com.bdmplatform.lang.v1.traits.domain

import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.lang.v1.traits.domain.Recipient.Address

case class ScriptAssetInfo(
    id:         ByteStr,
    quantity:   Long,
    decimals:   Int,
    issuer:     Address,
    issuerPk:   ByteStr,
    reissuable: Boolean,
    scripted:   Boolean,
    sponsored:  Boolean
)
