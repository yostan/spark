/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.test

import scala.language.implicitConversions

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SQLConf, SQLContext}
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan

/** A SQLContext that can be used for local testing. */
class LocalSQLContext
  extends SQLContext(
    new SparkContext("local[2]", "TestSQLContext", new SparkConf()
      .set("spark.sql.testkey", "true")
      // SPARK-8910
      .set("spark.ui.enabled", "false"))) {

  override protected[sql] def createSession(): SQLSession = {
    new this.SQLSession()
  }

  protected[sql] class SQLSession extends super.SQLSession {
    protected[sql] override lazy val conf: SQLConf = new SQLConf {
      /** Fewer partitions to speed up testing. */
      override def numShufflePartitions: Int = this.getConf(SQLConf.SHUFFLE_PARTITIONS, 5)
    }
  }

  /**
   * Turn a logical plan into a [[DataFrame]]. This should be removed once we have an easier way to
   * construct [[DataFrame]] directly out of local data without relying on implicits.
   */
  protected[sql] implicit def logicalPlanToSparkQuery(plan: LogicalPlan): DataFrame = {
    DataFrame(this, plan)
  }

}

object TestSQLContext extends LocalSQLContext

