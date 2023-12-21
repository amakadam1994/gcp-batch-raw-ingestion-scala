def userId = slackUserIdFromEmail("${env.BUILD_USER_EMAIL}")
pipeline {
    agent any
      tools {
        maven 'maven-3.6.3'
      }


      parameters {

      choice(name:'Airflow_Dag_Upload',choices:['NONE','gcp-batch-raw-ingestion-scala'],description:'Which Dag should be uploaded')
      choice(name:'Deploy_Jar',choices:['NONE','gcp-batch-raw-ingestion-scala-jar-with-dependencies'],description:'Which template should be build')
      choice(name:'Run_Airflow_Dag',choices:['NONE','gcp-batch-raw-ingestion-scala'],description:'Which Dag should be triggered')
      booleanParam(name:'Rule_Execution',defaultValue:false,description:'rule execution is needed?')
      choice(name:'Bucket_Name',choices:['NONE','bronze-poc-group','bronze-poc-group-archive'],description:'Which bucket rules needs to execute')

      }

    stages {
        stage('New Build') {
            steps {
                script {
                    if(params.Deploy_Jar != "NONE"){
                        echo "The build number is ${env.BUILD_NUMBER}"
                        slackSend color: 'good', message: "Hi <@$userId> your build has started and url is ${env.BUILD_URL}"
                        sh 'mvn clean package'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    if(params.Deploy_Jar != "NONE"){
                    echo 'Testing..'
                    }
                }
            }
        }

        stage('Moving jar to GCS BUCKET') {
            steps {
                script {
                    if(params.Deploy_Jar != "NONE"){
                        withEnv(['GCLOUD_PATH=/usr/lib/google-cloud-sdk/bin']) {
                        slackSend color: 'good', message: "Moving fat jar to gcs"
                        sh '$GCLOUD_PATH/gsutil cp /bitnami/jenkins/home/workspace/gcp-batch-raw-ingestion-scala/target/gcp-batch-raw-ingestion-scala-jar-with-dependencies.jar gs://bronze-poc-group/gcp-batch-raw-ingestion-scala/'
                        }
                    }
                }
            }
        }



        stage('Uploading airflow Dags') {
            steps {
                script {
                    if(params.Airflow_Dag_Upload != "NONE"){
                    slackSend color: 'good', message: "Hi <@$userId> airflow dag "+ params.Airflow_Dag_Upload +" deployed"
                    sh 'gsutil cp /bitnami/jenkins/home/workspace/gcp-batch-raw-ingestion-scala/airflow_dags/'+ params.Airflow_Dag_Upload +'.py  gs://us-central1-bronze-poc-grou-747c386f-bucket/dags'
                    }
                }
            }
        }


        stage('Running Dag with airflow') {
             steps {
                 script {
                    if(params.Run_Airflow_Dag != "NONE"){
                        withEnv(['GCLOUD_PATH=/usr/lib/google-cloud-sdk/bin']) {
                         slackSend color: 'good', message: "Running gcloud spark command"
                           sh '$GCLOUD_PATH/gcloud composer environments  run  bronze-poc-group --location us-central1  dags trigger -- gcp-batch-raw-ingestion-scala'
                         }
                    }
                 }
             }
        }

        stage('Lifecycle rules execution') {
            steps {
                script {
                   if(params.Rule_Execution == true &&  params.Bucket_Name != "NONE"){
                       withEnv(['GCLOUD_PATH=/usr/lib/google-cloud-sdk/bin']) {
                           dir('/bitnami/jenkins/home/workspace/gcp-batch-raw-ingestion-scala/lifecycle_rules/'){
                           slackSend color: 'good', message: "Executing lifecycle rules on "+ params.Bucket_Name
                                sh '$GCLOUD_PATH/gcloud storage buckets update gs://'+params.Bucket_Name+' --lifecycle-file='+params.Bucket_Name+'.json'
                           }
                       }
                   }
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