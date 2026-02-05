# Run in dev

## Start minikube and make sure kubectl points to it.

```sh
minikube start
kubectl config current-context
```

## Configure dev DB env

Ensure `k8s/overlays/dev/.env` contains your dev DB credentials. The dev overlay uses a Kustomize `secretGenerator` to create `postgres-secret` from this file.

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

Apply the Kubernetes manifests (dev overlay includes Postgres + app services).

```sh
kubectl apply -k k8s/overlays/dev
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
If you prefer your global ~/.m2, I can update the dev overlay volume mounts to point to it and adjust the mount strategy.
