# Apache Kafka

- Apache Software Foundation의 Scalar 언어로 된 오픈 소스 메시지 브로커 프로젝트
- 링크드인에서 개발, 2011년 오픈소스화, 2014년 Confluent라는 회사 창립
- 실시간 데이터 피드 관리를 위한 높은 처리량, 낮은 지연 시간을 지닌 플랫폼 제공

### 기존 End-to-End 시스템의 문제

- 데이터 연동의 복잡성 증가
- 서로 다른 데이터 Pipeline 연결 구조
- 확장이 어렵다

### 특징

- Producer/Consumer 분리
- 메시지를 여러 Consumer에게 허용
- 높은 처리량을 위한 메시지 최적화
- Scale-out 가능
- Eco-system

```
            Zookeeper : 메타데이터(Broker ID, Controller ID 등)저장
      /         |           \
  ---------------------------------
  |Broker #0  Broker #1  Broker #2| --> Kafka Cluster : 보통 3대 이상의 Broker로 구성된다.
  ---------------------------------
n개의 Broker 중에서 1대는 Controller 기능을 수행한다.

Controller?
  - 각 Broker에게 담당 파티션을 할당한다.
  - Broker가 정상 동작하는지 모니터링한다.
```

### Kafka 홈페이지

- http://kafka.apache.org

### Kafka와 데이터를 주고받기 위해 사용하는 Java Library

- https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients

### Zookeeper 및 Kafka 서버 기동

```
$KAFKA_HOME/bin/zookeeper-server-start.sh  $KAFKA_HOME/config/zookeeper.properties

$KAFKA_HOME/bin/kafka-server-start.sh  $KAFKA_HOME/config/server.properties
```

### Topic 생성

```
$KAFKA_HOME/bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server localhost:9092 \
--partitions 1
```

### Topic 목록 확인

```
$KAFKA_HOME/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

### Topic 정보 확인

```
$KAFKA_HOME/bin/kafka-topics.sh --describe --topic quickstart-events --bootstrap-server localhost:9092
```

### Windows에서 기동

- 모든 명령어는 $KAFKA_HOME\bin\windows 폴더에 저장

```
.\bin\windows\zookeeper-server-start.bat  .\config\zookeeper.properties
```

### 메시지 생산

```
$KAFKA_HOME/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic quickstart-events
```

### 메시지 소비

```
$KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic quickstart-events \
--from-beginning
```

## Kafka Connect

Kafka Connect를 통해 데이터를 Import/Export 할 수 있다.

```
데이터 소스(Hive, jdbc, ...) => Kafka Connect Source => Kafka Cluster => Kafka Connect Sink => Target System(S3, ...)
```

### 설치
1. kafka connect 설치
```
$ curl -O http://packages.confluent.io/archive/6.1/confluent-community-6.1.0.tar.gz

$ tar xvf confluent-community-6.1.0.tar.gz
```

2. kafka connect 플러그인 설치   
원하는 플러그인을 찾아 다운받으면 된다. https://www.confluent.io/hub/
```
A Kafka Connect plugin can be:
    
a directory on the file system that contains all required JAR files 
and third-party dependencies for the plugin.

This is most common and is preferred.
```
예제로 jdbc connector를 설치  
https://www.confluent.io/hub/confluentinc/kafka-connect-jdbc
- confluentinc-kafka-connect-jdbc-10.2.5.zip

3. 플러그인 설정  
플러그인이 위치한 경로를 설정 파일에 지정한다.
```
Kafka Connect finds the plugins using a plugin path 
defined as a comma-separated list of directory paths 
in the plugin.path worker configuration property. 

To install a plugin, place the plugin directory
```
- /Users/hyunjin/dev/kafka/confluent-6.1.0/etc/kafka/connect-distributed.properties 파일 수정
```
plugin.path=/Users/hyunjin/dev/kafka/confluentinc-kafka-connect-jdbc-10.2.5/lib
```

4. JdbcSourceConnector에서 MariaDB를 사용하기 위해 mariadb 드라이버 필요
- /Users/hyunjin/dev/kafka/confluent-6.1.0/share/java/kafka 경로에 mariadb-java-client-2.7.2.jar 파일 복사

5. kafka connect 실행
```
# 실행
$ /Users/hyunjin/dev/kafka/confluent-6.1.0/bin/connect-distributed etc/kafka/connect-distributed.properties

# 구동 확인
$ curl http://localhost:8083/

