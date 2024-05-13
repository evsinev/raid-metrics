#!/bin/bash -eux


native-image \
  --verbose \
  --allow-incomplete-classpath \
  --no-fallback  \
  -jar ../target/raid-metrics-shaded.jar
