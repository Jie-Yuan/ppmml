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

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.sql.{SaveMode, SparkSession}

object LogisticRegression extends TrainingUtils {
  override val spark = SparkSession.builder().master("local[*]")
    .appName(this.getClass.getCanonicalName).getOrCreate()

  def train(outputBase: String) = {
    val data = loadIris()
    val Array(trainData, testData) = data.randomSplit(Array(0.8, 0.2))

    // preprocessing
    val indexer = new StringIndexer().setInputCol("y").setOutputCol("label")
    val vectorAssembler = new VectorAssembler()
      .setInputCols(Array("x1", "x2", "x3", "x4")).setOutputCol("features")
    val classifier = new LogisticRegression()
      .setTol(1e-4)
      .setLabelCol("label")
      .setFeaturesCol("features")
    // pipeline data
    val pipeline = new Pipeline().setStages(Array(indexer, vectorAssembler, classifier))
    val model = pipeline.fit(trainData)

    // output spark model
    println(s"saving model to ${outputBase}")
    model.write.overwrite().save(s"${outputBase}/logistic_regression_model")
    saveSchema(trainData.schema, s"${outputBase}/logistic_regression.json")
    doEvaluate(model, testData)
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
