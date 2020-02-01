package com.bdmplatform.lang.v1

import cats.Id

package object task {
  type TaskM[S, E, R] = TaskMT[Id, S, E, R]
}
