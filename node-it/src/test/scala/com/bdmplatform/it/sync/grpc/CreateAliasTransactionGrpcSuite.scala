package com.bdmplatform.it.sync.grpc

import com.bdmplatform.it.NTPTime
import com.bdmplatform.account.Address
import com.bdmplatform.it.api.SyncHttpApi._
import com.bdmplatform.it.sync.{minFee, transferAmount}
import com.bdmplatform.it.util._
import com.bdmplatform.protobuf.transaction.Recipient
import com.bdmplatform.common.utils.EitherExt2
import io.grpc.Status.Code
import org.scalatest.prop.TableDrivenPropertyChecks

import scala.util.Random

class CreateAliasTransactionGrpcSuite extends GrpcBaseTransactionSuite with NTPTime with TableDrivenPropertyChecks {

  val (aliasCreator, aliasCreatorAddr) = (firstAcc, firstAddress)
  test("Able to send money to an alias") {
    val alias             = randomAlias()
    val creatorBalance    = sender.grpc.bdmBalance(aliasCreatorAddr).available
    val creatorEffBalance = sender.grpc.bdmBalance(aliasCreatorAddr).effective

    sender.grpc.broadcastCreateAlias(aliasCreator, alias, minFee, waitForTx = true)

    sender.grpc.bdmBalance(aliasCreatorAddr).available shouldBe creatorBalance - minFee
    sender.grpc.bdmBalance(aliasCreatorAddr).effective shouldBe creatorEffBalance - minFee

    sender.grpc.resolveAlias(alias) shouldBe Address.fromBytes(aliasCreatorAddr.toByteArray).explicitGet()

    sender.grpc.broadcastTransfer(aliasCreator, Recipient().withAlias(alias), transferAmount, minFee, waitForTx = true)

    sender.grpc.bdmBalance(aliasCreatorAddr).available shouldBe creatorBalance - 2 * minFee
    sender.grpc.bdmBalance(aliasCreatorAddr).effective shouldBe creatorEffBalance - 2 * minFee
  }

  test("Not able to create same aliases to same address") {
    val alias             = randomAlias()
    val creatorBalance    = sender.grpc.bdmBalance(aliasCreatorAddr).available
    val creatorEffBalance = sender.grpc.bdmBalance(aliasCreatorAddr).effective

    sender.grpc.broadcastCreateAlias(aliasCreator, alias, minFee, waitForTx = true)
    sender.grpc.bdmBalance(aliasCreatorAddr).available shouldBe creatorBalance - minFee
    sender.grpc.bdmBalance(aliasCreatorAddr).effective shouldBe creatorEffBalance - minFee

    assertGrpcError(sender.grpc.broadcastCreateAlias(aliasCreator, alias, minFee), "Alias already claimed", Code.INVALID_ARGUMENT)

    sender.grpc.bdmBalance(aliasCreatorAddr).available shouldBe creatorBalance - minFee
    sender.grpc.bdmBalance(aliasCreatorAddr).effective shouldBe creatorEffBalance - minFee
  }

  test("Not able to create aliases to other addresses") {
    val alias            = randomAlias()
    val secondBalance    = sender.grpc.bdmBalance(secondAddress).available
    val secondEffBalance = sender.grpc.bdmBalance(secondAddress).effective

    sender.grpc.broadcastCreateAlias(aliasCreator, alias, minFee, waitForTx = true)
    assertGrpcError(sender.grpc.broadcastCreateAlias(secondAcc, alias, minFee), "Alias already claimed", Code.INVALID_ARGUMENT)

    sender.grpc.bdmBalance(secondAddress).available shouldBe secondBalance
    sender.grpc.bdmBalance(secondAddress).effective shouldBe secondEffBalance
  }

  val aliases_names =
    Table(s"aliasName${randomAlias()}", s"aaaa${randomAlias()}", s"....${randomAlias()}", s"1234567890.${randomAlias()}", s"@.@-@_@${randomAlias()}")

  aliases_names.foreach { alias =>
    test(s"create alias named $alias") {
      sender.grpc.broadcastCreateAlias(aliasCreator, alias, minFee, waitForTx = true)
      sender.grpc.resolveAlias(alias) shouldBe Address.fromBytes(aliasCreatorAddr.toByteArray).explicitGet()
    }
  }

  val invalid_aliases_names =
    Table(
      ("aliasName", "message"),
      ("", "Alias '' length should be between 4 and 30"),
      ("abc", "Alias 'abc' length should be between 4 and 30"),
      ("morethen_thirtycharactersinline", "Alias 'morethen_thirtycharactersinline' length should be between 4 and 30"),
      ("~!|#$%^&*()_+=\";:/?><|\\][{}", "Alias should contain only following characters: -.0123456789@_abcdefghijklmnopqrstuvwxyz"),
      ("multilnetest\ntest", "Alias should contain only following characters: -.0123456789@_abcdefghijklmnopqrstuvwxyz"),
      ("UpperCaseAliase", "Alias should contain only following characters: -.0123456789@_abcdefghijklmnopqrstuvwxyz")
    )

  forAll(invalid_aliases_names) { (alias: String, message: String) =>
    test(s"Not able to create alias named $alias") {
      assertGrpcError(sender.grpc.broadcastCreateAlias(aliasCreator, alias, minFee), message, Code.INTERNAL)
    }
  }

  test("Able to lease by alias") {
    val (leaser, leaserAddr) = (thirdAcc, thirdAddress)
    val alias                = randomAlias()

    val aliasCreatorBalance    = sender.grpc.bdmBalance(aliasCreatorAddr).available
    val aliasCreatorEffBalance = sender.grpc.bdmBalance(aliasCreatorAddr).effective
    val leaserBalance          = sender.grpc.bdmBalance(leaserAddr).available
    val leaserEffBalance       = sender.grpc.bdmBalance(leaserAddr).effective

    sender.grpc.broadcastCreateAlias(aliasCreator, alias, minFee, waitForTx = true)
    val leasingAmount = 1.bdm

    sender.grpc.broadcastLease(leaser, Recipient().withAlias(alias), leasingAmount, minFee, waitForTx = true)

    sender.grpc.bdmBalance(aliasCreatorAddr).available shouldBe aliasCreatorBalance - minFee
    sender.grpc.bdmBalance(aliasCreatorAddr).effective shouldBe aliasCreatorEffBalance + leasingAmount - minFee
    sender.grpc.bdmBalance(leaserAddr).available shouldBe leaserBalance - leasingAmount - minFee
    sender.grpc.bdmBalance(leaserAddr).effective shouldBe leaserEffBalance - leasingAmount - minFee
  }

  test("Not able to create alias when insufficient funds") {
    val balance = sender.grpc.bdmBalance(aliasCreatorAddr).available
    val alias   = randomAlias()
    assertGrpcError(sender.grpc.broadcastCreateAlias(aliasCreator, alias, balance + minFee), "negative bdm balance", Code.INVALID_ARGUMENT)
  }

  private def randomAlias(): String = {
    s"testalias.${Random.alphanumeric.take(9).mkString}".toLowerCase
  }

}
