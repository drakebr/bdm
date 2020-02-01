package com.bdmplatform.lang

import com.bdmplatform.lang.v1.BaseGlobal

package object hacks {
  private[lang] val Global: BaseGlobal = com.bdmplatform.lang.Global // Hack for IDEA
}
