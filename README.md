# jose-quarkus-openshift
## Run in dev

Steps (Minikube)

Start minikube and make sure kubectl points to it.

```sh
minikube start
kubectl config current-context
```

Mount the repo into minikube (keep this terminal open).

```sh
minikube mount ./:/mnt/host
```

Create a local Maven cache directory in the repo (matches your hostPath).

```sh
mkdir -p ./.m2
```

Build the dev images inside minikube’s Docker daemon.

```sh
eval $(minikube docker-env)

docker build -t jcardozo/service-transactions-dev:dev -f service-transactions/src/main/docker/Dockerfile.dev .
docker build -t jcardozo/service-analytics-dev:dev -f service-analytics/src/main/docker/Dockerfile.dev .
```

Create the Postgres secret (matches both the app env vars and the quay.io/sclorg/postgresql-15-c9s image).

```sh
kubectl delete secret postgres-secret --ignore-not-found
kubectl create secret generic postgres-secret --from-env-file=.env

kubectl patch secret postgres-secret --type merge -p "{
  \"stringData\": {
    \"POSTGRESQL_DATABASE\": \"${POSTGRES_DB}\",
    \"POSTGRESQL_USER\": \"${POSTGRES_USER}\",
    \"POSTGRESQL_PASSWORD\": \"${POSTGRES_PASSWORD}\"
  }
}"
```

Apply the Kubernetes manifests.

```sh
kubectl apply -f k8s/postgres-db.yaml
kubectl apply -f k8s/app-services.dev.yaml
```

Watch pods and logs.

```sh
kubectl get pods -w
kubectl logs -f deploy/transactions-dep
kubectl logs -f deploy/analytics-dep
```

Access the services.

```sh
minikube service service-transactions --url
minikube service service-analytics --url
```

Or use minikube ip with NodePorts 30001 and 30002.

Notes

Dev mode is driven by mvn quarkus:dev inside the pods, and the source is mounted from your host via /mnt/host.
If you prefer your global ~/.m2, I can update app-services.dev.yaml to point to it and adjust the mount strategy.