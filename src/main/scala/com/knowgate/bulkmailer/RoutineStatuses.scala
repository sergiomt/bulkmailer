package com.knowgate.bulkmailer

/**
 * © Copyright 2016 the original author.
 * This file is licensed under the Apache License version 2.0.
 * You may not use this file except in compliance with the license.
 * You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.
 */

case object UNKNOWN extends RoutineStatus(-2) {
  override def name = "UNKNOWN"
}

case object ABORTED extends RoutineStatus(-1) {
  override def name = "ABORTED"
}

case object FINISHED extends RoutineStatus(0) {
  override def name = "FINISHED"
}

case object PENDING extends RoutineStatus(1) {
  override def name = "PENDING"
}

case object SUSPENDED extends RoutineStatus(2) {
  override def name = "SUSPENDED"
}

case object RUNNING extends RoutineStatus(3) {
  override def name = "RUNNING"
}

case object INTERRUPTED extends RoutineStatus(4) {
  override def name = "INTERRUPTED"
}
