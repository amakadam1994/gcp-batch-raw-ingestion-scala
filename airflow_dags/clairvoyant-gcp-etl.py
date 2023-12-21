from airflow import DAG
from airflow.utils.dates import days_ago

from airflow.providers.google.cloud.operators.dataproc import DataprocCreateClusterOperator, DataprocSubmitJobOperator, DataprocDeleteClusterOperator


default_arguments = {"owner": "Amar", "start_date": days_ago(1)}



SPARK_JOB = {
    "reference": {"project_id": "playground-375318"},
    "placement": {"cluster_name": "gcp-batch-raw-ingestion-scala"},
    "spark_job": {
        "jar_file_uris": ["gs://bronze-poc-group/gcp-batch-raw-ingestion-scala/cv-gcp-etl-jar-with-dependencies.jar","gs://bronze-poc-group/gcp-batch-raw-ingestion/dataproc/scopt_2.12-4.0.1.jar","gs://bronze-poc-group/gcp-batch-raw-ingestion/dataproc/jars/spark-3.1-bigquery-0.31.1.jar"],
        "main_class": "com.clairvoyant.app.CLAIRVOYANTApp",
    },
}

args = {
    'owner': 'Airflow',
}

with DAG(
        "gcp-batch-raw-ingestion-scala",
        schedule_interval=None,
        catchup=False,
        default_args=default_arguments,
) as dag:
    create_cluster = DataprocCreateClusterOperator(
        task_id='create_cluster',
        project_id='playground-375318',
        cluster_name='gcp-batch-raw-ingestion-scala',
        num_workers=2,
        worker_machine_type='n1-standard-2',
        storage_bucket="dataproc_ravi_poc",
        region='us-central1',
        zone='',
    )

    spark_submit = DataprocSubmitJobOperator(
        task_id="spark_submit", job=SPARK_JOB, region='us-central1', project_id='playground-375318'
    )

    delete_cluster = DataprocDeleteClusterOperator(
        task_id="delete_cluster",
        project_id="playground-375318",
        cluster_name="gcp-batch-raw-ingestion-scala",
        region='us-central1',
        trigger_rule="all_done",
    )

create_cluster >> spark_submit >> delete_cluster