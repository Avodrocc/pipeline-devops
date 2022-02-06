/*
	forma de invocación de método call:
	def ejecucion = load 'script.groovy'
	ejecucion.call()
*/
import utilities.*

def call(stages){

    echo 'El pipeline se ejecutará segun la rama ' + env.GIT_BRANCH
    if (env.GIT_BRANCH == 'origin/develop')
        stagesCI
    if (env.GIT_BRANCH == 'origin/release')
        stagesCD

  //  def listStages = stages.split(";")
    def listStagesOrder = [
        'build': 'stageCleanBuildTest',
        'sonar': 'stageSonar',
        'run_spring_curl': 'stageRunSpringCurl',
        'upload_nexus': 'stageUploadNexus',
        'download_nexus': 'stageDownloadNexus',
        'run_jar': 'stageRunJar',
        'curl_jar': 'stageCurlJar'
    ]

    def arrayUtils = new array.arrayExtentions();
    def stagesArray = []
    stagesArray = arrayUtils.searchKeyInArray(stages, ";", listStagesOrder)

    if (stages.isEmpty()) {
        echo 'El pipeline se ejecutará completo'
        allStages()
    } 
    else {
        echo 'Stages a ejecutar :' + stages
      /*  listStagesOrder.each { stageName, stageFunction ->
            listStages.each{ stageToExecute ->
                if(stageName.equals(stageToExecute)){
                    echo 'Ejecutando ' + stageFunction
                    "${stageFunction}"()
                 }
            }
        } */
        stagesArray.each { stageFunction ->
            echo 'Ejecutando ' + stageFunction
            "${stageFunction}"()
        }

    } 
}


def stageCleanBuildTest(){
    env.DESCRTIPTION_STAGE = 'Paso 1: Build - Test'
    stage("${env.DESCRTIPTION_STAGE}"){
        env.STAGE = "build - ${env.DESCRTIPTION_STAGE}"
        sh "echo  ${env.STAGE}"
        sh "gradle clean build"
    }
}

def stageSonar(){
    env.DESCRTIPTION_STAGE = "Paso 2: Sonar - Análisis Estático"
    stage("${env.DESCRTIPTION_STAGE}"){
        env.STAGE = "sonar - ${DESCRTIPTION_STAGE}"
        withSonarQubeEnv('sonar') {
            sh "echo  ${env.STAGE}"
            sh './gradlew sonarqube -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build'
        }
    }
}

def stageRunSpringCurl(){
    env.DESCRTIPTION_STAGE = "Paso 3: Curl Springboot Gralde sleep 60"
    stage("${env.DESCRTIPTION_STAGE}"){
        env.STAGE = "run_spring_curl - ${DESCRTIPTION_STAGE}"
        sh "echo  ${env.STAGE}"
        sh "gradle bootRun&"
        sh "sleep 60 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}

def stageUploadNexus(){
    env.DESCRTIPTION_STAGE = "Paso 4: Subir Nexus"
    stage("${env.DESCRTIPTION_STAGE}"){
                nexusPublisher nexusInstanceId: 'nexus',
        nexusRepositoryId: 'devops-usach-nexus',
        packages: [
            [$class: 'MavenPackage',
                mavenAssetList: [
                    [classifier: '',
                    extension: 'jar',
                    filePath: 'build/libs/DevOpsUsach2020-0.0.1.jar'
                ]
            ],
                mavenCoordinate: [
                    artifactId: 'DevOpsUsach2020',
                    groupId: 'com.devopsusach2020',
                    packaging: 'jar',
                    version: '0.0.1'
                ]
            ]
        ]
        env.STAGE = "upload_nexus - ${DESCRTIPTION_STAGE}"
        sh "echo  ${env.STAGE}"
    }
}

def stageDownloadNexus(){
    env.DESCRTIPTION_STAGE = "Paso 5: Descargar Nexus"
   stage("${env.DESCRTIPTION_STAGE}"){
        env.STAGE = "download_nexus - ${DESCRTIPTION_STAGE}"
        sh "echo  ${env.STAGE}"
        sh 'curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
    }
}

def stageRunJar(){
    env.DESCRTIPTION_STAGE = "Paso 6: Levantar Artefacto Jar"
    stage("${env.DESCRTIPTION_STAGE}"){
        env.STAGE = "run_jar - ${DESCRTIPTION_STAGE}"
        sh "echo  ${env.STAGE}"
        sh 'nohup bash java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
    }
}

def stageCurlJar(){
    env.DESCRTIPTION_STAGE = "Paso 7: Testear Artefacto - Dormir Esperar 60sg "
    stage("${env.DESCRTIPTION_STAGE}"){
        env.STAGE = "curl_jar - ${DESCRTIPTION_STAGE}"
        sh "echo  ${env.STAGE}"
        sh "sleep 60 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}

def stageRest(){
    env.DESCRTIPTION_STAGE = "Paso 8: Rest"
    stage("${env.DESCRTIPTION_STAGE}"){
        figlet "REST"
    }
}

def stagesCI(){
    stageCleanBuildTest()
    stageSonar()
    stageRunSpringCurl()
    stageRest()
    stageUploadNexus()
}

def stagesCD(){
    stageDownloadNexus()
    stageRunJar()
    stageRest()
    stageUploadNexus()
}

def allStages(){
    stageCleanBuildTest()
    stageSonar()
    stageRunSpringCurl()
    stageUploadNexus()
    stageDownloadNexus()
    stageRunJar()
    stageCurlJar()
}



return this;