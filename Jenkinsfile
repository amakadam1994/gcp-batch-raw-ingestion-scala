def userId = slackUserIdFromEmail("${BUILD_USER_EMAIL}")
pipeline {
    agent any
      tools {
        maven 'maven-3.6.3'
      }

    stages {
        stage('New Build') {
            steps {
             echo "The build number is ${env.BUILD_NUMBER}"
             slackSend color: 'good', message: "Hi <@$userId> your build has started and url is ${env.BUILD_URL}"
              sh 'mvn clean package'
            }
        }

        stage('Test') {
            steps {
                echo 'Testing..'
            }
        }

        stage('Moving jar to GCS BUCKET') {
            steps {
                  withEnv(['GCLOUD_PATH=/usr/lib/google-cloud-sdk/bin']) {
                  slackSend color: 'good', message: "Moving fat jar to gcs"
                     sh '$GCLOUD_PATH/gsutil cp /bitnami/jenkins/home/workspace/gcp-batch-raw-ingestion-scala/target/gcp-batch-raw-ingestion-scala-jar-with-dependencies.jar gs://bronze-poc-group/gcp-batch-raw-ingestion-scala/'
                    }
                 }
              }
        stage('Running Dag with airflow') {
             steps {
                    withEnv(['GCLOUD_PATH=/usr/lib/google-cloud-sdk/bin']) {
                     slackSend color: 'good', message: "Running gcloud spark command"
                       //sh '$GCLOUD_PATH/gcloud dataproc jobs submit spark --cluster=cluster-e7b7 --region=us-central1 --class=org.example.hello --jars=gs://dataproc_ravi_poc/spark_jar/spark-scala-etl-1.0-SNAPSHOT-jar-with-dependencies.jar'
                       sh '$GCLOUD_PATH/gcloud composer environments  run  data-generator-demo --location us-central1  dags trigger -- gcp-batch-raw-ingestion-scala'

                    }
                 }
              }
           }
           post {
                  success {
                      slackSend color: 'good', message: "Hi <@$userId> Airflow dag is trigged please check the ui"
                  }
                  failure {
                     slackSend color: 'danger', message: "Hi <@$userId> your build has failed pleas check ${env.BUILD_URL}"
                  }
               }
}