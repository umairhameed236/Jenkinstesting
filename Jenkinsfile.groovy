pipeline {
agent any
stages {
stage('Test') {
steps {
echo 'Welcome to AP-Testing'
  echo 'Application in Testing Phase…'
  echo 'Application Tested'
  echo 'Application Deployed'
  echo 'Application Displayed'
  
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
