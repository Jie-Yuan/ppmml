/*
 * Copyright (c) 2017 the ppmml authors. All Rights Reserved
 * ppmml is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * ppmml is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with ppmml. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ppmml.spark.examples

import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.{DataFrame, SparkSession}

object KMeans extends TrainingUtils {
  val spark = SparkSession.builder()
    .master("local[*]").appName(this.getClass.getName).getOrCreate()

  def train(outputBase: String) = {
    val data: DataFrame = loadIris()
    val Array(trainData, testData) = data.randomSplit(Array(0.8, 0.2))
    // preprocessing
    val vectorAssembler = new VectorAssembler()
      .setInputCols(Array("x1", "x2", "x3", "x4")).setOutputCol("features")
    val cluster = new KMeans().setK(3).setFeaturesCol("features")
    // pipeline data
    val pipeline = new Pipeline().setStages(Array(vectorAssembler, cluster))
    val model: PipelineModel = pipeline.fit(trainData)
    // Save model and schema
    println(s"saving model to ${outputBase}")
    model.write.overwrite().save(s"${outputBase}/kmeans_model")
    saveSchema(trainData.schema, s"${outputBase}/kmeans.json")
  }

  def main(args: Array[String]): Unit = {
    val outputBasePath = if (args.length >= 1) {
      args(0)
    } else {
      "./spark-models"
    }
    train(outputBasePath)
  }

}
