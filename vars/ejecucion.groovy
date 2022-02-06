/*

    forma de invocación de método call:

    def ejecucion = load 'script.groovy'
    ejecucion.call()

*/

def call(){
  
pipeline {
    agent any
    environment {
        NEXUS_USER         = credentials('nexus-user-name')
        NEXUS_PASSWORD     = credentials('nexus-user-pass')
    }
    parameters {
        choice choices: ['Maven', 'Gradle'], description: 'Seleccione herramienta de compilacion', name: 'compileTool'
        text description: 'Enviar los stages separados por ";". Vacío significa TODOS', name: 'stages'
    }

    sh "echo  ${env.STAGE}"
    sh "echo  ${env.GIT_BRANCH}"

    stages {
        stage("Pipeline"){
            steps {
                script{
                  switch(params.compileTool)
                    {
                        case 'Maven':
                       //   maven.call(params.stages)
                        break;
                        case 'Gradle':
                       //   gradle.call(params.stages)
                        break;
                    }
                }
            }
            post{
                success{
                    slackSend color: 'good', message: "[Christian Córdova] [${JOB_NAME}] [${BUILD_TAG}] Ejecucion Exitosa", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-slack'
                }
                failure{
                    slackSend color: 'danger', message: "[Christian Córdova] [${env.JOB_NAME}] [${BUILD_TAG}] Ejecucion fallida en stage [${env.TAREA}]", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-slack'
                }
            }
        }
    }
}

}

return this;
