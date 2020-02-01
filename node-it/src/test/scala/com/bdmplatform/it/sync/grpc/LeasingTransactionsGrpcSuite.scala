package com.bdmplatform.it.sync.grpc

import com.bdmplatform.it.api.SyncHttpApi._
import com.bdmplatform.it.sync._
import com.bdmplatform.it.util._
import com.bdmplatform.common.utils.EitherExt2
import com.bdmplatform.protobuf.transaction.{PBRecipients, PBTransactions, Recipient}
import io.grpc.Status.Code

class LeasingTransactionsGrpcSuite extends GrpcBaseTransactionSuite {
  private val errorMessage = "Reason: Cannot lease more than own"

  test("leasing bdm decreases lessor's eff.b. and increases lessee's eff.b.; lessor pays fee") {
    for (v <- supportedVersions) {
      val firstBalance     = sender.grpc.bdmBalance(firstAddress)
      val secondBalance    = sender.grpc.bdmBalance(secondAddress)

      val leaseTx = sender.grpc.broadcastLease(firstAcc, PBRecipients.create(secondAcc.toAddress), leasingAmount, minFee, version = v, waitForTx = true)
      val leaseTxId = PBTransactions.vanilla(leaseTx).explicitGet().id().base58

      sender.grpc.bdmBalance(firstAddress).regular shouldBe firstBalance.regular - minFee
      sender.grpc.bdmBalance(firstAddress).effective shouldBe firstBalance.effective - minFee - leasingAmount
      sender.grpc.bdmBalance(secondAddress).regular shouldBe secondBalance.regular
      sender.grpc.bdmBalance(secondAddress).effective shouldBe secondBalance.effective + leasingAmount

      sender.grpc.getActiveLeases(secondAddress) shouldBe List(leaseTx)
      sender.grpc.getActiveLeases(firstAddress) shouldBe List(leaseTx)

      sender.grpc.broadcastLeaseCancel(firstAcc, leaseTxId, minFee, waitForTx = true)
    }
  }

  test("cannot lease non-own bdm") {
    for (v <- supportedVersions) {
      val leaseTx = sender.grpc.broadcastLease(firstAcc, PBRecipients.create(secondAcc.toAddress), leasingAmount, minFee, version = v, waitForTx = true)
      val leaseTxId = PBTransactions.vanilla(leaseTx).explicitGet().id().base58
      val secondEffBalance = sender.grpc.bdmBalance(secondAddress).effective
      val thirdEffBalance = sender.grpc.bdmBalance(thirdAddress).effective

      assertGrpcError(
        sender.grpc.broadcastLease(secondAcc, PBRecipients.create(thirdAcc.toAddress), secondEffBalance - minFee, minFee, version = v),
        errorMessage,
        Code.INVALID_ARGUMENT
      )

      sender.grpc.bdmBalance(secondAddress).effective shouldBe secondEffBalance
      sender.grpc.bdmBalance(thirdAddress).effective shouldBe thirdEffBalance
      sender.grpc.getActiveLeases(secondAddress) shouldBe List(leaseTx)
      sender.grpc.getActiveLeases(thirdAddress) shouldBe List.empty

      sender.grpc.broadcastLeaseCancel(firstAcc, leaseTxId, minFee, waitForTx = true)
    }
  }

  test("can not make leasing without having enough balance") {
    for (v <- supportedVersions) {
      val firstBalance = sender.grpc.bdmBalance(firstAddress)
      val secondBalance = sender.grpc.bdmBalance(secondAddress)

      //secondAddress effective balance more than general balance
      assertGrpcError(
        sender.grpc.broadcastLease(secondAcc, Recipient().withAddress(firstAddress), secondBalance.regular + 1.bdm, minFee, version = v),
        errorMessage,
        Code.INVALID_ARGUMENT
      )

      assertGrpcError(
        sender.grpc.broadcastLease(firstAcc, Recipient().withAddress(secondAddress), firstBalance.regular, minFee, version = v),
        "Reason: negative effective balance",
        Code.INVALID_ARGUMENT
      )

      assertGrpcError(
        sender.grpc.broadcastLease(firstAcc, Recipient().withAddress(secondAddress), firstBalance.regular - minFee / 2, minFee, version = v),
        "Reason: negative effective balance",
        Code.INVALID_ARGUMENT
      )

      sender.grpc.bdmBalance(firstAddress) shouldBe firstBalance
      sender.grpc.bdmBalance(secondAddress) shouldBe secondBalance
      sender.grpc.getActiveLeases(firstAddress) shouldBe List.empty
      sender.grpc.getActiveLeases(secondAddress) shouldBe List.empty
    }
  }

