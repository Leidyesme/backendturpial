# =========================================================================
# ETAPA 1: COMPILACIÓN Y EMPAQUETADO (Maven)
# =========================================================================
FROM maven:3.9-eclipse-temurin-17 AS builder

# Directorio de trabajo dentro del contenedor de compilación
WORKDIR /app

# Copiar el descriptor del proyecto de dependencias Maven
COPY pom.xml .

# Copiar el código fuente de Java
COPY src ./src

# Copiar los recursos y páginas web estáticas/dinámicas de la carpeta web
COPY web ./web

# Compilar y empaquetar en formato .war omitiendo los test unitarios
RUN mvn clean package -DskipTests

# =========================================================================
# ETAPA 2: EJECUCIÓN (Apache Tomcat 10)
# =========================================================================
FROM tomcat:10.1-jdk17

# Eliminar las aplicaciones predeterminadas que vienen con Tomcat (Root, Docs, etc.)
RUN rm -rf /usr/local/tomcat/webapps/*

# Copiar el archivo .war generado en la etapa de compilación y renombrarlo como turpialJava.war
# Esto permite que la API sea accesible bajo el contexto "/turpialJava"
COPY --from=builder /app/target/turpialJava-*.war /usr/local/tomcat/webapps/turpialJava.war

# Exponer el puerto por defecto en el contenedor
EXPOSE 8080

# Iniciar el servidor Tomcat
CMD ["catalina.sh", "run"]
