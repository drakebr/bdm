package com.bdmplatform.serialization.protobuf

import java.util.concurrent.TimeUnit

import com.bdmplatform.account.PublicKey
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.common.utils.Base58
import com.bdmplatform.protobuf.transaction.PBTransactions
import com.bdmplatform.transaction.Asset.Bdm
import com.bdmplatform.transaction.Proofs
import com.bdmplatform.transaction.transfer.MassTransferTransaction
import com.bdmplatform.transaction.transfer.MassTransferTransaction.Transfer
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Array(Mode.AverageTime))
@Threads(1)
@Fork(1)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
class ProtoBufBenchmark {

  @Benchmark
  def serializeMassTransferPB_test(bh: Blackhole): Unit = {
    val vanillaTx = {
      val transfers = MassTransferTransaction
        .parseTransfersList(
          List(Transfer("3N5GRqzDBhjVXnCn44baHcz2GoZy5qLxtTh", 100000000L), Transfer("3N5GRqzDBhjVXnCn44baHcz2GoZy5qLxtTh", 200000000L)))
        .right
        .get

      MassTransferTransaction
        .create(
          Bdm,
          PublicKey.fromBase58String("FM5ojNqW7e9cZ9zhPYGkpSP1Pcd8Z3e3MNKYVS5pGJ8Z").right.get,
          transfers,
          1518091313964L,
          200000,
          Base58.tryDecodeWithLimit("59QuUcqP6p").get,
          Proofs(Seq(ByteStr.decodeBase58("FXMNu3ecy5zBjn9b69VtpuYRwxjCbxdkZ3xZpLzB8ZeFDvcgTkmEDrD29wtGYRPtyLS3LPYrL2d5UM6TpFBMUGQ").get))
        )
        .right
        .get
    }

    val tx = PBTransactions.protobuf(vanillaTx)
    bh.consume(tx.toByteArray)
  }

  @Benchmark
  def serializeMassTransferVanilla_test(bh: Blackhole): Unit = {
    val vanillaTx = {
      val transfers = MassTransferTransaction
        .parseTransfersList(
          List(Transfer("3N5GRqzDBhjVXnCn44baHcz2GoZy5qLxtTh", 100000000L), Transfer("3N5GRqzDBhjVXnCn44baHcz2GoZy5qLxtTh", 200000000L)))
        .right
        .get

      MassTransferTransaction
        .create(
          Bdm,
          PublicKey.fromBase58String("FM5ojNqW7e9cZ9zhPYGkpSP1Pcd8Z3e3MNKYVS5pGJ8Z").right.get,
          transfers,
          1518091313964L,
          200000,
          Base58.tryDecodeWithLimit("59QuUcqP6p").get,
          Proofs(Seq(ByteStr.decodeBase58("FXMNu3ecy5zBjn9b69VtpuYRwxjCbxdkZ3xZpLzB8ZeFDvcgTkmEDrD29wtGYRPtyLS3LPYrL2d5UM6TpFBMUGQ").get))
        )
        .right
        .get
    }

    bh.consume(vanillaTx.bytes())
  }
}
