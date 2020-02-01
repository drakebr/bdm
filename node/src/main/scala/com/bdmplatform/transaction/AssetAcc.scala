package com.bdmplatform.transaction

import com.bdmplatform.account.Address

case class AssetAcc(account: Address, assetId: Option[Asset])
