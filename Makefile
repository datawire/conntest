GIT_SHA=$(shell git rev-parse --short --verify HEAD)
JAVABUILD=docker run

SHELL_IMAGE=openjdk:10-jdk-

DOCKER_REPO=datawire/conntest
DOCKER_REGISTRY=quay.io

default: docker-build

all: default

docker-build:
	docker build -t $(DOCKER_REPO):$(GIT_SHA) -t $(DOCKER_REPO):latest .

docker-run: docker-build
	docker run \
	-it \
	-p 7000:7000 \
	--rm $(DOCKER_REPO):$(GIT_SHA)

docker-tag:
	docker tag $(DOCKER_REPO):$(GIT_SHA) $(DOCKER_REGISTRY)/$(DOCKER_REPO):$(GIT_SHA)
	docker tag $(DOCKER_REPO):latest $(DOCKER_REGISTRY)/$(DOCKER_REPO):latest

docker-push: docker-tag
	docker push $(DOCKER_REGISTRY)/$(DOCKER_REPO):$(GIT_SHA)
	docker push $(DOCKER_REGISTRY)/$(DOCKER_REPO):latest

shell:
	docker run \
	--name conntest-$(GIT_SHA)-shell \
	-it \
	-w /src \
	-v $(PWD):/src \
	--rm $(SHELL_IMAGE) /bin/bash
