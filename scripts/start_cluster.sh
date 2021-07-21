#!/bin/bash

set -exu
#eval $(minikube -p minikube docker-env)
sbt docker:publishLocal

#export KUBECONFIG=~/.kube/config
kubectl config set-context docker-desktop

kubectl apply -f kubernetes/namespace.json
kubectl config set-context --current --namespace=todolist-app-1
kubectl apply -f kubernetes/akka-cluster.yml
kubectl expose deployment todolist-app --type=LoadBalancer --name=todolist-app-service


#minikube service todolist-app-service -n todolist-app-1 --url
#minikube start --network bridge