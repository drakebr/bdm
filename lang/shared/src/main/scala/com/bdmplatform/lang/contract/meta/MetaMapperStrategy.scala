package com.bdmplatform.lang.contract.meta

import com.bdmplatform.lang.contract.DApp
import com.bdmplatform.protobuf.dapp.DAppMeta

private[meta] trait MetaMapperStrategy[V <: MetaVersion] {
  def toProto(data: V#Data): Either[String, DAppMeta]
  def fromProto(meta: DAppMeta): Either[String, V#Data]
  def textMap(data: V#Data, dapp: DApp): Dic
  def protoInfo(dapp: DApp): Either[String, Dic] = fromProto(dapp.meta).map(textMap(_, dapp))
}
