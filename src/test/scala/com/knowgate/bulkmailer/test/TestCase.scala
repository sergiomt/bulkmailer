package com.knowgate.bulkmailer.test

trait TestCase {

  def assert(b: Boolean, m: String) = {
    if (!b) throw new RuntimeException(m)
  }

}