#!/bin/bash

# Docker 인프라 서비스 관리 스크립트

set -e

COMMAND=${1:-help}
shift

case $COMMAND in
  up)
    echo "Starting infrastructure services..."
    docker-compose -f docker-compose.infra.yml up -d "$@"
    echo "Infrastructure services started successfully!"
    ;;
  down)
    echo "Stopping infrastructure services..."
    docker-compose -f docker-compose.infra.yml down "$@"
    echo "Infrastructure services stopped!"
    ;;
  restart)
    echo "Restarting infrastructure services..."
    docker-compose -f docker-compose.infra.yml restart "$@"
    echo "Infrastructure services restarted!"
    ;;
  logs)
    docker-compose -f docker-compose.infra.yml logs -f "$@"
    ;;
  ps)
    docker-compose -f docker-compose.infra.yml ps "$@"
    ;;
  clean)
    echo "Stopping and removing infrastructure services with volumes..."
    docker-compose -f docker-compose.infra.yml down -v "$@"
    echo "Infrastructure services and volumes removed!"
    ;;
  help)
    echo "Usage: $0 {up|down|restart|logs|ps|clean|help} [options]"
    echo ""
    echo "Commands:"
    echo "  up       - Start infrastructure services"
    echo "  down     - Stop infrastructure services"
    echo "  restart  - Restart infrastructure services"
    echo "  logs     - Show logs (optionally specify service name)"
    echo "  ps       - Show running services"
    echo "  clean    - Stop services and remove volumes"
    echo "  help     - Show this help message"
    ;;
  *)
    echo "Invalid command: $COMMAND"
    echo "Run '$0 help' for usage information"
    exit 1
    ;;
esac