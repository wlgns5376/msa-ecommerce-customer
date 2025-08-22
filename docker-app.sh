#!/bin/bash

# Docker 애플리케이션 관리 스크립트

set -e

COMMAND=${1:-help}
shift

case $COMMAND in
  up)
    echo "Starting application..."
    docker-compose -f docker-compose.app.yml up -d "$@"
    echo "Application started successfully!"
    ;;
  down)
    echo "Stopping application..."
    docker-compose -f docker-compose.app.yml down "$@"
    echo "Application stopped!"
    ;;
  restart)
    echo "Restarting application..."
    docker-compose -f docker-compose.app.yml restart "$@"
    echo "Application restarted!"
    ;;
  build)
    echo "Building application..."
    docker-compose -f docker-compose.app.yml build "$@"
    echo "Application built successfully!"
    ;;
  logs)
    docker-compose -f docker-compose.app.yml logs -f "$@"
    ;;
  ps)
    docker-compose -f docker-compose.app.yml ps "$@"
    ;;
  help)
    echo "Usage: $0 {up|down|restart|build|logs|ps|help} [options]"
    echo ""
    echo "Commands:"
    echo "  up       - Start application"
    echo "  down     - Stop application"
    echo "  restart  - Restart application"
    echo "  build    - Build application image"
    echo "  logs     - Show logs"
    echo "  ps       - Show running services"
    echo "  help     - Show this help message"
    ;;
  *)
    echo "Invalid command: $COMMAND"
    echo "Run '$0 help' for usage information"
    exit 1
    ;;
esac