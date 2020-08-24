pipeline {
	agent any

	environment{
		registry = "clms/typhon-polystore-api"
			registryCredential = 'Nemo_account'
	}

	tools {
		maven 'Maven 3.6.0'
	}

	stages {
		// stage('Build') {
		// 	steps {
		// 	    configFileProvider([configFile(fileId: 'c262b5dc-6fc6-40eb-a271-885950d8cf70', variable: 'MAVEN_SETTINGS')]) {
		// 	        withCredentials([usernamePassword(credentialsId: 'jib-creds', usernameVariable: 'REGISTRY_USERNAME', passwordVariable: 'REGISTRY_PASSWORD')]) {
		// 		        sh "mvn -U -gs $MAVEN_SETTINGS -f com.clms.typhonapi clean install jib:build"
		// 		    }
		// 		}
		// 	}
		// }

		stage('Deploy (master)') {
			when {
				expression { env.BRANCH_NAME == "master" }
			}
			steps{
				configFileProvider([configFile(fileId: 'c262b5dc-6fc6-40eb-a271-885950d8cf70', variable: 'MAVEN_SETTINGS')]) {
						withCredentials([usernamePassword(credentialsId: 'jib-creds', usernameVariable: 'REGISTRY_USERNAME', passwordVariable: 'REGISTRY_PASSWORD')]) {
						// withCredentials([usernamePassword(credentialsId: 'Nemo_account', usernameVariable: 'REGISTRY_USERNAME', passwordVariable: 'REGISTRY_PASSWORD')]) {
							// sh 'mvn -U -B -gs $MAVEN_SETTINGS clean compile jib:build  -Djib.to.tags="latest,${BUILD_NUMBER}"'
							sh 'mvn -U -gs $MAVEN_SETTINGS -f com.clms.typhonapi clean install jib:build -Djib.to.tags="latest,${BUILD_NUMBER}"'
					}
				}
			}
		}

		stage('Deploy (develop)') {
			when {
				expression { env.BRANCH_NAME == "dev" }
			}
			steps{
				configFileProvider([configFile(fileId: 'c262b5dc-6fc6-40eb-a271-885950d8cf70', variable: 'MAVEN_SETTINGS')]) {
						withCredentials([usernamePassword(credentialsId: 'jib-creds', usernameVariable: 'REGISTRY_USERNAME', passwordVariable: 'REGISTRY_PASSWORD')]) {
						// withCredentials([usernamePassword(credentialsId: 'Nemo_account', usernameVariable: 'REGISTRY_USERNAME', passwordVariable: 'REGISTRY_PASSWORD')]) {
							// sh 'mvn -U -B -gs $MAVEN_SETTINGS clean compile jib:build  -Djib.to.tags="dev"'
							sh 'mvn -U -gs $MAVEN_SETTINGS -f com.clms.typhonapi clean install jib:build -Djib.to.tags="dev"'
					}
				}
			}
		}
	}
}