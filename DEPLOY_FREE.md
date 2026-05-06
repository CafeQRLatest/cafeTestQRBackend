# 🚀 Free Tier Deployment Guide

This project is configured to run on 100% free services. Follow these steps to deploy.

## 1. Database (Postgres) - [Neon.tech](https://neon.tech) or [Supabase.com](https://supabase.com)
### Option A: Neon.tech
1. Sign up/Login to **Neon.tech**.
2. Create a new project named `cafe-qr`.
3. Copy the **Connection String** (choose "Connection string" from the dashboard).

### Option B: Supabase.com
1. Sign up/Login to **Supabase**.
2. Create a new project named `cafe-qr`.
3. Go to **Project Settings** -> **Database**.
4. Copy the **Connection URI** (use "Transaction mode" on port 6543 for better connection pooling).

### Final Details for Render:
- `SPRING_DATASOURCE_URL`: The Connection String/URI
- `SPRING_DATASOURCE_USERNAME`: Provided by provider
- `SPRING_DATASOURCE_PASSWORD`: Provided by provider

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

## 4. Email Delivery
Render free web services block outbound SMTP traffic on ports `25`, `465`, and `587`. Gmail SMTP uses `587` or `465`, so Gmail SMTP will not send email from a free Render backend even when the username and app password are correct.

Use one of these options:

1. Upgrade the Render backend to a paid instance, then Gmail SMTP can use the normal settings below.
2. Stay on free Render and use a mail provider that supports an HTTPS email API.
3. Stay on free Render and use an SMTP provider that supports an allowed alternate port such as `2525`.

For Gmail on a paid Render instance or local development:
   - Go to **Google Account Settings** -> **Security**.
   - Enable **2-Step Verification**.
   - Search for **App Passwords**.
   - Create a new App Password for "Mail" on "Other (Custom name: Cafe-QR)".

You will need:
   - `SMTP_HOST`: `smtp.gmail.com`
   - `SMTP_PORT`: `587`
   - `SMTP_USERNAME`: Your Gmail address
   - `SMTP_PASSWORD`: The 16-character App Password

## 5. Backend - [Render.com](https://render.com)
1. Push your code to your **Private** GitHub repository.
2. Go to **Render Dashboard** -> **New** -> **Blueprint**.
3. Connect your GitHub repo.
4. Render will automatically detect `render.yaml`.
5. Fill in the environment variables when prompted:
    - `SPRING_DATASOURCE_URL`: From Neon (jdbc:...neondb?sslmode=require)
    - `SPRING_RABBITMQ_HOST`: From CloudAMQP
    - `SPRING_DATA_REDIS_HOST`: From Upstash
    - `SMTP_USERNAME`: Your Gmail address
    - `SMTP_PASSWORD`: Your Gmail App Password
    - `ALLOWED_ORIGINS`: Your Vercel URL (e.g., `https://cafe-qr.vercel.app`)
6. Once deployed, copy your backend URL (e.g., `https://cafe-qr-backend.onrender.com`).

## 6. Frontend - [Vercel.com](https://vercel.com)
1. Go to **Vercel Dashboard** -> **Add New** -> **Project**.
2. Select your GitHub repo (and the `cafe-qr-frontend` folder).
3. Add an environment variable:
   - `NEXT_PUBLIC_API_URL`: `https://your-backend-url.onrender.com`
4. Deploy.

---

### Important: Free Tier "Sleep"
> [!NOTE]
> Render's free tier and Neon's free tier will "sleep" after inactivity. The first request after a long break will take 30-60 seconds to respond as the services wake up.
