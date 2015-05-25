#!/bin/bash -e
docker rm -f xforms || true
docker run --rm --name=xforms --link redis:redis -p 8080:8080 -p 8081:8081 praekelt/xforms-xforms
