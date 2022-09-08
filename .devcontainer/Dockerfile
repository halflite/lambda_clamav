FROM maven:3.8-amazoncorretto-11
RUN amazon-linux-extras install epel -y
RUN yum -y update && \
  yum -y install git clamav clamav-update clamd
RUN freshclam