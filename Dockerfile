FROM maven:3.6.3-jdk-11 as build
WORKDIR /app
COPY . /app
RUN mvn clean package
FROM amazoncorretto:11.0.6
WORKDIR /app
EXPOSE 8080
COPY --from=build /app/ /app/
RUN chmod -R 777 /app/
RUN ls -altr
ENTRYPOINT ["sh", "-c"]
CMD ["java -javaagent:/app/opentracing-specialagent-1.5.8.jar -Dsa.tracer=jaeger -Dsa.log.level=FINE -jar ./target/pass-the-buck-1.0.0.jar"]
