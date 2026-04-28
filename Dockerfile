# ---- Build stage ----
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files trước để tận dụng Docker layer cache
COPY package.json package-lock.json ./
RUN npm ci

# Copy toàn bộ source
COPY . .

# Build Next.js (env vars được inject lúc runtime qua docker-compose)
# NEXT_PUBLIC_* cần có lúc build nên truyền vào đây
ARG NEXT_PUBLIC_API_URL
ARG NEXT_PUBLIC_SOCKET_ENDPOINT
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_SOCKET_ENDPOINT=$NEXT_PUBLIC_SOCKET_ENDPOINT

RUN npm run build

# ---- Runtime stage ----
FROM node:20-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production

# Chỉ copy những thứ cần thiết để chạy
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

EXPOSE 3000

CMD ["node", "server.js"]
