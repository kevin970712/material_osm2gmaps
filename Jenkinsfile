pipeline {
  agent { label 'android-x86' }

  environment {
    ANDROID_HOME     = "/home/jenkins/android-sdk"
    ANDROID_SDK_ROOT = "/home/jenkins/android-sdk"
    PATH             = "${env.PATH}:" +
                       "${ANDROID_HOME}/cmdline-tools/latest/bin:" +
                       "${ANDROID_HOME}/platform-tools"
  }

  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
    ansiColor('xterm')
  }

  stages {
    stage('Clean workspace') {
      steps { deleteDir() }
    }

    stage('Checkout source') {
      steps { checkout scm }
    }

    stage('Install SDK & NDK') {
      steps {
        sh '''
          set -eux
          sdkmanager --sdk_root="$ANDROID_HOME" \
            "platform-tools" \
            "platforms;android-34" \
            "build-tools;34.0.0" \
            "ndk;27.0.12077973"
          yes | sdkmanager --sdk_root="$ANDROID_HOME" --licenses
        '''
      }
    }

    stage('Prepare signing') {
      steps {
        withCredentials([
          file  (credentialsId: 'osm2gmaps-keystore', variable: 'KS_FILE'),
          string(credentialsId: 'osm2gmaps-ks-pass',  variable: 'KS_PASS'),
          string(credentialsId: 'osm2gmaps-key-pass',variable: 'KEY_PASS'),
          string(credentialsId: 'osm2gmaps-alias',   variable: 'KEY_ALIAS'),
        ]) {
          sh '''
            set -eux
            mkdir -p app
            cp "$KS_FILE" app/release.keystore

            cat > app/key.properties <<EOF
storeFile=release.keystore
storePassword=$KS_PASS
keyAlias=$KEY_ALIAS
keyPassword=$KEY_PASS
EOF
          '''
        }
      }
    }

    stage('Build signed release') {
      steps {
        sh '''
          set -eux
          chmod +x ./gradlew
          ./gradlew clean assembleRelease
        '''
      }
    }

    stage('Copy, Archive & Publish APK') {
      steps {
        sh '''
          set -eux
          # move the one signed-and-zipaligned APK into the workspace root
          cp app/build/outputs/apk/release/net.retiolus.osm2gmaps-v*.apk .
        '''
        archiveArtifacts   artifacts: 'net.retiolus.osm2gmaps-v*.apk', fingerprint: true
        publishGiteaAssets assets:    'net.retiolus.osm2gmaps-v*.apk'
      }
    }
  }
}
