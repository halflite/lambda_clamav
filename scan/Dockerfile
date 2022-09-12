# Dockerfile
# Build jar
FROM public.ecr.aws/docker/library/maven:3.8-amazoncorretto-11 as build
WORKDIR /root

# pom.xml at first resolve dependency
COPY pom.xml .
RUN mvn -B package

# copy source/build jar
COPY src src
RUN mvn -B package

# Build image
FROM public.ecr.aws/lambda/java:11 as deploy
RUN yum -y install clamav
RUN freshclam
WORKDIR ${LAMBDA_TASK_ROOT}
COPY --from=build /root/target/scan.jar ./
RUN jar -xvf scan.jar
CMD [ "net.halflite.scan.Handler::handleRequest" ]
