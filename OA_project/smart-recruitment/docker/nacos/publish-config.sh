#!/bin/sh
set -eu

NACOS_SERVER_ADDR="${NACOS_SERVER_ADDR:-nacos:8848}"
NACOS_CONFIG_GROUP="${NACOS_CONFIG_GROUP:-SMART_RECRUITMENT}"
NACOS_NAMESPACE="${NACOS_NAMESPACE:-}"
NACOS_USERNAME="${NACOS_USERNAME:-}"
NACOS_PASSWORD="${NACOS_PASSWORD:-}"
CONFIG_DIR="${CONFIG_DIR:-/config}"
MAX_WAIT_SECONDS="${MAX_WAIT_SECONDS:-120}"
BASE_URL="http://${NACOS_SERVER_ADDR}/nacos/v1"

echo "Waiting for Nacos at ${NACOS_SERVER_ADDR}..."
waited_seconds=0
until curl --fail --silent --connect-timeout 2 --max-time 5 \
  "${BASE_URL}/console/health/readiness" >/dev/null; do
  if [ "${waited_seconds}" -ge "${MAX_WAIT_SECONDS}" ]; then
    echo "Nacos was not ready after ${MAX_WAIT_SECONDS} seconds." >&2
    exit 1
  fi
  sleep 2
  waited_seconds=$((waited_seconds + 2))
done

ACCESS_TOKEN=""
if [ -n "${NACOS_USERNAME}" ] && [ -n "${NACOS_PASSWORD}" ]; then
  auth_response="$(curl --fail --silent --show-error \
    --connect-timeout 5 --max-time 30 --retry 2 --retry-all-errors -X POST \
    "${BASE_URL}/auth/users/login" \
    --data-urlencode "username=${NACOS_USERNAME}" \
    --data-urlencode "password=${NACOS_PASSWORD}")"
  ACCESS_TOKEN="$(printf '%s' "${auth_response}" | sed -n 's/.*"accessToken"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
  if [ -z "${ACCESS_TOKEN}" ]; then
    echo "Nacos authentication succeeded without returning an access token." >&2
    exit 1
  fi
fi

found_config=false
for config_file in "${CONFIG_DIR}"/*.yml; do
  if [ ! -f "${config_file}" ]; then
    continue
  fi
  found_config=true
  data_id="$(basename "${config_file}")"
  set -- \
    --data-urlencode "dataId=${data_id}" \
    --data-urlencode "group=${NACOS_CONFIG_GROUP}" \
    --data-urlencode "type=yaml" \
    --data-urlencode "content@${config_file}"

  if [ -n "${NACOS_NAMESPACE}" ]; then
    set -- "$@" --data-urlencode "tenant=${NACOS_NAMESPACE}"
  fi
  if [ -n "${ACCESS_TOKEN}" ]; then
    set -- "$@" --data-urlencode "accessToken=${ACCESS_TOKEN}"
  fi

  result="$(curl --fail --silent --show-error \
    --connect-timeout 5 --max-time 30 --retry 2 --retry-all-errors \
    -X POST "${BASE_URL}/cs/configs" "$@")"
  if [ "${result}" != "true" ]; then
    echo "Failed to publish ${data_id}: ${result}" >&2
    exit 1
  fi
  echo "Published ${data_id} to group ${NACOS_CONFIG_GROUP}."
done

if [ "${found_config}" != "true" ]; then
  echo "No .yml configuration files found in ${CONFIG_DIR}." >&2
  exit 1
fi