  test("lease cancellation reverts eff.b. changes; lessor pays fee for both lease and cancellation") {
    for (v <- supportedVersions) {
      val firstBalance = sender.grpc.bdmBalance(firstAddress)
      val secondBalance = sender.grpc.bdmBalance(secondAddress)

      val leaseTx = sender.grpc.broadcastLease(firstAcc, PBRecipients.create(secondAcc.toAddress), leasingAmount, minFee, version = v, waitForTx = true)
      val leaseTxId = PBTransactions.vanilla(leaseTx).explicitGet().id().base58

      sender.grpc.broadcastLeaseCancel(firstAcc, leaseTxId, minFee, waitForTx = true)

      sender.grpc.bdmBalance(firstAddress).regular shouldBe firstBalance.regular - 2 * minFee
      sender.grpc.bdmBalance(firstAddress).effective shouldBe firstBalance.effective - 2 * minFee
      sender.grpc.bdmBalance(secondAddress).regular shouldBe secondBalance.regular
      sender.grpc.bdmBalance(secondAddress).effective shouldBe secondBalance.effective
      sender.grpc.getActiveLeases(secondAddress) shouldBe List.empty
      sender.grpc.getActiveLeases(firstAddress) shouldBe List.empty
    }
  }

  test("lease cancellation can be done only once") {
    for (v <- supportedVersions) {
      val firstBalance = sender.grpc.bdmBalance(firstAddress)
      val secondBalance = sender.grpc.bdmBalance(secondAddress)

      val leaseTx = sender.grpc.broadcastLease(firstAcc, PBRecipients.create(secondAcc.toAddress), leasingAmount, minFee, version = v, waitForTx = true)
      val leaseTxId = PBTransactions.vanilla(leaseTx).explicitGet().id().base58

      sender.grpc.broadcastLeaseCancel(firstAcc, leaseTxId, minFee, waitForTx = true)

      assertGrpcError(
        sender.grpc.broadcastLeaseCancel(firstAcc, leaseTxId, minFee),
        "Reason: Cannot cancel already cancelled lease",
        Code.INVALID_ARGUMENT
      )
      sender.grpc.bdmBalance(firstAddress).regular shouldBe firstBalance.regular - 2 * minFee
      sender.grpc.bdmBalance(firstAddress).effective shouldBe firstBalance.effective - 2 * minFee
      sender.grpc.bdmBalance(secondAddress).regular shouldBe secondBalance.regular
      sender.grpc.bdmBalance(secondAddress).effective shouldBe secondBalance.effective

      sender.grpc.getActiveLeases(secondAddress) shouldBe List.empty
      sender.grpc.getActiveLeases(firstAddress) shouldBe List.empty
    }
  }

  test("only sender can cancel lease transaction") {
    for (v <- supportedVersions) {
      val firstBalance = sender.grpc.bdmBalance(firstAddress)
      val secondBalance = sender.grpc.bdmBalance(secondAddress)

      val leaseTx = sender.grpc.broadcastLease(firstAcc, PBRecipients.create(secondAcc.toAddress), leasingAmount, minFee, version = v, waitForTx = true)
      val leaseTxId = PBTransactions.vanilla(leaseTx).explicitGet().id().base58

      assertGrpcError(
        sender.grpc.broadcastLeaseCancel(secondAcc, leaseTxId, minFee),
        "LeaseTransaction was leased by other sender",
        Code.INVALID_ARGUMENT
      )
      sender.grpc.bdmBalance(firstAddress).regular shouldBe firstBalance.regular - minFee
      sender.grpc.bdmBalance(firstAddress).effective shouldBe firstBalance.effective - minFee - leasingAmount
      sender.grpc.bdmBalance(secondAddress).regular shouldBe secondBalance.regular
      sender.grpc.bdmBalance(secondAddress).effective shouldBe secondBalance.effective + leasingAmount
      sender.grpc.getActiveLeases(secondAddress) shouldBe List(leaseTx)
      sender.grpc.getActiveLeases(firstAddress) shouldBe List(leaseTx)

      sender.grpc.broadcastLeaseCancel(firstAcc, leaseTxId, minFee, waitForTx = true)
    }
  }

  test("can not make leasing to yourself") {
    for (v <- supportedVersions) {
      val firstBalance = sender.grpc.bdmBalance(firstAddress)
      assertGrpcError(
        sender.grpc.broadcastLease(firstAcc, PBRecipients.create(firstAcc.toAddress), leasingAmount, minFee, v),
        "ToSelf",
        Code.INTERNAL
      )
      sender.grpc.bdmBalance(firstAddress).regular shouldBe firstBalance.regular
      sender.grpc.bdmBalance(firstAddress).effective shouldBe firstBalance.effective
      sender.grpc.getActiveLeases(firstAddress) shouldBe List.empty
    }
  }

}
