package com.bdmplatform.history

import com.bdmplatform.db.WithDomain
import com.bdmplatform.settings.BdmSettings
import org.scalacheck.Gen
import org.scalatest.{Assertion, Suite}
import org.scalatestplus.scalacheck.{ScalaCheckDrivenPropertyChecks => GeneratorDrivenPropertyChecks}

trait DomainScenarioDrivenPropertyCheck extends WithDomain { _: Suite with GeneratorDrivenPropertyChecks =>
  def scenario[S](gen: Gen[S], bs: BdmSettings = DefaultBdmSettings)(assertion: (Domain, S) => Assertion): Assertion =
    forAll(gen) { s =>
      withDomain(bs) { domain =>
        assertion(domain, s)
      }
    }
}
