package com.bdmplatform.lang.v1

import java.util.concurrent.TimeUnit

import com.bdmplatform.lang.directives.values.V1
import com.bdmplatform.lang.utils
import com.bdmplatform.lang.v1.ScriptEstimatorBenchmark.St
import com.bdmplatform.lang.v2.estimator.ScriptEstimatorV2
import monix.eval.Coeval
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Array(Mode.AverageTime))
@Threads(4)
@Fork(1)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
class ScriptEstimatorBenchmark {
  @Benchmark
  def apply_test(st: St, bh: Blackhole): Unit = bh.consume(ScriptEstimatorV2(Set.empty, st.functionCosts, st.expr))
}

object ScriptEstimatorBenchmark {

  class St extends BigSum {
    val functionCosts: Map[FunctionHeader, Coeval[Long]] = utils.functionCosts(V1)
  }

}
