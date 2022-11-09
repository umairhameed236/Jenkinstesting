pipeline {
agent any
stages {
stage('Test') {
steps {
echo 'Application in Testing Phase…'
  echo 'Application Tested'
}
}
stage('Deploy CloudHub') {
steps {
echo 'Deploying mule project due to the latest code commit…'
echo 'Deploying to the configured environment….'

}
}
}
post {
        always {
            emailext body: 'A Test EMail', recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']], subject: 'Test'
        }
    }
}
