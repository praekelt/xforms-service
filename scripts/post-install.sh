#!/bin/bash

# # Exit on errors from here.
set -e

docker build -t praekelt/xforms-redis $INSTALLDIR/$REPO/docker-vms/redis
docker build -t praekelt/xforms-xforms $INSTALLDIR/$REPO/docker-vms/xforms
