#!/bin/bash -e
docker rm -f redis || true
docker run --rm --name=redis praekelt/xforms-redis
