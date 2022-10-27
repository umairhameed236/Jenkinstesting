node("unixaws") {
    try {
        stage("checkout") {
            checkout scm
        }
        String environment = 'dev'
        if (BRANCH_NAME == 'int')
            environment = 'int'
        else if (BRANCH_NAME == 'master')
            environment = 'prod'
        def packageJson = readJSON file: 'package.json'
        def version = packageJson.getAt('web-version')
    
        // Install correct Node version using NVM
        nvm(packageJson.getAt('engines').getAt('exact')) {
            stage("install") {
                sh "npm install"
                sh "npm rebuild node-sass"
            }
            stage("build") {
                sh "npm run test"
                sh "npm run build"
            }
        }
        stage("deploy") {
            lock("apnews-web") {
                // withCredentials in withGCloud are helpers from http://hydra/utilities/jenkins-commons
                withCredentials([file(credentialsId: "apnews-$environment-deployment", variable: 'SERVICE_ACCOUNT')]) {
                    withGCloud(SERVICE_ACCOUNT) {
                        sh 'rm -f app-config/config.js'
                        writeFile(file: 'app-config/config.js', text: readFile("app-config/config.${environment}.js"))

                        sh 'rm -f app.yaml'
                        appYaml = "${readFile('app.common.yaml')}\n${readFile("app.${environment}.yaml")}"
                        writeFile(file: 'app.yaml', text: appYaml)

                        sh "gcloud config set app/promote_by_default false"
                        // I couldn't figure out why rarely server.js doesn't exist so we simply fail the build if this happens.
                        if (!fileExists('build/server.js'))
                            throw new Exception('build/server.js doesn\'t exists')

                        if(environment == 'prod') {
                            sh "gcloud app deploy --quiet --project apnews-$environment --version $version"
                            sh "bash slackr.sh -c good -a apnews-$environment \"Web $version deployed to *${environment.toUpperCase()}*!\""
                        }
                        else {
                            sh "gcloud app deploy --quiet --project apnews-$environment --version $version"
                            if (environment != 'dev')
                                sh "bash slackr.sh -c good -a apnews-$environment \"Web $version deployed to *${environment.toUpperCase()}*!\""
                        }
                    }
                }
            }
        }
        
    }
    catch (Exception e) {
        print e.message
        if (!(e instanceof InterruptedException || (e.message != null && e.message.contains("task was cancelled"))) && (BRANCH_NAME != 'dev')) {
            currentBuild.result = 'FAILURE'

            step([$class                  : 'Mailer',
                  notifyEveryUnstableBuild: true,
                  recipients              : 'apnewsplatformsupport@ap.org',
                  sendToIndividuals       : true])
        }
    }
}

def withGCloud(credentialsJsonPath, cb) {
    withEnv(["PATH=$WORKSPACE/google-cloud-sdk/bin:$WORKSPACE/google-cloud-sdk/platform/google_appengine:$PATH", "CLOUDSDK_CONFIG=$WORKSPACE/google-cloud-sdk/_credentials"]) {
        echo 'Setting up Google Cloud SDK!'
        sh 'rm -rf google-cloud-sdk'
        sh 'curl -o  google-cloud-sdk.tar.gz https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-313.0.0-linux-x86_64.tar.gz'
        sh 'tar -xf google-cloud-sdk.tar.gz -C .'
        sh 'rm google-cloud-sdk.tar.gz'
        sh 'mkdir google-cloud-sdk/_credentials'
        sh "gcloud auth activate-service-account --key-file=$credentialsJsonPath"
        sh 'gcloud info'
        cb()
    }
}
