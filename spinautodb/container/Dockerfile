FROM opsmx11/java:ubuntu16_java8

MAINTAINER OpsMx

#COPY /run.sh  /usr/local/bin/run.sh

#RUN apt-get update && apt-get install -y postgresql-client

RUN groupadd -g 999 spinautodb && \
    useradd -r -u 999 -g spinautodb spinautodb

RUN mkdir -p /home/spinautodb

RUN chown spinautodb /home/spinautodb

USER spinautodb

RUN mkdir -p /home/spinautodb/artifact/ /home/spinautodb/opsmx/app/config/ /home/spinautodb/opsmx/db/ /home/spinautodb/opsmx/artifact/

RUN touch /home/spinautodb/artifact/spinautodb.log && \
    chmod 777 /home/spinautodb/artifact/spinautodb.log

WORKDIR /home/spinautodb

COPY /run.sh  /usr/local/bin/run.sh

COPY /spinautodb.jar  /home/spinautodb/artifact/spinautodb.jar

#CMD run.sh
