# 🚀 Free Tier Deployment Guide

This project is configured to run on 100% free services. Follow these steps to deploy.

## 1. Database (Postgres) - [Neon.tech](https://neon.tech)
1. Sign up/Login to **Neon.tech**.
2. Create a new project named `cafe-qr`.
3. Copy the **Connection String** (choose "Connection string" from the dashboard).
4. You will need these details for Render:
   - `SPRING_DATASOURCE_URL`: `jdbc:postgresql://<host>/neondb?sslmode=require`
   - `SPRING_DATASOURCE_USERNAME`: Provided by Neon
   - `SPRING_DATASOURCE_PASSWORD`: Provided by Neon

## 2. Cache (Redis) - [Upstash.com](https://upstash.com)
1. Sign up/Login to **Upstash**.
2. Create a "Redis" database in a region close to your Render service (e.g., `us-east-1`).
3. Under the "Details" tab, copy:
   - `SPRING_DATA_REDIS_HOST`: The endpoint string
   - `SPRING_DATA_REDIS_PORT`: The port (usually 6379)
   - `SPRING_DATA_REDIS_PASSWORD`: The password

## 3. Messaging (RabbitMQ) - [CloudAMQP.com](https://cloudamqp.com)
1. Sign up for the **Little Lemur** (Free) plan.
2. Create an instance.
3. From the dashboard, copy:
   - `SPRING_RABBITMQ_HOST`: The hostname
   - `SPRING_RABBITMQ_USERNAME`: The user
   - `SPRING_RABBITMQ_PASSWORD`: The password

## 4. Backend - [Render.com](https://render.com)
1. Push your code to your **Private** GitHub repository.
2. Go to **Render Dashboard** -> **New** -> **Blueprint**.
3. Connect your GitHub repo.
4. Render will automatically detect `render.yaml`.
5. Fill in the environment variables when prompted:
   - `SPRING_DATASOURCE_URL`: From Neon (jdbc:...neondb?sslmode=require)
   - `SPRING_RABBITMQ_HOST`: From CloudAMQP
   - `SPRING_DATA_REDIS_HOST`: From Upstash
   - `ALLOWED_ORIGINS`: Your Vercel URL (e.g., `https://cafe-qr.vercel.app`)
6. Once deployed, copy your backend URL (e.g., `https://cafe-qr-backend.onrender.com`).

## 5. Frontend - [Vercel.com](https://vercel.com)
1. Go to **Vercel Dashboard** -> **Add New** -> **Project**.
2. Select your GitHub repo (and the `cafe-qr-frontend` folder).
3. Add an environment variable:
   - `NEXT_PUBLIC_API_URL`: `https://your-backend-url.onrender.com`
4. Deploy.

---

### Important: Free Tier "Sleep"
> [!NOTE]
> Render's free tier and Neon's free tier will "sleep" after inactivity. The first request after a long break will take 30-60 seconds to respond as the services wake up.
