#!/bin/bash

//set -x 

SPRING_PROFILE=$1
SERVICE_NAME=$2
ENVIRONMENT=$3
echo "Deploying build tag: LATEST"

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
INFRA_DIR="$( cd "${SCRIPT_DIR}/../infra" && pwd )"
ROOT_DIR="$( cd "${SCRIPT_DIR}/../" && pwd )"

function createYmlsConfigMap() {
    service=${1}
    local springprofile=${2}
    echo "Creating config map $service-ymls for environment ${springprofile}"
    mkdir -p $SCRIPT_DIR/configs/output/ymls/$service
    cp $ROOT_DIR/src/main/resources/application.yml $ROOT_DIR/src/main/resources/application-${springprofile}.yml $SCRIPT_DIR/configs/output/ymls/$service
    kubectl create configmap $service-ymls --from-file=$SCRIPT_DIR/configs/output/ymls/$service -n xbankspace -o yaml --dry-run | kubectl apply -f -
}

function generateYaml () {
    deploymentName=$1
    export SPRING_PROFILE=$SPRING_PROFILE
    export ENVIRONMENT=$ENVIRONMENT
    echo "Generating the ./k8s/$deploymentName.yaml.generated file"
    envsubst < "$INFRA_DIR/k8s/$deploymentName.yaml" > "$INFRA_DIR/k8s/$deploymentName.yaml.generated"
    echo "successfully generated yaml file for deployment"
}

function k8sDeploy(){
    deploymentName=$1
    echo "Updating ${deploymentName}..."
    kubectl apply -f $INFRA_DIR/k8s/${deploymentName}.yaml.generated -n xbankspace
}

createYmlsConfigMap $SERVICE_NAME $SPRING_PROFILE

generateYaml ${SERVICE_NAME}
echo "Deploying ${SERVICE_NAME}"
k8sDeploy ${SERVICE_NAME}