# 설치된 플러그인 확인
$ curl http://localhost:8083/connector-plugins | python -m json.tool
```

### kafka source connect 추가 (MariaDB)
```
$ echo '
{
    "name" : "my-source-connect",
    "config" : {
        "connector.class" : "io.confluent.connect.jdbc.JdbcSourceConnector",
        "connection.url":"jdbc:mysql://localhost:3306/mydb",
        "connection.user":"root",
        "connection.password":"1234",
        "mode": "incrementing",
        "incrementing.column.name" : "id",
        "table.whitelist":"users",
        "topic.prefix" : "my_topic_",
        "tasks.max" : "1"
    }
}
' | curl -X POST -d @- http://localhost:8083/connectors --header "content-Type:application/json"

# 커넥터 등록 확인
$ curl http://localhost:8083/connectors
 
# 커넥터 상태 정보
$ curl http://localhost:8083/connectors/my-source-connect/status

# 생성된 토픽 확인
$ docker exec -ti kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```
커넥터를 생성하기 위해 /connectors 경로로 위와 같은 정보들을 body에 담아서 커넥터를 생성한다.  
jdbc 커넥터의 설정옵션은 간단하게 다음과 같다.

- connection.url, connection.user, connection.password  
  : DB에 접속하기 위한 설정 정보
- mode, incrementing. colmn.name  
  : 실행하고 있는 동안 커넥터는 jdbc를 통해 rdb를 폴링한다. 변경이 있으면 카프카에 전달하고 변경 감지는 incrementing 방법으로 진행한다. mode는 incrementing외에도 bulk, timestamp 등이 있다. incrementing.column.name 을 통해 변경을 감지한다.
- table.whitelist  
  : 로드할 대상의 테이블을 지정한다. 반대로 blacklist 도 있다.
- topic-prefix  
  : 카프카에 데이터를 넣을때 토픽 명을 결정할 접두어를 지정한다.
- tasks.max  
  : 이 커넥터에서 만들어지는 최소의 테스크 수

이제 source(connection config에 설정된 db와 table)에 변경이 발생하면
이를 감지하고 있다가 kafka 메시지를 만든다.
```
# 생성된 메시지 확인
$ docker exec -ti kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic my_topic_users --from-beginning
```
아래와 같은 메시지가 만들어진다.
```
{
    "schema":{
        "type":"struct",
        "fields":[
            {
                "type":"int32",
                "optional":false,
                "field":"id"
            },
            {
                "type":"string",
                "optional":true,
                "field":"user_id"
            },
            {
                "type":"string",
                "optional":true,
                "field":"pwd"
            },
            {
                "type":"string",
                "optional":true,
                "field":"name"
            },
            {
                "type":"int64",
                "optional":true,
                "name":"org.apache.kafka.connect.data.Timestamp",
                "version":1,
                "field":"created_at"
            }
        ],
        "optional":false,
        "name":"users"
    },
    "payload":{
        "id":2,
        "user_id":"user2",
        "pwd":"1234",
        "name":"lee",
        "created_at":1638687468000
    }
}
```

### kafka sink connect 추가 (MariaDB)    
아래 예제에서는 my_topic_users 토픽으로 들어온 메시지를 보고 토픽과 동일한 이름의 테이블을 생성해 데이터를 복사할 것이다.
```
echo '
{
    "name":"my-sink-connect",
    "config":{
        "connector.class":"io.confluent.connect.jdbc.JdbcSinkConnector",
        "connection.url":"jdbc:mysql://localhost:3306/mydb",
        "connection.user":"root",
        "connection.password":"1234",
        "auto.create":"true",
        "auto.evolve":"true",
        "delete.enabled":"false",
        "tasks.max":"1",
        "topics":"my_topic_users"
    }
}
'| curl -X POST -d @- http://localhost:8083/connectors --header "content-Type:application/json"
```

### kafka consumer group 확인
```
$ docker exec -ti kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
```

### kafka consumer group 상제 정보
```
$ docker exec -ti kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group connect-my-sink-connect --describe 
```

### kafka consumer group의 offset 변경하기
```
$ docker exec -ti kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group connect-my-sink-connect --topic my_topic_users --reset-offsets --to-latest --execute

오프셋의 위치를 재설정하기 위한 아래와같은 상세 옵션들이 있다.

--shift-by <Long: number-of-offsets> 형식 (+/- 모두 가능)
--to-offset <Long: offset>
--to-current
--by-duration <String: duration> : 형식 ‘PnDTnHnMnS’
--to-datetime <String: datetime> : 형식 ‘YYYY-MM-DDTHH:mm:SS.sss’
--to-latest
--to-earliest
```