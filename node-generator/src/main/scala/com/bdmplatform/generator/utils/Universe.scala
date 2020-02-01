package com.bdmplatform.generator.utils

import com.bdmplatform.generator.Preconditions.CreatedAccount
import com.bdmplatform.transaction.assets.IssueTransaction
import com.bdmplatform.transaction.lease.LeaseTransaction

object Universe {
  var Accounts: List[CreatedAccount]           = Nil
  var IssuedAssets: List[IssueTransaction]     = Nil
  @volatile var Leases: List[LeaseTransaction] = Nil
}
