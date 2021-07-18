#!/bin/bash

set -exu
eval $(minikube -p minikube docker-env)
sbt clean stage
sbt docker:publishLocal

export KUBECONFIG=~/.kube/config

#export PLAY_HTTP_PORT=8080
kubectl config set-context docker-desktop

kubectl apply -f kubernetes/namespace.json
kubectl config set-context --current --namespace=appka-1
kubectl apply -f kubernetes/akka-cluster.yml
#target/universal/stage/bin/todolist-app -Dhttp.port=8080 -Dplay.http.secret.key='QCY?tAnfk?aZ?iwrNwnxIlR6CTf:G3gf:90Latabg@5241AB`R5W:1uDFN];Ik@n'
kubectl expose deployment appka --name=appka-service --type=LoadBalancer

#minikube tunnel
#kubectl get svc
#kubectl delete namespaces appka-1
