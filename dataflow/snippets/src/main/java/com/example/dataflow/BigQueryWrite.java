package com.example.dataflow;

// [START dataflow_bigquery_write]
import com.google.api.services.bigquery.model.TableRow;
import java.util.Arrays;
import java.util.List;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;

public class BigQueryWrite {
  // A custom datatype for the source data.
  @DefaultCoder(AvroCoder.class)
  public static class MyData {
    public String name;
    public Long age;
    public MyData() {}
    public MyData(String name, Long age)
    {
      this.name = name;
      this.age = age;
    }
  }

  public static void main(String[] args) {
    // Example source data.
    final List<MyData> data = Arrays.asList(
        new MyData("Alice", 40L),
        new MyData("Bob", 30L),
        new MyData("Charlie", 20L)
    );

    // Parse the pipeline options passed into the application. Example:
    //   --projectId=$PROJECT_ID --datasetName=$DATASET_NAME --tableName=$TABLE_NAME
    // For more information, see https://beam.apache.org/documentation/programming-guide/#configuring-pipeline-options
    PipelineOptionsFactory.register(ExamplePipelineOptions.class);
    ExamplePipelineOptions options = PipelineOptionsFactory.fromArgs(args)
        .withValidation()
        .as(ExamplePipelineOptions.class);

    // Create a pipeline and apply transforms.
    Pipeline pipeline = Pipeline.create(options);
    pipeline.
        // Create an in-memory PCollection of MyData objects.
        apply(Create.of(data))
        // Write the data to an exiting BigQuery table.
        .apply(BigQueryIO.<MyData>write()
            .to(String.format("%s:%s.%s",
                options.getProjectId(),
                options.getDatasetName(),
                options.getTableName()))
            .withFormatFunction(
                (MyData x) -> new TableRow().set("user_name",x.name).set("age",x.age))
            .withCreateDisposition(CreateDisposition.CREATE_NEVER)
            .withWriteDisposition(WriteDisposition.WRITE_APPEND)
            .withMethod(Write.Method.STORAGE_WRITE_API));
    pipeline.run().waitUntilFinish();
  }
}
// [END dataflow_bigquery_write]
