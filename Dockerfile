# Use a imagem base do Eclipse Temurin com JDK 17
FROM eclipse-temurin:21-jdk

# Instale o Maven
RUN apt-get update && \
    apt-get install -y maven

# Configurar o diretório de trabalho
WORKDIR /usr/src/app

# Copiar o código-fonte para o contêiner
COPY . .

# Executar o comando Maven para compilar e empacotar o aplicativo
RUN mvn clean package

# Copiar qualquer arquivo JAR no diretório target para app.jar
RUN cp ./target/*.jar app.jar

# Expor a porta necessária
EXPOSE 8080

# Comando padrão para iniciar o aplicativo
CMD ["java", "-jar", "/usr/src/app/app.jar"]
