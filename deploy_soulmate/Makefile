# SoulMate 心伴 - 部署 Makefile

.PHONY: help dev prod dev-rag-only stop stop-dev stop-prod logs logs-dev logs-prod

# 默认目标：打印可用命令
help:
	@echo "SoulMate 心伴 - 简化部署命令："
	@echo ""
	@echo "  make dev         - 一键部署开发环境 (deploy.dev.sh)"
	@echo "  make prod        - 一键部署生产环境 (deploy.prod.sh)"
	@echo "  make dev-rag-only - 只部署 MySQL + phpMyAdmin + RAG 服务"
	@echo "  make stop-dev    - 停止开发环境容器"
	@echo "  make stop-prod   - 停止生产环境容器"
	@echo "  make stop        - 停止所有容器"
	@echo "  make logs-dev    - 查看开发环境日志"
	@echo "  make logs-prod   - 查看生产环境日志"
	@echo ""

# 一键部署开发环境
dev:
	@bash deploy.dev.sh

# 一键部署生产环境
prod:
	@bash deploy.prod.sh

# 只启动 MySQL + phpMyAdmin + RAG 服务（开发环境）
dev-rag-only:
	@echo "启动 MySQL + phpMyAdmin + RAG 服务..."
	@docker compose -f docker-compose-dev.yml --env-file .env.dev up -d mysql phpmyadmin rag-service
	@echo "服务已启动："
	@echo "  - RAG 服务: http://localhost:8000"
	@echo "  - RAG API 文档: http://localhost:8000/docs"
	@echo "  - phpMyAdmin: http://localhost:8082"
	@echo "  - MySQL: localhost:3307"

# 停止开发环境
stop-dev:
	@docker compose -f docker-compose-dev.yml --env-file .env.dev down
	@echo "开发环境已停止"

# 停止生产环境
stop-prod:
	@docker compose -f docker-compose-prod.yml --env-file .env.prod down
	@echo "生产环境已停止"

# 停止所有环境
stop:
	@docker compose -f docker-compose-dev.yml --env-file .env.dev down 2>/dev/null || true
	@docker compose -f docker-compose-prod.yml --env-file .env.prod down 2>/dev/null || true
	@echo "所有服务已停止"

# 查看开发环境日志
logs-dev:
	@docker compose -f docker-compose-dev.yml --env-file .env.dev logs -f

# 查看生产环境日志
logs-prod:
	@docker compose -f docker-compose-prod.yml --env-file .env.prod logs -f
