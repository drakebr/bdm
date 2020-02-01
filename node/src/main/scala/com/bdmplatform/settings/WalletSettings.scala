package com.bdmplatform.settings

import java.io.File

import com.bdmplatform.common.state.ByteStr

case class WalletSettings(file: Option[File], password: Option[String], seed: Option[ByteStr])
