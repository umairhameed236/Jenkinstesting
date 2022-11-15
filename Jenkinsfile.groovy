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
    catch (Exception e) {
        print e.message
         if (!(e instanceof InterruptedException || (e.message != null && e.message.contains("task was cancelled"))) && (BRANCH_NAME != 'dev')) {
            currentBuild.result = 'FAILURE'

            step([$class                  : 'Mailer',
                  notifyEveryUnstableBuild: true,
                  recipients              : 'umair.hameed236@gmail.com',
                  sendToIndividuals       : true])
        }
    }
}

