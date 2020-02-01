package com.bdmplatform.common
import java.util.concurrent.TimeUnit

import com.bdmplatform.state.diffs.FeeValidation
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
@Threads(4)
@Fork(1)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
class SponsorshipMathBenchmark {
  @Benchmark
  def bigDecimal_test(bh: Blackhole): Unit = {
    def toBdm(assetFee: Long, sponsorship: Long): Long = {
      val bdm = (BigDecimal(assetFee) * BigDecimal(FeeValidation.FeeUnit)) / BigDecimal(sponsorship)
      if (bdm > Long.MaxValue) {
        throw new java.lang.ArithmeticException("Overflow")
      }
      bdm.toLong
    }

    bh.consume(toBdm(100000, 100000000))
  }

  @Benchmark
  def bigInt_test(bh: Blackhole): Unit = {
    def toBdm(assetFee: Long, sponsorship: Long): Long = {
      val bdm = BigInt(assetFee) * FeeValidation.FeeUnit / sponsorship
      bdm.bigInteger.longValueExact()
    }

    bh.consume(toBdm(100000, 100000000))
  }
}
