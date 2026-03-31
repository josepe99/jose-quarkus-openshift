# Run in dev

## Resource limits

Base deployments use the following hardware limits to avoid exhausting cluster resources:

- Requests:
  - CPU: `250m`
  - Memory: `256Mi`
- Limits:
  - CPU: `500m`
  - Memory: `512Mi`

If you create or edit the Deployments manually from the Kubernetes or OpenShift console, keep these same values for every application Pod.

In the dev overlay, `transactions` and `analytics` run with a higher memory limit (`1Gi`) because `mvn quarkus:dev` needs more headroom than the packaged runtime image.

## Start minikube and make sure kubectl points to it.

```sh
minikube start
kubectl config current-context
```

## Configure dev DB env

Ensure `k8s/overlays/dev/.env` contains your dev DB credentials. The dev overlay uses a Kustomize `secretGenerator` to create `postgres-secret` from this file.

Mount the repo into minikube from the repository root and keep this terminal open.

```sh
minikube mount "$(pwd)":/mnt/host
```

Verify the mount before applying the manifests. `/mnt/host` should contain `pom.xml`, `common-modules/`, and the `service-*` directories.

```sh
minikube ssh -- 'ls -la /mnt/host'
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
docker build -t jcardozo/service-bank-dev:dev -f service-bank/src/main/docker/Dockerfile.dev .
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
kubectl logs -f deploy/bank-dep
```

Set the JWT in secret
```sh
kubectl create secret generic bank-secret \
  --from-literal=JWT_TOKEN='WORKING_TOKEN' \
  -o yaml --dry-run=client | kubectl apply -f -

kubectl create configmap service-bank-config \
  --from-literal=MIDDLEWARE_URL=http://10.1.0.83:8092 \
  -o yaml --dry-run=client | oc apply -f -

kubectl apply -k k8s/overlays/dev
kubectl rollout restart deployment bank-dep

# Verify the token in the pod
kubectl get secret bank-secret
```

Show service url
```sh
minikube service service-bank --url
```

Access the services.

```sh
minikube service service-transactions --url
minikube service service-analytics --url
minikube service service-bank --url
```

Or use minikube ip with NodePorts 30001 and 30002.

Notes

Dev mode is driven by mvn quarkus:dev inside the pods, and the source is mounted from your host via /mnt/host.
If you prefer your global ~/.m2, I can update the dev overlay volume mounts to point to it and adjust the mount strategy.

## Service bank

Service `service-bank` proxies calls to the middleware API. It reads the base URL and token from the ConfigMap.

Required config (ConfigMap):

- `MIDDLEWARE_URL` (example: `http://10.1.0.83:8092`)
- `JWT_TOKEN` (Bearer token value)

Endpoints:

- `GET /api/common/centros-servicios?nombreODireccion=Microcentro`
- `GET /api/secure/common/parametros?dominio=motivos-sipap` (requires `JWT_TOKEN`)


# Run in production
## Configure production DB env

Ensure `k8s/overlays/prod/.env` contains the production database credentials. The prod overlay uses a Kustomize `secretGenerator` to create `postgres-secret` from this file, and now also deploys an internal Postgres service named `postgres-shared-db`.

Use `k8s/overlays/prod/.env.example` as a template. The minimum required values are:

```sh
POSTGRES_HOST=postgres-shared-db
POSTGRES_PORT=5432
POSTGRES_DB=<POSTGRES_DB>
POSTGRES_USER=<POSTGRES_USER>
POSTGRES_PASSWORD=<POSTGRES_PASSWORD>
```

## Create bank secret and configMap
```sh
oc -n jcardozo-playground create secret generic bank-secret \
  --from-literal=JWT_TOKEN='TOKEN-HERE' \
  -o yaml --dry-run=client | oc apply -f -

oc -n jcardozo-playground create configmap service-bank-config \
  --from-literal=MIDDLEWARE_URL=http://10.1.0.83:8092 \
  -o yaml --dry-run=client | oc apply -f -
```


```sh
export NS=jcardozo-playground
oc project $NS

# 1) Build + start OpenShift binary builds (JVM images)
mvn -pl service-transactions -am package -DskipTests
cd service-transactions
cp src/main/docker/Dockerfile.jvm ./Dockerfile
oc -n $NS new-build --binary --name=service-transactions --strategy=docker || true
oc -n $NS start-build service-transactions --from-dir=. --follow
rm Dockerfile
cd ..

mvn -pl service-analytics -am package -DskipTests
cd service-analytics
cp src/main/docker/Dockerfile.jvm ./Dockerfile
oc -n $NS new-build --binary --name=service-analytics --strategy=docker || true
oc -n $NS start-build service-analytics --from-dir=. --follow
rm Dockerfile
cd ..

mvn -pl service-bank -am package -DskipTests
cd service-bank
cp src/main/docker/Dockerfile.jvm ./Dockerfile
oc -n $NS new-build --binary --name=service-bank --strategy=docker || true
oc -n $NS start-build service-bank --from-dir=. --follow
rm Dockerfile
cd ..

# service-bank: add Dockerfile.jvm and repeat the steps above if/when it has a container build.
# 2) Make sure prod overlay points to the built ImageStream tags
# (default binary build outputs :latest)
sed -i 's|service-transactions:.*|service-transactions:latest|g' k8s/overlays/prod/images.yaml
sed -i 's|service-analytics:.*|service-analytics:latest|g' k8s/overlays/prod/images.yaml
sed -i 's|service-bank:.*|service-bank:latest|g' k8s/overlays/prod/images.yaml
# Add service-bank image override to k8s/overlays/prod/images.yaml when available.

# 3) Apply prod overlay (includes Postgres, app services, route, and secretGenerator from k8s/overlays/prod/.env)
oc -n $NS apply -k k8s/overlays/prod

# 4) Wait for rollout
oc -n $NS rollout status deploy/postgres
oc -n $NS rollout status deploy/transactions-dep
oc -n $NS rollout status deploy/analytics-dep
oc -n $NS rollout status deploy/bank-dep
```
