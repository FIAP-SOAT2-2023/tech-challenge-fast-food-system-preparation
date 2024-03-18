FROM maven:3.9.4-amazoncorretto-21-debian
VOLUME /tmp
COPY . .
RUN mvn clean package
ENV TZ=America/Sao_Paulo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
EXPOSE 8080
ENTRYPOINT ["java","-jar", "/target/preparation-0.0.1-SNAPSHOT.jar"]