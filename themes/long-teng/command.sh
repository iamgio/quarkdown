#!/bin/bash
set -e
set -u

ENV=local
PARAMETER=

BASE_DIR=$(dirname $0)
SCRIPT_PATH="$( cd "${BASE_DIR}" && pwd -P )"

load_env(){
  ENV_FILE="${SCRIPT_PATH}/env/${ENV}.env"
  if test -f "${ENV_FILE}"; then
      source "${ENV_FILE}"
  fi
}
load_env

exit_err() {
  echo "ERROR: ${1}" >&2
  exit 1
}

# Usage: -h, --help
# Description: Show help text
option_help() {
  printf "Usage: %s [options...] COMMAND <parameter> \n\n" "${0}"
  printf "Default command: --help\n\n"

  echo "Options:"
  grep -e '^[[:space:]]*# Usage:' -e '^[[:space:]]*# Description:' -e '^option_.*()[[:space:]]*{' "${0}" | while read -r usage; read -r description; read -r option; do
    if [[ ! "${usage}" =~ Usage ]] || [[ ! "${description}" =~ Description ]] || [[ ! "${option}" =~ ^option_ ]]; then
      exit_err "Error generating help text."
    fi
    printf " %-32s %s\n" "${usage##"# Usage: "}" "${description##"# Description: "}"
  done

  printf "\n"
  echo "Commands:"
  grep -e '^[[:space:]]*# Command Usage:' -e '^[[:space:]]*# Command Description:' -e '^command_.*()[[:space:]]*{' "${0}" | while read -r usage; read -r description; read -r command; do
    if [[ ! "${usage}" =~ Usage ]] || [[ ! "${description}" =~ Description ]] || [[ ! "${command}" =~ ^command_ ]]; then
      exit_err "Error generating help text."
    fi
    printf " %-32s %s\n" "${usage##"# Command Usage: "}" "${description##"# Command Description: "}"
  done
}

# Command Usage: tag
# Command Description: Git release with version
command_tag() {
  git tag ${PARAMETER} && git push origin ${PARAMETER}
}


check_msg() {
  printf "\xE2\x9C\x94 ${1}\n"
}

main() {
  [[ -z "${@}" ]] && eval set -- "--help"

  local theCommand=

  set_command() {
    [[ -z "${theCommand}" ]] || exit_err "Only one command at a time!"
    theCommand="${1}"
  }

  while (( ${#} )); do
    case "${1}" in

      --help|-h)
        option_help
        exit 0
        ;;

      tag|test|coverage|bump|rn)
        set_command "${1}"
        ;;

      *)
        PARAMETER="${1}"
        ;;
    esac

    shift 1
  done

  [[ ! -z "${theCommand}" ]] || exit_err "Command not found!"

  case "${theCommand}" in
    tag) command_tag;;

    *) option_help; exit 1;;
  esac
}

main "${@-}"
