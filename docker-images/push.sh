#!/bin/bash

TAG="$1"

docker push walmartlabs/concord-server:${TAG}
docker push walmartlabs/concord-server:latest

docker push walmartlabs/concord-console:${TAG}
docker push walmartlabs/concord-console:latest

docker push walmartlabs/concord-ansible:${TAG}
docker push walmartlabs/concord-ansible:latest

docker push walmartlabs/concord-agent:${TAG}
docker push walmartlabs/concord-agent:latest
