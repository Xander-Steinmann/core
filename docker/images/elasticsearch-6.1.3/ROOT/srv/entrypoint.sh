#!/bin/bash

set -e

if [[ ! -z "${ES_NETWORK_PUBLISH_HOST}" ]]; then
  echo "" >> /srv/config/elasticsearch.yml
  echo "network.publish_host: $ES_NETWORK_PUBLISH_HOST" >> /srv/config/elasticsearch.yml
fi

bin/elasticsearch
