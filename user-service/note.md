### 실행방법
```
$ mvn spring-boot:run -"Dspring-boot.run.jvmArguments='-Dserver.port=900x'"
or 
$ mvn package
$ java -jar -"Dserver.port=900x" .\target\user-service-0.0.1-SNAPSHOT.jar

포트를 지정하지 않으면 무작위 포트 사용(server.port=0으로 설정함)
```

### 로그인 과정
```
AuthenticationFilter
    attemptAuthentication()
        |
        V
UsernamePasswordAuthenticationToken
        |
        V
UserDetailService
    loadUserByUsername() -> UserRepository
                                findByEmail()
        |
        V
AuthenticationFilter
    successfulAuthentication() {
        ...
        Jwt 생성 후 헤더에 넣어줌   
    }
    
```