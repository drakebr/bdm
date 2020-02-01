package com.bdmplatform.lang

import com.bdmplatform.lang.directives.values._
import com.bdmplatform.lang.v1.evaluator.ctx.impl.bdm.Types
import org.scalatest.{FreeSpec, Matchers}

class ContextVersionTest extends FreeSpec with Matchers {

  "InvokeScriptTransaction" - {
    "exist in lib version 3" in {
      val types = Types.buildBdmTypes(true, V3)
      types.count(c => c.name == "InvokeScriptTransaction") shouldEqual 1
    }

    "doesn't exist in lib version 2" in {
      val types = Types.buildBdmTypes(true, V2)
      types.count(c => c.name == "InvokeScriptTransaction") shouldEqual 0
    }
  }
}
