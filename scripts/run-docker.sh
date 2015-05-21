docker run -d --restart=on-failure:10 --name redis praekelt/xforms-redis
docker run -d --restart=on-failure:10 -p 8080:8080 -p 8081:8081 --link redis:redis --name xforms praekelt/xforms-xforms
