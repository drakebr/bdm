package com.bdmplatform.network

import com.bdmplatform.TransactionGen
import com.bdmplatform.common.state.ByteStr
import com.bdmplatform.crypto._
import org.scalacheck.Gen
import org.scalatest.concurrent.Eventually
import org.scalatest.{FreeSpec, Matchers}
import org.scalatestplus.scalacheck.{ScalaCheckPropertyChecks => PropertyChecks}

class MicroBlockInvSpecSpec extends FreeSpec with Matchers with PropertyChecks with Eventually with TransactionGen {

  private val microBlockInvGen: Gen[MicroBlockInv] = for {
    acc          <- accountGen
    totalSig     <- byteArrayGen(SignatureLength)
    prevBlockSig <- byteArrayGen(SignatureLength)
  } yield MicroBlockInv(acc, ByteStr(totalSig), ByteStr(prevBlockSig))

  "MicroBlockInvMessageSpec" - {
    import MicroBlockInvSpec._

    "deserializeData(serializedData(data)) == data" in forAll(microBlockInvGen) { inv =>
      inv.signaturesValid() shouldBe 'right
      val restoredInv = deserializeData(serializeData(inv)).get
      restoredInv.signaturesValid() shouldBe 'right

      restoredInv shouldBe inv
    }
  }

}
