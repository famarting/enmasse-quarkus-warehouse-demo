FROM jenkins/jenkins:lts

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
ENV CASC_JENKINS_CONFIG="/var/jenkins_home/jenkins.yml"
COPY jenkins-casc.yaml ${CASC_JENKINS_CONFIG}

ENV JENKINS_SEEDS_DIR="/var/jenkins_home/seeds/"
RUN mkdir -p ${JENKINS_SEEDS_DIR}
COPY seed.groovy ${JENKINS_SEEDS_DIR}seed.groovy

ENV JENKINS_JOBS_DIR="/var/jenkins_home/seed_workspace/ci-config/"
RUN mkdir -p ${JENKINS_JOBS_DIR}
COPY jobs ${JENKINS_JOBS_DIR}jobs
COPY src ${JENKINS_JOBS_DIR}src

USER jenkins
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt