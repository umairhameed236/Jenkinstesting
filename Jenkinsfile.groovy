pipeline {
agent any
stages {
stage('Test') {
steps {
echo 'Application in Testing Phase…'
  echo 'Application Tested'
  echo 'Application Deployed'
}
}
stage('Deploy CloudHub') {
steps {
echo 'Deploying mule project due to the latest code commit…'
echo 'Deploying to the configured environment….'

}
}
}
}
