pipeline {
agent any
stages {
stage('Test') {
steps {
echo 'Application in Testing Phase…'
}
}
stage('Deploy CloudHub') {
environment {
ANYPOINT_CREDENTIALS = credentials('anypointPlatform')
}
steps {
echo 'Deploying mule project due to the latest code commit…'
echo 'Deploying to the configured environment….'

}
}
}
}
