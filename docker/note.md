# Docker network 생성

    Docker 컨테이너(container)는 격리된 환경에서 돌아가기 때문에 기본적으로 다른 컨테이너와의 통신이 불가능합니다. 하지만 여러 개의 컨테이너를 하나의 Docker 네트워크(network)에 연결시키면 서로 통신이 가능해집니다.

```
$ docker network create --gateway 172.18.0.1 --subnet 172.18.0.0/16 ecommerce-network

$ docker network ls

$ docker network inspect ecommerce-network
```

# RabbitMQ

```
# 위에서 만든 ecommerce-network에 연결
# 4369 포트는 EPMD(Erlang Port Mapper Daemon)를 위해 사용됨
# 5671 포트는 TLS(Transport Layer Security)을 위해 사용됨

$ docker run -d --name rabbitmq --network ecommerce-network -p 15672:15672 -p 5672:5672 -p 15671:15671 -p 5671:5671 -p 4369:4369 rabbitmq:management
```

# config service

```
# 같은 ecommerce-network 네트워크에 미리 등록해둔 rabbitmq 컨테이너 사용.(컨테이너의 이름을 지정하면 된다.)

$ docker run -d -p 8888:8888 --network ecommerce-network -e "spring.rabbitmq.host=rabbitmq" -e "spring.profiles.active=default" --name config-service ygasok21/config-service:1.0
```

# apigateway service

```
$ docker run -d -p 8000:8000 --network ecommerce-network \

 -e "spring.cloud.config.uri=http://config-service:8888" \

 -e "spring.rabbitmq.host=rabbitmq" \

 -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" \

 --name apigateway-service \

 ygasok21/apigateway-service:1.0
```

# mariadb

```
$ docker run -d -p 3306:3306 --network ecommerce-network --name mariadb -v ~/dev/Shared/data/mariadb:/var/lib/mysql ygasok21/my_mariadb:1.0

$ 다른 마이크로서비스에서 접근할 수 있도록 권한 수정
$ docker exec -it mariadb bash

$ mysql -uroot -p1234

$ grant all privileges on *.* to 'root'@'%' identified by '1234';
```
