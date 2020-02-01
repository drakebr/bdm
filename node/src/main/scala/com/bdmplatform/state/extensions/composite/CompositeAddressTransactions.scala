package com.bdmplatform.state.extensions.composite

import com.bdmplatform.account.Address
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.state.extensions.AddressTransactions
import com.bdmplatform.state.{Diff, Height}
import com.bdmplatform.transaction.{Transaction, TransactionParser}
import monix.reactive.Observable

private[state] final class CompositeAddressTransactions(baseProvider: AddressTransactions, height: Height, getDiff: () => Option[Diff]) extends AddressTransactions {
  override def addressTransactionsObservable(address: Address,
                                             types: Set[TransactionParser],
                                             fromId: Option[ByteStr]): Observable[(Height, Transaction)] = {
    val fromDiff = for {
      diff <- getDiff().toIterable
      (height, tx, addresses) <- diff.transactions.values.toVector.reverse
    } yield (Height(height), tx, addresses)

    com.bdmplatform.state.addressTransactionsCompose(baseProvider, Observable.fromIterable(fromDiff))(address, types, fromId)
  }
}
