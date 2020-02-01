package com.bdmplatform

import com.bdmplatform.settings.WalletSettings
import com.bdmplatform.wallet.Wallet

trait TestWallet {
  protected val testWallet: Wallet = {
    val wallet = Wallet(WalletSettings(None, Some("123"), None))
    wallet.generateNewAccounts(10)
    wallet
  }
}
