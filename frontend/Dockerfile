# 校园自习室预约系统 - 前端镜像（使用预编译 dist）
FROM nginx:alpine

COPY frontend/dist /usr/share/nginx/html
COPY docker/nginx/default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
