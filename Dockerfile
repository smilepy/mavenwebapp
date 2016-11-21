FROM java:8
MAINTAINER smilepy "peiyu617@163.com"


#安装tomcat
RUN mkdir /var/tmp/tomcat
RUN wget -P /var/tmp/tomcat http://mirrors.cnnic.cn/apache/tomcat/tomcat-9/v9.0.0.M11/bin/apache-tomcat-9.0.0.M11.tar.gz
RUN tar xzf /var/tmp/tomcat/apache-tomcat-9.0.0.M11.tar.gz -C /var/tmp/tomcat
RUN rm -rf /var/tmp/tomcat/apache-tomcat-9.0.0.M11.tar.gz

RUN mkdir /var/tmp/webapp
#ADD ./ /var/tmp/webapp
#RUN cd /var/tmp/webapp && mvn package && cp /var/tmp/webapp/target/CIJD.war /var/tmp/tomcat/apache-tomcat-9.0.0.M11/webapps
ADD ./target/*.war /var/tmp/webapp
RUN cd /var/tmp/webapp && ls  -al
RUN cp -r /var/tmp/webapp/* /var/tmp/tomcat/apache-tomcat-9.0.0.M11/webapps

EXPOSE 8080

CMD ["./var/tmp/tomcat/apache-tomcat-9.0.0.M11/bin/catalina.sh","run"]

